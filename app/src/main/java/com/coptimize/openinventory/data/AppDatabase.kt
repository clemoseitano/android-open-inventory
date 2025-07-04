package com.coptimize.openinventory.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.coptimize.openinventory.data.dao.CustomerDao
import com.coptimize.openinventory.data.dao.ProductDao
import com.coptimize.openinventory.data.dao.SaleDao
import com.coptimize.openinventory.data.dao.UserDao
import com.coptimize.openinventory.data.model.Product
import com.coptimize.openinventory.data.model.Customer
import com.coptimize.openinventory.data.model.Sale
import com.coptimize.openinventory.data.model.SaleItem
import com.coptimize.openinventory.data.model.User
import com.coptimize.openinventory.data.model.Stock
import com.coptimize.openinventory.data.model.SavedCart
import com.coptimize.openinventory.data.model.OutOfStockLog

@Database(
    entities = [
        Product::class, Customer::class, Sale::class, SaleItem::class,
        User::class, Stock::class, SavedCart::class, OutOfStockLog::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun userDao(): UserDao
    abstract fun customerDao(): CustomerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory.db"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}