package com.coptimize.openinventory.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.coptimize.openinventory.data.NonAuthDb
import com.coptimize.openinventory.data.ProductQueries
import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.repository.AuthCustomerRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthProductRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthSaleRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthSavedCartRepositoryImpl
import com.coptimize.openinventory.data.repository.CustomerRepository
import com.coptimize.openinventory.data.repository.NonAuthCustomerRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthSavedCartRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthProductRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthSaleRepositoryImpl
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.data.repository.SaleRepository
import com.coptimize.openinventory.data.repository.SavedCartRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    // You can keep these as private properties of the object, as they are compile-time constants.
    private val allTriggers = listOf(
        "CREATE TRIGGER IF NOT EXISTS set_paid_amount_default BEFORE INSERT ON sales FOR EACH ROW WHEN NEW.paid_amount IS NULL BEGIN     UPDATE sales SET paid_amount = NEW.total WHERE rowid = NEW.rowid; END;",
        "CREATE TRIGGER IF NOT EXISTS update_customers_updated_at AFTER UPDATE ON customers FOR EACH ROW BEGIN    UPDATE customers SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;",
        "CREATE TRIGGER IF NOT EXISTS update_out_of_stock_log_updated_at AFTER UPDATE ON out_of_stock_log FOR EACH ROW BEGIN    UPDATE out_of_stock_log SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;",
        "CREATE TRIGGER IF NOT EXISTS update_products_updated_at AFTER UPDATE ON products FOR EACH ROW BEGIN    UPDATE products SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;",
        "CREATE TRIGGER IF NOT EXISTS update_sale_items_updated_at AFTER UPDATE ON sale_items FOR EACH ROW BEGIN    UPDATE sale_items SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;",
        "CREATE TRIGGER IF NOT EXISTS update_sales_updated_at AFTER UPDATE ON sales FOR EACH ROW BEGIN    UPDATE sales SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;",
        "CREATE TRIGGER IF NOT EXISTS update_saved_carts_updated_at AFTER UPDATE ON saved_carts FOR EACH ROW BEGIN    UPDATE saved_carts SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;",
        "CREATE TRIGGER IF NOT EXISTS update_stocks_updated_at AFTER UPDATE ON stocks FOR EACH ROW BEGIN    UPDATE stocks SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;"
    )

    private val authTriggers = allTriggers + listOf(
        "CREATE TRIGGER IF NOT EXISTS restrict_product_inserts_to_admins BEFORE INSERT ON products FOR EACH ROW WHEN (SELECT role FROM users WHERE id = NEW.user_id) NOT IN ('admin', 'superadmin') BEGIN SELECT RAISE(FAIL, 'Permission denied: only admin or superadmin can add a product'); END;",
        "CREATE TRIGGER IF NOT EXISTS restrict_product_updates_to_users BEFORE UPDATE ON products FOR EACH ROW WHEN (SELECT role FROM users WHERE id = NEW.user_id) NOT IN ('staff', 'admin', 'superadmin') BEGIN SELECT RAISE(FAIL, 'Permission denied: only users can update a product'); END;",
        "CREATE TRIGGER IF NOT EXISTS restrict_stock_inserts_to_admins BEFORE INSERT ON stocks FOR EACH ROW WHEN (SELECT role FROM users WHERE id = NEW.user_id) NOT IN ('admin', 'superadmin') BEGIN SELECT RAISE(FAIL, 'Permission denied: only admin or superadmin can modify stock'); END;",
        "CREATE TRIGGER IF NOT EXISTS restrict_stock_updates_to_admins BEFORE UPDATE ON stocks FOR EACH ROW WHEN (SELECT role FROM users WHERE id = NEW.user_id) NOT IN ('admin', 'superadmin') BEGIN SELECT RAISE(FAIL, 'Permission denied: only admin or superadmin can update stock'); END;",
        "CREATE TRIGGER IF NOT EXISTS update_user_updated_at AFTER UPDATE ON users FOR EACH ROW BEGIN    UPDATE users SET updated_at = CURRENT_TIMESTAMP WHERE id = OLD.id; END;"
    )

    private fun isAuthMode(context: Context): Boolean {
        val authDbFile = context.getDatabasePath("inventory.auth.db")
        return authDbFile.exists()
    }

    @Provides
    @Singleton
    fun provideNonAuthDatabase(@ApplicationContext context: Context): NonAuthDb {
        // --- DEFINE THE CALLBACK *INSIDE* THE FUNCTION ---
        val driverCallback = object : AndroidSqliteDriver.Callback(NonAuthDb.Schema) {
            override fun onConfigure(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onConfigure(db)
                db.setForeignKeyConstraintsEnabled(true)
            }

            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                println("Database created, applying programmatic triggers.")
                allTriggers.forEach { triggerQuery ->
                    db.execSQL(triggerQuery.trimIndent())
                }
            }
        }

        val dbFile = context.getDatabasePath("inventory.db")
        if (!dbFile.exists()) {
            if (context.assets.list("")?.contains("inventory.db") == true) {
                dbFile.parentFile?.mkdirs()
                context.assets.open("inventory.db").use { input ->
                    dbFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            }
        }

        val driver: SqlDriver = AndroidSqliteDriver(
            schema = NonAuthDb.Schema,
            context = context,
            name = "inventory.db",
            callback = driverCallback
        )
        return NonAuthDb(driver)
    }

    @Provides
    @Singleton
    fun provideAuthDatabase(@ApplicationContext context: Context): AuthDb {
        // --- DEFINE THE CALLBACK *INSIDE* THE FUNCTION ---
        val authDriverCallback = object : AndroidSqliteDriver.Callback(AuthDb.Schema) {
            override fun onConfigure(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onConfigure(db)
                db.setForeignKeyConstraintsEnabled(true)
            }

            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                println("Auth Database created, applying programmatic triggers.")
                authTriggers.forEach { triggerQuery ->
                    db.execSQL(triggerQuery.trimIndent())
                }
            }
        }

        val driver: SqlDriver = AndroidSqliteDriver(
            schema = AuthDb.Schema,
            context = context,
            name = "inventory.auth.db",
            callback = authDriverCallback
        )
        return AuthDb(driver)
    }

    @Provides
    @Singleton
    fun provideProductQueries(db: NonAuthDb): ProductQueries {
        return db.productQueries
    }

    // Auth DB Queries
    @Provides
    @Singleton
    fun provideAuthProductQueries(db: AuthDb): com.coptimize.openinventory.data.auth.ProductQueries {
        return db.productQueries
    }

    @Provides
    @Singleton
    fun provideProductRepository(
        @ApplicationContext context: Context,
        nonAuthDb: NonAuthDb,
        authDb: AuthDb
    ): ProductRepository { // Provide the INTERFACE
        return if (isAuthMode(context)) {
            // Hilt now knows how to get authDb because it's in the same module
            AuthProductRepositoryImpl(authDb)
        } else {
            // Hilt now knows how to get nonAuthDb because it's in the same module
            NonAuthProductRepositoryImpl(nonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideSaleQueries(db: NonAuthDb): com.coptimize.openinventory.data.SaleQueries {
        return db.saleQueries
    }

    @Provides
    @Singleton
    fun provideSaleItemQueries(db: NonAuthDb): com.coptimize.openinventory.data.SaleItemQueries {
        return db.saleItemQueries
    }

    // Auth DB Queries
    @Provides
    @Singleton
    fun provideAuthSaleQueries(db: AuthDb): com.coptimize.openinventory.data.auth.SaleQueries {
        return db.saleQueries
    }

    @Provides
    @Singleton
    fun provideAuthSaleItemQueries(db: AuthDb): com.coptimize.openinventory.data.auth.SaleItemQueries {
        return db.saleItemQueries
    }

    @Provides @Singleton fun provideCustomerQueries(db: NonAuthDb) = db.customerQueries
    @Provides @Singleton fun provideAuthCustomerQueries(db: AuthDb) = db.customerQueries

    @Provides
    @Singleton
    fun provideSaleRepository(
        @ApplicationContext context: Context,
        nonAuthDb: NonAuthDb,
        authDb: AuthDb
    ): SaleRepository { // Provide the INTERFACE
        return if (isAuthMode(context)) {
            AuthSaleRepositoryImpl(authDb)
        } else {
            // You need to create this NonAuthSaleRepositoryImpl class
            NonAuthSaleRepositoryImpl(nonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideSavedCartRepository(
        @ApplicationContext context: Context,
        nonAuthDb: NonAuthDb,
        authDb: AuthDb,
    ): SavedCartRepository {
        return if (isAuthMode(context)) {
            AuthSavedCartRepositoryImpl(authDb)
        } else {
            // You need to create this NonAuthSavedCartRepositoryImpl class
            NonAuthSavedCartRepositoryImpl(nonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(
        @ApplicationContext context: Context,
        nonAuthDb: NonAuthDb,
        authDb: AuthDb
    ): CustomerRepository {
        return if (isAuthMode(context)) {
            AuthCustomerRepositoryImpl(authDb)
        } else {
            NonAuthCustomerRepositoryImpl(nonAuthDb)
        }
    }
}