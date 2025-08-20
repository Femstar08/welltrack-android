package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.analysis.DietaryAnalyzer
import com.beaconledger.welltrack.data.analysis.DietaryFilteringService
import com.beaconledger.welltrack.domain.usecase.DietaryFilteringUseCase
import com.beaconledger.welltrack.domain.repository.*
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DietaryFilteringModule {
    
    @Provides
    @Singleton
    fun provideDietaryAnalyzer(gson: Gson): DietaryAnalyzer {
        return DietaryAnalyzer(gson)
    }
    
    @Provides
    @Singleton
    fun provideDietaryFilteringService(
        dietaryAnalyzer: DietaryAnalyzer
    ): DietaryFilteringService {
        return DietaryFilteringService(dietaryAnalyzer)
    }
    
    @Provides
    @Singleton
    fun provideDietaryFilteringUseCase(
        dietaryFilteringService: DietaryFilteringService,
        dietaryRestrictionsRepository: DietaryRestrictionsRepository,
        recipeRepository: RecipeRepository,
        mealPlanRepository: MealPlanRepository,
        shoppingListRepository: ShoppingListRepository
    ): DietaryFilteringUseCase {
        return DietaryFilteringUseCase(
            dietaryFilteringService,
            dietaryRestrictionsRepository,
            recipeRepository,
            mealPlanRepository,
            shoppingListRepository
        )
    }
}