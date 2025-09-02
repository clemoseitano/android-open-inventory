package com.coptimize.openinventory.data.repository

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import com.coptimize.openinventory.data.DatabaseManager
import com.coptimize.openinventory.data.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import javax.inject.Inject

class MigrationRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager,
    private val databaseManager: DatabaseManager
) : MigrationRepository {

    // Helper function to execute a script file from assets.
    private fun executeSqlScript(driver: SupportSQLiteDatabase, script: String): Boolean {
        try {
            // The regular expression to split by semicolons that are followed by
            // zero or more whitespace characters and then either a newline or the end of the file.
            val regex = ";\\s*(\\n|$)".toRegex()

            // Split the script using the regex. This is the direct equivalent of the C++ line.
            val statements: List<String> = script.split(regex)
                .filter { it.isNotBlank() }

            try {
                statements.forEach { statement ->
                    // Execute each statement, ignoring empty lines
                    if (statement.trim().isNotEmpty()) {
                        driver.execSQL(statement.trim())
                    }
                }
            } catch (e: Exception) {
                // Step 5: If any SQL statement failed, roll back the entire transaction.
                println("Error executing SQL script. Rolling back transaction.")
                e.printStackTrace()
                driver.execSQL("ROLLBACK;")
                // Re-throw the exception so the calling code knows the migration failed.
                throw e
            }
        } catch (e: IOException) {
            driver.execSQL("ROLLBACK;")
            e.printStackTrace()
            // Handle the error, maybe log it or re-throw as a runtime exception
            throw RuntimeException("Failed to read or execute SQL script", e)
        }
        return true
    }

    private fun executeSqlScript2(db: SupportSQLiteDatabase, assetPath: String) {
        println(assetPath)
//        val script = context.assets.open(assetPath).bufferedReader().use { it.readText() }
        // The script itself will manage its own transaction (BEGIN/COMMIT)
        db.execSQL(assetPath)
    }

    override suspend fun performMigration(username: String, passwordHash: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            // Forcefully close any existing connection the app might have to the old DB.
            databaseManager.closeNonAuthDb()

            val oldDatabasePath = context.getDatabasePath("inventory.db").absolutePath
            val newDatabaseFile = File("$oldDatabasePath.auth")
            var dbHelper: SupportSQLiteOpenHelper? = null
            var db: SupportSQLiteDatabase? = null
            var success = false

            try {
                // --- Step 1: Create a raw SQLite connection helper, BYPASSING SQLDelight Schema ---
                val config = SupportSQLiteOpenHelper.Configuration.builder(context)
                    .name(newDatabaseFile.name)
                    .callback(object : SupportSQLiteOpenHelper.Callback(1) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            // This is called when the DB file is first created.
                            // We run our base schema script here.
                            val migrationScript = context.assets.open("sql/auth/inventory_0001.sql")
                                .bufferedReader().use { it.readText() }
                            println("Creating AuthDb schema from sql/auth/inventory_0001.sql")
                            executeSqlScript(db, migrationScript)
                        }
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                            // Not needed for this one-time migration
                        }
                    })
                    .build()
                databaseManager.closeNonAuthDb()
                dbHelper = FrameworkSQLiteOpenHelperFactory().create(config)
                db = dbHelper.writableDatabase // This triggers onCreate if needed.

                // --- Step 2: Manually run the migration transaction ---
                db.beginTransaction()

                // Create Superuser
                var superuserId:String? = null
                db.execSQL(
                    "INSERT INTO users (username, password_hash, role) VALUES (?, ?, 'superadmin');",
                    arrayOf(username, passwordHash)
                )
                val cursor = db.query("SELECT id FROM users WHERE username = ?", arrayOf(username))
                cursor.use { // .use ensures the cursor is always closed
                    if (it.moveToFirst()) {
                        superuserId = it.getString(0)
                    }
                }

                if (superuserId == null) {
                    throw IllegalStateException("Failed to create or retrieve superadmin user after insert.")
                }
                // Run the data migration script from assets
                val migrationScript = context.assets.open("sql/auth/migration.sql")
                    .bufferedReader().use { it.readText() }
                    .replace("__OLD_DB_PATH_PLACEHOLDER__", oldDatabasePath)
                    .replace("__SUPERADMIN_ID_PLACEHOLDER__", superuserId)
                db.execSQL("ATTACH DATABASE '$oldDatabasePath' AS old_db;")
                executeSqlScript(db, migrationScript)
//                db.execSQL("DETACH DATABASE old_db;")

                // Add final user restrictions
                db.execSQL("ALTER TABLE users ADD COLUMN creator_user_id TEXT REFERENCES users(id)")
                val restrictUserInsertsSql = """
                    CREATE TRIGGER IF NOT EXISTS restrict_user_inserts_to_superadmins
                    BEFORE INSERT ON users
                    WHEN (SELECT role FROM users WHERE id = NEW.creator_user_id) NOT IN ('superadmin')
                    BEGIN
                        SELECT RAISE(FAIL, 'Permission denied: only superadmin can add a user');
                    END;
                """.trimIndent()

                val restrictUserRoleChangeSql = """
                    CREATE TRIGGER IF NOT EXISTS restrict_user_role_change_to_superadmins
                    BEFORE UPDATE ON users
                    FOR EACH ROW
                    WHEN (SELECT role FROM users WHERE id = NEW.creator_user_id) != 'superadmin'
                    BEGIN
                        SELECT RAISE(FAIL, 'Permission denied: only superadmin can change a user role');
                    END;
                """.trimIndent()
                db.execSQL(restrictUserInsertsSql)
                db.execSQL(restrictUserRoleChangeSql)

                db.setTransactionSuccessful() // Mark transaction as successful before ending
                success = true

            } catch (e: Exception) {
                e.printStackTrace()
                success = false
            } finally {
                // End the transaction and close the raw connection.
                db?.endTransaction()
                dbHelper?.close()
            }

            // --- Step 3: Finalize ---
            if (success) {
                preferenceManager.setAuthModeEnabled(true)
//                context.deleteDatabase("inventory.db")
                Result.success(Unit)
            } else {
                context.deleteDatabase(newDatabaseFile.name) // Cleanup failed migration
                Result.failure(Exception("Migration failed. The operation was rolled back."))
            }
        }
    }
}