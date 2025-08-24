package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.domain.repository.CostBudgetRepository
import com.beaconledger.welltrack.domain.usecase.ProfileContextUseCase
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class CostBudgetUseCase @Inject constructor(
    private val costBudgetRepository: CostBudgetRepository,
    private val profileContextUseCase: ProfileContextUseCase
) {

    // Meal Cost Management
    suspend fun calculateAndSaveMealCost(
        userId: String,
        mealId: String,
        recipe: Recipe,
        servings: Int,
        mealType: MealType,
        date: LocalDate = LocalDate.now()
    ): Result<MealCost> {
        return try {
            val mealCostResult = costBudgetRepository.calculateMealCost(recipe, servings)
            if (mealCostResult.isFailure) {
                return mealCostResult
            }

            val mealCost = mealCostResult.getOrNull()!!.copy(
                userId = userId,
                mealId = mealId,
                date = date,
                mealType = mealType
            )

            val saveResult = costBudgetRepository.saveMealCost(mealCost)
            if (saveResult.isFailure) {
                return Result.failure(saveResult.exceptionOrNull()!!)
            }

            // Update budget tracking after saving meal cost
            updateBudgetTrackingForUser(userId)

            Result.success(mealCost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMealCost(mealId: String): MealCost? {
        return costBudgetRepository.getMealCost(mealId)
    }

    fun getMealCostsForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MealCost>> {
        return costBudgetRepository.getMealCostsForPeriod(userId, startDate, endDate)
    }

    fun getMealCostsForDate(userId: String, date: LocalDate): Flow<List<MealCost>> {
        return costBudgetRepository.getMealCostsForDate(userId, date)
    }

    // Budget Settings Management
    suspend fun getBudgetSettings(userId: String): BudgetSettings? {
        return costBudgetRepository.getBudgetSettings(userId)
    }

    fun getBudgetSettingsFlow(userId: String): Flow<BudgetSettings?> {
        return costBudgetRepository.getBudgetSettingsFlow(userId)
    }

    suspend fun createBudgetSettings(
        userId: String,
        weeklyBudget: Double? = null,
        monthlyBudget: Double? = null,
        alertThreshold: Double = 0.8,
        enableAlerts: Boolean = true,
        currency: String = "USD"
    ): Result<BudgetSettings> {
        return try {
            val settings = BudgetSettings(
                id = UUID.randomUUID().toString(),
                userId = userId,
                weeklyBudget = weeklyBudget,
                monthlyBudget = monthlyBudget,
                alertThreshold = alertThreshold,
                enableAlerts = enableAlerts,
                currency = currency,
                lastUpdated = LocalDateTime.now()
            )

            val result = costBudgetRepository.saveBudgetSettings(settings)
            if (result.isSuccess) {
                Result.success(settings)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateBudgetSettings(
        userId: String,
        weeklyBudget: Double? = null,
        monthlyBudget: Double? = null,
        alertThreshold: Double? = null,
        enableAlerts: Boolean? = null,
        currency: String? = null
    ): Result<BudgetSettings> {
        return try {
            val existingSettings = costBudgetRepository.getBudgetSettings(userId)
                ?: return Result.failure(Exception("No budget settings found for user"))

            val updatedSettings = existingSettings.copy(
                weeklyBudget = weeklyBudget ?: existingSettings.weeklyBudget,
                monthlyBudget = monthlyBudget ?: existingSettings.monthlyBudget,
                alertThreshold = alertThreshold ?: existingSettings.alertThreshold,
                enableAlerts = enableAlerts ?: existingSettings.enableAlerts,
                currency = currency ?: existingSettings.currency,
                lastUpdated = LocalDateTime.now()
            )

            val result = costBudgetRepository.updateBudgetSettings(updatedSettings)
            if (result.isSuccess) {
                Result.success(updatedSettings)
            } else {
                Result.failure(result.exceptionOrNull()!!)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Budget Tracking
    suspend fun getCurrentBudgetStatus(userId: String): BudgetStatus {
        return try {
            val settings = costBudgetRepository.getBudgetSettings(userId)
            val weeklyTracking = costBudgetRepository.getCurrentBudgetTracking(userId, BudgetPeriod.WEEKLY)
            val monthlyTracking = costBudgetRepository.getCurrentBudgetTracking(userId, BudgetPeriod.MONTHLY)

            BudgetStatus(
                hasSettings = settings != null,
                weeklyBudget = settings?.weeklyBudget,
                monthlyBudget = settings?.monthlyBudget,
                weeklyTracking = weeklyTracking,
                monthlyTracking = monthlyTracking,
                currency = settings?.currency ?: "USD"
            )
        } catch (e: Exception) {
            BudgetStatus(
                hasSettings = false,
                weeklyBudget = null,
                monthlyBudget = null,
                weeklyTracking = null,
                monthlyTracking = null,
                currency = "USD"
            )
        }
    }

    private suspend fun updateBudgetTrackingForUser(userId: String) {
        try {
            costBudgetRepository.updateBudgetTracking(userId, BudgetPeriod.WEEKLY)
            costBudgetRepository.updateBudgetTracking(userId, BudgetPeriod.MONTHLY)
        } catch (e: Exception) {
            // Log error but don't fail the main operation
        }
    }

    fun getBudgetTrackingHistory(userId: String, period: BudgetPeriod): Flow<List<BudgetTracking>> {
        return costBudgetRepository.getBudgetTrackingHistory(userId, period)
    }

    // Cost Analysis
    suspend fun getCostAnalysis(userId: String, days: Int = 30): CostAnalysis {
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(days.toLong())

        return try {
            val totalSpent = costBudgetRepository.getTotalSpentForPeriod(userId, startDate, endDate)
            val averageCostPerServing = costBudgetRepository.getAverageCostPerServing(userId, startDate, endDate)
            val cheapestRecipes = costBudgetRepository.getCheapestRecipes(userId, startDate, endDate)
            val expensiveRecipes = costBudgetRepository.getMostExpensiveRecipes(userId, startDate, endDate)
            val costByMealType = costBudgetRepository.getCostByMealType(userId, startDate, endDate)
            val dailySpending = costBudgetRepository.getDailySpendingTrend(userId, startDate, endDate)

            CostAnalysis(
                totalSpent = totalSpent,
                averageCostPerServing = averageCostPerServing,
                averageDailySpending = totalSpent / days,
                cheapestRecipes = cheapestRecipes.take(5),
                mostExpensiveRecipes = expensiveRecipes.take(5),
                costByMealType = costByMealType,
                dailySpendingTrend = dailySpending,
                periodDays = days
            )
        } catch (e: Exception) {
            CostAnalysis(
                totalSpent = 0.0,
                averageCostPerServing = 0.0,
                averageDailySpending = 0.0,
                cheapestRecipes = emptyList(),
                mostExpensiveRecipes = emptyList(),
                costByMealType = emptyList(),
                dailySpendingTrend = emptyList(),
                periodDays = days
            )
        }
    }

    // Recipe Cost Comparison
    suspend fun compareRecipeCosts(recipeIds: List<String>): List<RecipeCostComparison> {
        return costBudgetRepository.compareRecipeCosts(recipeIds)
    }

    // Shopping List Cost Estimation
    suspend fun estimateShoppingListCost(shoppingList: List<ShoppingListItem>): Result<ShoppingListCostEstimate> {
        return try {
            val costResult = costBudgetRepository.estimateShoppingListCost(shoppingList)
            if (costResult.isFailure) {
                return Result.failure(costResult.exceptionOrNull()!!)
            }

            val totalCost = costResult.getOrNull()!!
            val itemBreakdown = mutableListOf<ShoppingListItemCost>()

            for (item in shoppingList) {
                val price = costBudgetRepository.getIngredientPrice(item.name)
                val itemCost = if (price != null) {
                    item.quantity * price.price
                } else {
                    item.quantity * 5.99 // Default estimated price
                }

                itemBreakdown.add(
                    ShoppingListItemCost(
                        itemName = item.name,
                        quantity = item.quantity,
                        unit = item.unit,
                        unitPrice = price?.price ?: 5.99,
                        totalCost = itemCost,
                        isEstimated = price?.isEstimated ?: true
                    )
                )
            }

            val estimate = ShoppingListCostEstimate(
                totalCost = totalCost,
                itemBreakdown = itemBreakdown,
                hasEstimatedPrices = itemBreakdown.any { it.isEstimated }
            )

            Result.success(estimate)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cost Optimization
    suspend fun getCostOptimizationSuggestions(userId: String): List<CostOptimizationSuggestion> {
        return costBudgetRepository.generateCostOptimizationSuggestions(userId)
    }

    // Budget Alerts
    suspend fun checkBudgetAlerts(userId: String): List<BudgetAlert> {
        return costBudgetRepository.checkBudgetAlerts(userId)
    }

    // Ingredient Price Management
    suspend fun updateIngredientPrice(
        ingredientName: String,
        price: Double,
        unit: String,
        storeId: String? = null,
        storeName: String? = null
    ): Result<Unit> {
        return try {
            val ingredientPrice = IngredientPrice(
                id = UUID.randomUUID().toString(),
                ingredientName = ingredientName.lowercase(),
                price = price,
                unit = unit,
                storeId = storeId,
                storeName = storeName,
                lastUpdated = LocalDateTime.now(),
                isEstimated = false
            )

            costBudgetRepository.saveIngredientPrice(ingredientPrice)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllIngredientPrices(): Flow<List<IngredientPrice>> {
        return costBudgetRepository.getAllIngredientPrices()
    }
}

// Note: Data classes moved to data.model.CostBudget to avoid duplicates

// Extension function to convert Recipe to have ingredients list
fun Recipe.getIngredients(): List<Ingredient> {
    // This would typically be fetched from RecipeIngredient table
    // For now, return empty list - this should be handled by the repository
    return emptyList()
}