import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object MigrationManager {
    suspend fun migrateToAuthMode(context: Context) {
        withContext(Dispatchers.IO) {
            // 1. Get instances of both old and new databases
            val oldDb = DatabaseManager.getDatabase(context) as NonAuthDb
            // This will create the new empty auth.db file
            val newDb = DatabaseManager.getAuthDatabase(context)

            // 2. Read all data from the old DB and insert into the new DB
            // The 'transaction' ensures this is all-or-nothing.
            newDb.transaction {
                // Migrate Products
                val allProducts = oldDb.productQueries.selectAllActive().executeAsList()
                allProducts.forEach { oldProduct ->
                    newDb.productQueries.insert(
                        id = oldProduct.id,
                        name = oldProduct.name,
                        userId = "your_superadmin_id" // Assign the new user ID
                    )
                }

                // Migrate Customers
                // ...

                // Migrate Sales
                // ...
            }

            // 3. (Optional but recommended) After successful migration, delete the old DB
            context.deleteDatabase("inventory.db")
        }
    }
}