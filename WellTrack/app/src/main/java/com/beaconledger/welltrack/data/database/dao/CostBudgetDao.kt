package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface CostBudgetDao {

    // Ingredient Prices
    @Query("SELECT * FROM ingredient_prices WHERE ingredientName = :ingredientName ORDER BY lastUpdated DESC LIMIT 1")
    suspend fun getLatestPriceForIngredient(ingredientName: String): IngredientPrice?

    @Query("SELECT * FROM ingredient_prices WHERE ingredientName = :ingredientName AND storeId = :storeId")
    suspend fun getPriceForIngredientAtStore(ingredientName: String, storeId: String): IngredientPrice?

    @Query("SELECT * FROM ingredient_prices ORDER BY ingredientName ASC")
    fun getAllIngredientPrices(): Flow<List<IngredientPrice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientPrice(price: IngredientPrice)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientPrices(prices: List<IngredientPrice>)

    @Update
    suspend fun updateIngredientPrice(price: IngredientPrice)

    @Delete
    suspend fun deleteIngredientPrice(price: IngredientPrice)

    // Meal Costs
    @Query("SELECT * FROM meal_costs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getMealCostsForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MealCost>>

    @Query("SELECT * FROM meal_costs WHERE userId = :userId AND date = :date ORDER BY mealType ASC")
    fun getMealCostsForDate(userId: String, date: LocalDate): Flow<List<MealCost>>

    @Query("SELECT * FROM meal_costs WHERE mealId = :mealId")
    suspend fun getMealCostByMealId(mealId: String): MealCost?

    @Query("SELECT AVG(costPerServing) FROM meal_costs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getAverageCostPerServing(userId: String, startDate: LocalDate, endDate: LocalDate): Double?

    @Query("SELECT SUM(totalCost) FROM meal_costs WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalSpentForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealCost(mealCost: MealCost)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealCosts(mealCosts: List<MealCost>)

    @Update
    suspend fun updateMealCost(mealCost: MealCost)

    @Delete
    suspend fun deleteMealCost(mealCost: MealCost)

    // Budget Settings
    @Query("SELECT * FROM budget_settings WHERE userId = :userId")
    suspend fun getBudgetSettings(userId: String): BudgetSettings?

    @Query("SELECT * FROM budget_settings WHERE userId = :userId")
    fun getBudgetSettingsFlow(userId: String): Flow<BudgetSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetSettings(settings: BudgetSettings)

    @Update
    suspend fun updateBudgetSettings(settings: BudgetSettings)

    @Delete
    suspend fun deleteBudgetSettings(settings: BudgetSettings)

    // Budget Tracking
    @Query("SELECT * FROM budget_tracking WHERE userId = :userId AND period = :period AND periodStart = :periodStart")
    suspend fun getBudgetTracking(userId: String, period: BudgetPeriod, periodStart: LocalDate): BudgetTracking?

    @Query("SELECT * FROM budget_tracking WHERE userId = :userId AND period = :period ORDER BY periodStart DESC")
    fun getBudgetTrackingHistory(userId: String, period: BudgetPeriod): Flow<List<BudgetTracking>>

    @Query("SELECT * FROM budget_tracking WHERE userId = :userId AND period = :period ORDER BY periodStart DESC LIMIT 1")
    suspend fun getCurrentBudgetTracking(userId: String, period: BudgetPeriod): BudgetTracking?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudgetTracking(tracking: BudgetTracking)

    @Update
    suspend fun updateBudgetTracking(tracking: BudgetTracking)

    @Delete
    suspend fun deleteBudgetTracking(tracking: BudgetTracking)

    // Cost Analysis Queries
    @Query("""
        SELECT recipeName, AVG(costPerServing) as avgCost, COUNT(*) as frequency
        FROM meal_costs 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY recipeName
        ORDER BY avgCost ASC
    """)
    suspend fun getCheapestRecipes(userId: String, startDate: LocalDate, endDate: LocalDate): List<RecipeCostSummary>

    @Query("""
        SELECT recipeName, AVG(costPerServing) as avgCost, COUNT(*) as frequency
        FROM meal_costs 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY recipeName
        ORDER BY avgCost DESC
    """)
    suspend fun getMostExpensiveRecipes(userId: String, startDate: LocalDate, endDate: LocalDate): List<RecipeCostSummary>

    @Query("""
        SELECT mealType, AVG(costPerServing) as avgCost, COUNT(*) as frequency
        FROM meal_costs 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY mealType
        ORDER BY avgCost DESC
    """)
    suspend fun getCostByMealType(userId: String, startDate: LocalDate, endDate: LocalDate): List<MealTypeCostSummary>

    @Query("""
        SELECT date, SUM(totalCost) as dailyTotal
        FROM meal_costs 
        WHERE userId = :userId AND date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    suspend fun getDailySpendingTrend(userId: String, startDate: LocalDate, endDate: LocalDate): List<DailySpending>

    @Query("DELETE FROM budget_settings WHERE userId = :userId")
    suspend fun deleteAllCostBudgetsForUser(userId: String)
}

