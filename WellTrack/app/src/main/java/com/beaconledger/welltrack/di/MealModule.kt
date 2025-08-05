package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.dao.MealDao
import com.beaconledger.welltrack.data.meal.MealRecognitionService
import com.beaconledger.welltrack.data.meal.MealScoringService
import com.beaconledger.welltrack.data.repository.MealRepositoryImpl
import com.beaconledger.welltrack.domain.repository.MealRepository
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MealModule {
    
    @Provides
    @Singleton
    fun provideMealRepository(
        mealDao: MealDao
    ): MealRepository {
        return MealRepositoryImpl(mealDao)
    }
    
    @Provides
    @Singleton
    fun provideMealRecognitionService(
        nutritionCalculator: com.beaconledger.welltrack.data.nutrition.NutritionCalculator
    ): MealRecognitionService {
        return MealRecognitionService(nutritionCalculator)
    }
    
    @Provides
    @Singleton
    fun provideMealScoringService(): MealScoringService {
        return MealScoringService()
    }
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }
}