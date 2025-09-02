package com.coptimize.openinventory.di

import android.content.Context
import com.coptimize.openinventory.data.DatabaseManager
import com.coptimize.openinventory.data.NonAuthDb
import com.coptimize.openinventory.data.PreferenceManager
import com.coptimize.openinventory.data.ProductQueries
import com.coptimize.openinventory.data.auth.AuthDb
import com.coptimize.openinventory.data.model.User
import com.coptimize.openinventory.data.repository.AppSetupRepository
import com.coptimize.openinventory.data.repository.AppSetupRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthCustomerRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthProductRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthSaleRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthSavedCartRepositoryImpl
import com.coptimize.openinventory.data.repository.AuthUserRepositoryImpl
import com.coptimize.openinventory.data.repository.CustomerRepository
import com.coptimize.openinventory.data.repository.MigrationRepository
import com.coptimize.openinventory.data.repository.MigrationRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthCustomerRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthProductRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthSaleRepositoryImpl
import com.coptimize.openinventory.data.repository.NonAuthSavedCartRepositoryImpl
import com.coptimize.openinventory.data.repository.ProductRepository
import com.coptimize.openinventory.data.repository.SaleRepository
import com.coptimize.openinventory.data.repository.SavedCartRepository
import com.coptimize.openinventory.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    fun provideNonAuthDatabase(databaseManager: DatabaseManager): NonAuthDb {
        return databaseManager.getNonAuthDb()
    }

    @Provides
    fun provideAuthDatabase(databaseManager: DatabaseManager): AuthDb {
        return databaseManager.getAuthDb()
    }

    @Provides
    @Singleton
    fun provideProductRepository(dbManager: DatabaseManager): ProductRepository {
        // The getDb() function returns the correct type based on the auth mode flag.
        val db = dbManager.getDb()
        return if (db is AuthDb) {
            AuthProductRepositoryImpl(db)
        } else {
            NonAuthProductRepositoryImpl(db as NonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideSaleRepository(dbManager: DatabaseManager): SaleRepository {
        val db = dbManager.getDb()
        return if (db is AuthDb) {
            AuthSaleRepositoryImpl(db)
        } else {
            NonAuthSaleRepositoryImpl(db as NonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideCustomerRepository(dbManager: DatabaseManager): CustomerRepository {
        val db = dbManager.getDb()
        return if (db is AuthDb) {
            AuthCustomerRepositoryImpl(db)
        } else {
            NonAuthCustomerRepositoryImpl(db as NonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideSavedCartRepository(dbManager: DatabaseManager): SavedCartRepository {
        val db = dbManager.getDb()
        return if (db is AuthDb) {
            AuthSavedCartRepositoryImpl(db)
        } else {
            NonAuthSavedCartRepositoryImpl(db as NonAuthDb)
        }
    }

    @Provides
    @Singleton
    fun provideUserRepository(dbManager: DatabaseManager): UserRepository {
        val db = dbManager.getDb()
        return if (db is AuthDb) {
            AuthUserRepositoryImpl(db)
        } else {
            // Provide the non-functional placeholder for non-auth mode.
            object : UserRepository { /* ... empty implementation ... */
                override fun getAllUsers(): Flow<List<User>> {
                    TODO("Not yet implemented")
                }

                override suspend fun deleteUser(userId: String) {
                    TODO("Not yet implemented")
                }

                override suspend fun updateLastLogin(userId: String) {
                    TODO("Not yet implemented")
                }

                override suspend fun getUserById(userId: String): User? {
                    TODO("Not yet implemented")
                }

                override suspend fun authenticate(
                    username: String,
                    passwordRaw: String
                ): Result<User> {
                    TODO("Not yet implemented")
                }

                override suspend fun addUser(user: User): Result<User> {
                    TODO("Not yet implemented")
                }

                override suspend fun updateUser(user: User): Result<Unit> {
                    TODO("Not yet implemented")
                }
            }
        }
    }

    @Provides
    @Singleton
    fun provideMigrationRepository(
        @ApplicationContext context: Context,
        preferenceManager: PreferenceManager,
        databaseManager: DatabaseManager
    ): MigrationRepository {
        return MigrationRepositoryImpl(context=context, preferenceManager=preferenceManager, databaseManager=databaseManager)
    }

    @Provides @Singleton fun provideAuthUserQueries(db: AuthDb) = db.userQueries

    @Provides
    @Singleton
    fun provideAppSetupRepository(preferenceManager: PreferenceManager): AppSetupRepository {
        return AppSetupRepositoryImpl(preferenceManager)
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
}