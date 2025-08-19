package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.ShoppingListDao
import com.beaconledger.welltrack.data.repository.ShoppingListRepositoryImpl
import com.beaconledger.welltrack.domain.repository.ShoppingListRepository
import com.beaconledger.welltrack.domain.usecase.ShoppingListUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ShoppingListModule {
    
    @Provides
    @Singleton
    fun provideShoppingListDao(database: WellTrackDatabase): ShoppingListDao {
        return database.shoppingListDao()
    }
    
    @Provides
    @Singleton
    fun provideShoppingListRepository(
        shoppingListDao: ShoppingListDao,
        mealPlanDao: com.beaconledger.welltrack.data.database.dao.MealPlanDao,
        recipeIngredientDao: com.beaconledger.welltrack.data.database.dao.RecipeIngredientDao,
        recipeDao: com.beaconledger.welltrack.data.database.dao.RecipeDao
    ): ShoppingListRepository {
        return ShoppingListRepositoryImpl(
            shoppingListDao = shoppingListDao,
            mealPlanDao = mealPlanDao,
            recipeIngredientDao = recipeIngredientDao,
            recipeDao = recipeDao
        )
    }
    
    @Provides
    @Singleton
    fun provideShoppingListUseCase(
        shoppingListRepository: ShoppingListRepository
    ): ShoppingListUseCase {
        return ShoppingListUseCase(shoppingListRepository)
    }
}