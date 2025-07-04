import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

object DatabaseManager {
    fun isAuthMode(context: Context): Boolean {
        // Your logic to detect auth mode.
        // For example, check if the "auth.db" file exists.
        val authDbFile = context.getDatabasePath("inventory.auth.db")
        return authDbFile.exists()
    }

    fun getDatabase(context: Context): Any { // Return 'Any' or a custom interface
        return if (isAuthMode(context)) {
            getAuthDatabase(context)
        } else {
            getNonAuthDatabase(context)
        }
    }

    private fun getNonAuthDatabase(context: Context): NonAuthDb {
        val driver = AndroidSqliteDriver(NonAuthDb.Schema, context, "inventory.db")
        return NonAuthDb(driver)
    }

    private fun getAuthDatabase(context: Context): AuthDb {
        val driver = AndroidSqliteDriver(AuthDb.Schema, context, "inventory.auth.db")
        return AuthDb(driver)
    }
}