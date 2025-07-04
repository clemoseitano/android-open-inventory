package com.coptimize.openinventory.data

import android.content.Context
import com.coptimize.openinventory.data.dao.ProductDao
import com.coptimize.openinventory.data.repository.ProductRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // These dependencies will live as long as the app does
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideProductDao(appDatabase: AppDatabase): ProductDao {
        return appDatabase.productDao()
    }

    // We can provide repositories the same way, but Hilt can also do it automatically
    // if the repository has an @Inject constructor, which is the preferred way.
    // So, we don't need a @Provides function for ProductRepository.

    // Add @Provides functions for other DAOs here (SaleDao, UserDao, etc.)
}