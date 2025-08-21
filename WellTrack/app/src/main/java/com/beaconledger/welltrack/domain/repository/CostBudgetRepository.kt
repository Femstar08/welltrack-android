package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface CostBudgetRepository {

    // Ingredient Price Management
    suspend fun getIngredientPrice(ingredientName: String): IngredientPrice?
    suspend fun getIngredientPriceAtStore(ingredientName: String, storeId: String): IngredientPrice?
    suspend fun saveIngredientPrice(price: IngredientPrice): Result<Unit>
    suspend fun updateIngredientPrices(prices: List<IngredientPrice>): Result<Unit>
    fun getAllIngredientPrices(): Flow<List<IngredientPrice>>

    // Meal Cost Calculation and Storage
    suspend fun calculateMealCost(recipe: Recipe, servings: Int): Result<MealCost>
    suspend fun saveMealCost(mealCost: MealCost): Result<Unit>
    suspend fun getMealCost(mealId: String): MealCost?
    fun getMealCostsForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MealCost>>
    fun getMealCostsForDate(userId: String, date: LocalDate): Flow<List<MealCost>>

    // Budget Management
    suspend fun getBudgetSettings(userId: String): BudgetSettings?
    fun getBudgetSettingsFlow(userId: String): Flow<BudgetSettings?>
    suspend fun saveBudgetSettings(settings: BudgetSettings): Result<Unit>
    suspend fun updateBudgetSettings(settings: BudgetSettings): Result<Unit>

    // Budget Tracking
    suspend fun getCurrentBudgetTracking(userId: String, period: BudgetPeriod): BudgetTracking?
    suspend fun updateBudgetTracking(userId: String, period: BudgetPeriod): Result<BudgetTracking>
    fun getBudgetTrackingHistory(userId: String, period: BudgetPeriod): Flow<List<BudgetTracking>>

    // Cost Analysis
    suspend fun getAverageCostPerServing(userId: String, startDate: LocalDate, endDate: LocalDate): Double
    suspend fun getTotalSpentForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Double
    suspend fun getCheapestRecipes(userId: String, startDate: LocalDate, endDate: LocalDate): List<RecipeCostSummary>
    suspend fun getMostExpensiveRecipes(userId: String, startDate: LocalDate, endDate: LocalDate): List<RecipeCostSummary>
    suspend fun getCostByMealType(userId: String, startDate: LocalDate, endDate: LocalDate): List<MealTypeCostSummary>
    suspend fun getDailySpendingTrend(userId: String, startDate: LocalDate, endDate: LocalDate): List<DailySpending>

    // Budget Optimization
    suspend fun generateCostOptimizationSuggestions(userId: String): List<CostOptimizationSuggestion>
    suspend fun checkBudgetAlerts(userId: String): List<BudgetAlert>

    // Shopping List Cost Estimation
    suspend fun estimateShoppingListCost(shoppingList: List<ShoppingListItem>): Result<Double>
    suspend fun compareRecipeCosts(recipeIds: List<String>): List<RecipeCostComparison>
}

