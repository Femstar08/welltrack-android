package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.dao.DietaryRestrictionsDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.dao.MealDao
import com.beaconledger.welltrack.data.repository.DietaryRestrictionsRepositoryImpl
import com.beaconledger.welltrack.domain.repository.DietaryRestrictionsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DietaryRestrictionsModule {
    
    @Provides
    @Singleton
    fun provideDietaryRestrictionsRepository(
        dietaryRestrictionsDao: DietaryRestrictionsDao,
        recipeDao: RecipeDao,
        mealDao: MealDao
    ): DietaryRestrictionsRepository {
        return DietaryRestrictionsRepositoryImpl(
            dietaryRestrictionsDao = dietaryRestrictionsDao,
            recipeDao = recipeDao,
            mealDao = mealDao
        )
    }
}