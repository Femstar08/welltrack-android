package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.cooking.CookingGuidanceService
import com.beaconledger.welltrack.data.cooking.CookingNotificationManager
import com.beaconledger.welltrack.data.database.dao.CookingSessionDao
import com.beaconledger.welltrack.data.repository.CookingRepositoryImpl
import com.beaconledger.welltrack.domain.repository.CookingRepository
import com.beaconledger.welltrack.domain.repository.RecipeRepository
import com.beaconledger.welltrack.domain.usecase.CookingGuidanceUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CookingModule {
    
    @Provides
    @Singleton
    fun provideCookingGuidanceService(): CookingGuidanceService {
        return CookingGuidanceService()
    }
    
    @Provides
    @Singleton
    fun provideCookingRepository(
        cookingSessionDao: CookingSessionDao,
        recipeRepository: RecipeRepository,
        cookingGuidanceService: CookingGuidanceService
    ): CookingRepository {
        return CookingRepositoryImpl(
            cookingSessionDao = cookingSessionDao,
            recipeRepository = recipeRepository,
            cookingGuidanceService = cookingGuidanceService
        )
    }
    
    @Provides
    @Singleton
    fun provideCookingGuidanceUseCase(
        cookingRepository: CookingRepository
    ): CookingGuidanceUseCase {
        return CookingGuidanceUseCase(cookingRepository)
    }
}