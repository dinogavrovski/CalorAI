package com.calorai.app.di

import com.calorai.app.data.local.TokenDataStore
import com.calorai.app.data.remote.ApiService
import com.calorai.app.data.repository.AuthRepository
import com.calorai.app.data.repository.MealRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        tokenDataStore: TokenDataStore
    ): AuthRepository = AuthRepository(apiService, tokenDataStore)

    @Provides
    @Singleton
    fun provideMealRepository(
        apiService: ApiService
    ): MealRepository = MealRepository(apiService)
}
