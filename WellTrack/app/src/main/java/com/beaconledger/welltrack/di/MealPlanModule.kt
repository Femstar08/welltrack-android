package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.dao.MealPlanDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.repository.MealPlanRepositoryImpl
import com.beaconledger.welltrack.domain.repository.MealPlanRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MealPlanModule {

    @Provides
    @Singleton
    fun provideMealPlanRepository(
        mealPlanDao: MealPlanDao,
        recipeDao: RecipeDao
    ): MealPlanRepository {
        return MealPlanRepositoryImpl(mealPlanDao, recipeDao)
    }
}