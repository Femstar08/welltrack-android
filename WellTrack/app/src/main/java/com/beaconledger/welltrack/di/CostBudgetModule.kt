package com.beaconledger.welltrack.di

import com.beaconledger.welltrack.data.database.dao.CostBudgetDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.dao.RecipeIngredientDao
import com.beaconledger.welltrack.data.database.dao.ShoppingListDao
import com.beaconledger.welltrack.data.repository.CostBudgetRepositoryImpl
import com.beaconledger.welltrack.domain.repository.CostBudgetRepository
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CostBudgetModule {

    @Provides
    @Singleton
    fun provideCostBudgetDao(database: WellTrackDatabase): CostBudgetDao {
        return database.costBudgetDao()
    }

    @Provides
    @Singleton
    fun provideCostBudgetRepository(
        costBudgetDao: CostBudgetDao,
        recipeDao: RecipeDao,
        recipeIngredientDao: RecipeIngredientDao,
        shoppingListDao: ShoppingListDao
    ): CostBudgetRepository {
        return CostBudgetRepositoryImpl(
            costBudgetDao = costBudgetDao,
            recipeDao = recipeDao,
            recipeIngredientDao = recipeIngredientDao,
            shoppingListDao = shoppingListDao
        )
    }
}