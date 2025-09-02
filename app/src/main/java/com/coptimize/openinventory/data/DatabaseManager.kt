package com.coptimize.openinventory.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.coptimize.openinventory.data.auth.AuthDb
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferenceManager: PreferenceManager
) {
    private var nonAuthDriver: SqlDriver? = null
    private var authDriver: SqlDriver? = null

    /**
     * Helper function to read a SQL script from assets and execute it.
     * It splits the script by semicolons and runs each statement individually.
     */
    private fun executeSqlScript(db: SupportSQLiteDatabase, fileName: String) {
        try {
            val inputStream = context.assets.open(fileName)
            val script = inputStream.bufferedReader().use { it.readText() }

            // The regular expression to split by semicolons that are followed by
            // zero or more whitespace characters and then either a newline or the end of the file.
            val regex = ";\\s*(\\n|$)".toRegex()

            // Split the script using the regex. This is the direct equivalent of the C++ line.
            val statements: List<String> = script.split(regex)
                .filter { it.isNotBlank() }

            statements.forEach { statement ->
                // Execute each statement, ignoring empty lines
                if (statement.trim().isNotEmpty()) {
                    db.execSQL(statement)
                }
            }
            println("Successfully executed SQL script: $fileName")
        } catch (e: IOException) {
            e.printStackTrace()
            // Handle the error, maybe log it or re-throw as a runtime exception
            throw RuntimeException("Failed to read or execute SQL script: $fileName", e)
        }
    }


    fun getDb(): Any {
        return if (preferenceManager.isAuthModeEnabled()) {
            getAuthDb()
        } else {
            getNonAuthDb()
        }
    }

    fun getAuthDb(): AuthDb {
        synchronized(this) {
            if (authDriver == null) {
                val authDriverCallback = object : AndroidSqliteDriver.Callback(AuthDb.Schema) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        executeSqlScript(db, "sql/auth/inventory_0001.sql")
                    }
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.setForeignKeyConstraintsEnabled(true)
                    }
                }
                authDriver = AndroidSqliteDriver(
                    schema = AuthDb.Schema,
                    context = context,
                    name = "inventory.db.auth",
                    callback = authDriverCallback
                )
            }
            return AuthDb(authDriver!!)
        }
    }

    fun getNonAuthDb(): NonAuthDb {
        synchronized(this) {
            if (nonAuthDriver == null) {
                val nonAuthDriverCallback = object : AndroidSqliteDriver.Callback(NonAuthDb.Schema) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        executeSqlScript(db, "sql/inventory_0001.sql")
                    }
                    override fun onConfigure(db: SupportSQLiteDatabase) {
                        db.setForeignKeyConstraintsEnabled(true)
                    }
                }
                nonAuthDriver = AndroidSqliteDriver(
                    schema = AuthDb.Schema,
                    context = context,
                    name = "inventory.db",
                    callback = nonAuthDriverCallback
                )
            }
            return NonAuthDb(nonAuthDriver!!)
        }
    }

    /**
     * Forcefully closes the singleton connection to the non-auth database.
     */
    fun closeNonAuthDb() {
        synchronized(this) {
            nonAuthDriver?.close()
            nonAuthDriver = null
        }
    }
}