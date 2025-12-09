package com.coptimize.openinventory.di

import android.content.Context
import com.coptimize.openinventory.data.DatabaseManager
import com.coptimize.openinventory.data.PreferenceManager
import com.coptimize.openinventory.data.api.AiInferenceService
import com.coptimize.openinventory.data.repository.MigrationRepository
import com.coptimize.openinventory.data.repository.MigrationRepositoryImpl
import com.coptimize.openinventory.data.repository.ProductAnalysisRepository
import com.coptimize.openinventory.data.repository.ProductAnalysisRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://192.168.0.4:8000/api/") // Replace with your actual server URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAiInferenceService(retrofit: Retrofit): AiInferenceService {
        return retrofit.create(AiInferenceService::class.java)
    }

    @Provides
    @Singleton
    fun provideProductAnalysisRepository(
        @ApplicationContext context: Context,
        aiInferenceService: AiInferenceService,
    ): ProductAnalysisRepository {
        return ProductAnalysisRepositoryImpl(
            context = context,
            apiService = aiInferenceService
        )
    }
}