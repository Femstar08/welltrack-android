package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.domain.repository.CostBudgetRepository
import com.beaconledger.welltrack.data.database.dao.CostBudgetDao
import com.beaconledger.welltrack.data.database.dao.RecipeDao
import com.beaconledger.welltrack.data.database.dao.ShoppingListDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CostBudgetRepositoryImpl @Inject constructor(
    private val costBudgetDao: CostBudgetDao,
    private val recipeDao: RecipeDao,
    private val recipeIngredientDao: RecipeIngredientDao,
    private val shoppingListDao: ShoppingListDao
) : CostBudgetRepository {

    // Default prices for common ingredients (fallback when no price data available)
    private val defaultIngredientPrices = mapOf(
        "chicken breast" to 8.99, // per kg
        "ground beef" to 7.99,
        "salmon" to 15.99,
        "rice" to 2.49,
        "pasta" to 1.99,
        "bread" to 2.99,
        "milk" to 3.49,
        "eggs" to 4.99, // per dozen
        "cheese" to 12.99,
        "tomatoes" to 4.99,
        "onions" to 2.99,
        "potatoes" to 3.99,
        "carrots" to 2.49,
        "broccoli" to 4.99,
        "spinach" to 3.99,
        "olive oil" to 8.99,
        "butter" to 5.99
    )

    override suspend fun getIngredientPrice(ingredientName: String): IngredientPrice? {
        return costBudgetDao.getLatestPriceForIngredient(ingredientName.lowercase())
    }

    override suspend fun getIngredientPriceAtStore(ingredientName: String, storeId: String): IngredientPrice? {
        return costBudgetDao.getPriceForIngredientAtStore(ingredientName.lowercase(), storeId)
    }

    override suspend fun saveIngredientPrice(price: IngredientPrice): Result<Unit> {
        return try {
            costBudgetDao.insertIngredientPrice(price)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateIngredientPrices(prices: List<IngredientPrice>): Result<Unit> {
        return try {
            costBudgetDao.insertIngredientPrices(prices)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllIngredientPrices(): Flow<List<IngredientPrice>> {
        return costBudgetDao.getAllIngredientPrices()
    }

    override suspend fun calculateMealCost(recipe: Recipe, servings: Int): Result<MealCost> {
        return try {
            val ingredientCosts = mutableListOf<IngredientCostBreakdown>()
            var totalCost = 0.0
            var hasEstimatedPrices = false

            // Get recipe ingredients from the database
            val recipeIngredients = recipeIngredientDao.getIngredientsByRecipeId(recipe.id)
            val ingredients = recipeIngredients.map { recipeIngredient ->
                Ingredient(
                    name = recipeIngredient.name,
                    quantity = recipeIngredient.quantity,
                    unit = recipeIngredient.unit,
                    category = recipeIngredient.category,
                    isOptional = recipeIngredient.isOptional,
                    notes = recipeIngredient.notes
                )
            }

            for (ingredient in ingredients) {
                val price = getIngredientPrice(ingredient.name) 
                    ?: createEstimatedPrice(ingredient.name)
                
                if (price.isEstimated) hasEstimatedPrices = true

                val ingredientTotalCost = calculateIngredientCost(ingredient, price)
                
                ingredientCosts.add(
                    IngredientCostBreakdown(
                        ingredientName = ingredient.name,
                        quantity = ingredient.quantity,
                        unit = ingredient.unit,
                        unitPrice = price.price,
                        totalCost = ingredientTotalCost,
                        isEstimated = price.isEstimated
                    )
                )
                
                totalCost += ingredientTotalCost
            }

            val mealCost = MealCost(
                id = UUID.randomUUID().toString(),
                userId = "", // Will be set by caller
                mealId = "", // Will be set by caller
                recipeName = recipe.name,
                totalCost = totalCost,
                costPerServing = totalCost / servings,
                servings = servings,
                date = LocalDate.now(),
                mealType = MealType.LUNCH, // Will be set by caller
                ingredientCosts = ingredientCosts
            )

            Result.success(mealCost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun createEstimatedPrice(ingredientName: String): IngredientPrice {
        val estimatedPrice = defaultIngredientPrices[ingredientName.lowercase()] ?: 5.99
        
        return IngredientPrice(
            id = UUID.randomUUID().toString(),
            ingredientName = ingredientName.lowercase(),
            price = estimatedPrice,
            unit = "kg",
            lastUpdated = LocalDateTime.now(),
            isEstimated = true
        )
    }

    private fun calculateIngredientCost(ingredient: Ingredient, price: IngredientPrice): Double {
        // Convert ingredient quantity to price unit if needed
        val normalizedQuantity = convertToStandardUnit(ingredient.quantity, ingredient.unit, price.unit)
        return normalizedQuantity * price.price
    }

    private fun convertToStandardUnit(quantity: Double, fromUnit: String, toUnit: String): Double {
        // Simple unit conversion - in a real app, this would be more comprehensive
        return when {
            fromUnit.lowercase() == toUnit.lowercase() -> quantity
            fromUnit.lowercase() == "g" && toUnit.lowercase() == "kg" -> quantity / 1000.0
            fromUnit.lowercase() == "kg" && toUnit.lowercase() == "g" -> quantity * 1000.0
            fromUnit.lowercase() == "ml" && toUnit.lowercase() == "l" -> quantity / 1000.0
            fromUnit.lowercase() == "l" && toUnit.lowercase() == "ml" -> quantity * 1000.0
            else -> quantity // Default: assume same unit
        }
    }

    override suspend fun saveMealCost(mealCost: MealCost): Result<Unit> {
        return try {
            costBudgetDao.insertMealCost(mealCost)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMealCost(mealId: String): MealCost? {
        return costBudgetDao.getMealCostByMealId(mealId)
    }

    override fun getMealCostsForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Flow<List<MealCost>> {
        return costBudgetDao.getMealCostsForPeriod(userId, startDate, endDate)
    }

    override fun getMealCostsForDate(userId: String, date: LocalDate): Flow<List<MealCost>> {
        return costBudgetDao.getMealCostsForDate(userId, date)
    }

    override suspend fun getBudgetSettings(userId: String): BudgetSettings? {
        return costBudgetDao.getBudgetSettings(userId)
    }

    override fun getBudgetSettingsFlow(userId: String): Flow<BudgetSettings?> {
        return costBudgetDao.getBudgetSettingsFlow(userId)
    }

    override suspend fun saveBudgetSettings(settings: BudgetSettings): Result<Unit> {
        return try {
            costBudgetDao.insertBudgetSettings(settings)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateBudgetSettings(settings: BudgetSettings): Result<Unit> {
        return try {
            costBudgetDao.updateBudgetSettings(settings)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentBudgetTracking(userId: String, period: BudgetPeriod): BudgetTracking? {
        return costBudgetDao.getCurrentBudgetTracking(userId, period)
    }

    override suspend fun updateBudgetTracking(userId: String, period: BudgetPeriod): Result<BudgetTracking> {
        return try {
            val settings = getBudgetSettings(userId) ?: return Result.failure(Exception("No budget settings found"))
            
            val (periodStart, periodEnd) = getCurrentPeriodDates(period)
            val budgetLimit = when (period) {
                BudgetPeriod.WEEKLY -> settings.weeklyBudget ?: 0.0
                BudgetPeriod.MONTHLY -> settings.monthlyBudget ?: 0.0
            }

            val totalSpent = getTotalSpentForPeriod(userId, periodStart, periodEnd)
            val mealCosts = costBudgetDao.getMealCostsForPeriod(userId, periodStart, periodEnd)
            val mealCount = mealCosts.first().size // Get current value from flow
            val averageCostPerMeal = if (mealCount > 0) totalSpent / mealCount else 0.0

            val tracking = BudgetTracking(
                id = UUID.randomUUID().toString(),
                userId = userId,
                period = period,
                periodStart = periodStart,
                periodEnd = periodEnd,
                budgetLimit = budgetLimit,
                totalSpent = totalSpent,
                remainingBudget = budgetLimit - totalSpent,
                mealCount = mealCount,
                averageCostPerMeal = averageCostPerMeal,
                lastUpdated = LocalDateTime.now()
            )

            costBudgetDao.insertBudgetTracking(tracking)
            Result.success(tracking)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCurrentPeriodDates(period: BudgetPeriod): Pair<LocalDate, LocalDate> {
        val now = LocalDate.now()
        return when (period) {
            BudgetPeriod.WEEKLY -> {
                val startOfWeek = now.minusDays(now.dayOfWeek.value - 1L)
                val endOfWeek = startOfWeek.plusDays(6)
                startOfWeek to endOfWeek
            }
            BudgetPeriod.MONTHLY -> {
                val startOfMonth = now.withDayOfMonth(1)
                val endOfMonth = startOfMonth.plusMonths(1).minusDays(1)
                startOfMonth to endOfMonth
            }
        }
    }

    override fun getBudgetTrackingHistory(userId: String, period: BudgetPeriod): Flow<List<BudgetTracking>> {
        return costBudgetDao.getBudgetTrackingHistory(userId, period)
    }

    override suspend fun getAverageCostPerServing(userId: String, startDate: LocalDate, endDate: LocalDate): Double {
        return costBudgetDao.getAverageCostPerServing(userId, startDate, endDate) ?: 0.0
    }

    override suspend fun getTotalSpentForPeriod(userId: String, startDate: LocalDate, endDate: LocalDate): Double {
        return costBudgetDao.getTotalSpentForPeriod(userId, startDate, endDate) ?: 0.0
    }

    override suspend fun getCheapestRecipes(userId: String, startDate: LocalDate, endDate: LocalDate): List<RecipeCostSummary> {
        return costBudgetDao.getCheapestRecipes(userId, startDate, endDate)
    }

    override suspend fun getMostExpensiveRecipes(userId: String, startDate: LocalDate, endDate: LocalDate): List<RecipeCostSummary> {
        return costBudgetDao.getMostExpensiveRecipes(userId, startDate, endDate)
    }

    override suspend fun getCostByMealType(userId: String, startDate: LocalDate, endDate: LocalDate): List<MealTypeCostSummary> {
        return costBudgetDao.getCostByMealType(userId, startDate, endDate)
    }

    override suspend fun getDailySpendingTrend(userId: String, startDate: LocalDate, endDate: LocalDate): List<DailySpending> {
        return costBudgetDao.getDailySpendingTrend(userId, startDate, endDate)
    }

    override suspend fun generateCostOptimizationSuggestions(userId: String): List<CostOptimizationSuggestion> {
        val suggestions = mutableListOf<CostOptimizationSuggestion>()
        val endDate = LocalDate.now()
        val startDate = endDate.minusDays(30)

        try {
            // Analyze expensive recipes
            val expensiveRecipes = getMostExpensiveRecipes(userId, startDate, endDate)
            if (expensiveRecipes.isNotEmpty()) {
                suggestions.add(
                    CostOptimizationSuggestion(
                        type = OptimizationType.RECIPE_ALTERNATIVES,
                        title = "Consider Recipe Alternatives",
                        description = "Your most expensive recipes could be replaced with more budget-friendly options",
                        potentialSavings = expensiveRecipes.take(3).sumOf { it.avgCost } * 0.3,
                        actionItems = expensiveRecipes.take(3).map { "Find alternatives to ${it.recipeName} (avg cost: $${String.format("%.2f", it.avgCost)})" },
                        priority = SuggestionPriority.HIGH
                    )
                )
            }

            // Analyze ingredient costs
            val allPricesFlow = getAllIngredientPrices()
            val allPrices = allPricesFlow.first() // Get current value from flow
            val expensiveIngredients = allPrices.filter { it.price > 10.0 && !it.isEstimated }
            if (expensiveIngredients.isNotEmpty()) {
                suggestions.add(
                    CostOptimizationSuggestion(
                        type = OptimizationType.INGREDIENT_SUBSTITUTION,
                        title = "Substitute Expensive Ingredients",
                        description = "Some ingredients in your recipes are quite expensive",
                        potentialSavings = expensiveIngredients.sumOf { it.price } * 0.2,
                        actionItems = expensiveIngredients.map { "Consider alternatives to ${it.ingredientName} ($${String.format("%.2f", it.price)}/${it.unit})" },
                        priority = SuggestionPriority.MEDIUM
                    )
                )
            }

            // Seasonal suggestions
            suggestions.add(
                CostOptimizationSuggestion(
                    type = OptimizationType.SEASONAL_INGREDIENTS,
                    title = "Use Seasonal Ingredients",
                    description = "Seasonal produce is typically more affordable and fresher",
                    potentialSavings = 15.0,
                    actionItems = listOf(
                        "Check what's in season this month",
                        "Plan meals around seasonal vegetables",
                        "Visit local farmers markets"
                    ),
                    priority = SuggestionPriority.LOW
                )
            )

        } catch (e: Exception) {
            // Return basic suggestions if analysis fails
            suggestions.add(
                CostOptimizationSuggestion(
                    type = OptimizationType.MEAL_PREP_OPTIMIZATION,
                    title = "Optimize Meal Prep",
                    description = "Batch cooking can reduce costs and save time",
                    potentialSavings = 20.0,
                    actionItems = listOf(
                        "Cook larger portions and freeze extras",
                        "Plan meals that share ingredients",
                        "Buy ingredients in bulk when possible"
                    ),
                    priority = SuggestionPriority.MEDIUM
                )
            )
        }

        return suggestions
    }

    override suspend fun checkBudgetAlerts(userId: String): List<BudgetAlert> {
        val alerts = mutableListOf<BudgetAlert>()
        val settings = getBudgetSettings(userId) ?: return alerts

        if (!settings.enableAlerts) return alerts

        try {
            // Check weekly budget
            settings.weeklyBudget?.let { weeklyBudget ->
                val weeklyTracking = getCurrentBudgetTracking(userId, BudgetPeriod.WEEKLY)
                weeklyTracking?.let { tracking ->
                    val percentageUsed = tracking.totalSpent / tracking.budgetLimit
                    
                    when {
                        percentageUsed >= 1.0 -> {
                            alerts.add(
                                BudgetAlert(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    type = AlertType.BUDGET_EXCEEDED,
                                    title = "Weekly Budget Exceeded",
                                    message = "You've spent $${String.format("%.2f", tracking.totalSpent)} of your $${String.format("%.2f", tracking.budgetLimit)} weekly budget",
                                    currentSpending = tracking.totalSpent,
                                    budgetLimit = tracking.budgetLimit,
                                    percentageUsed = percentageUsed,
                                    timestamp = LocalDateTime.now()
                                )
                            )
                        }
                        percentageUsed >= settings.alertThreshold -> {
                            alerts.add(
                                BudgetAlert(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    type = AlertType.BUDGET_WARNING,
                                    title = "Weekly Budget Warning",
                                    message = "You've used ${String.format("%.0f", percentageUsed * 100)}% of your weekly budget",
                                    currentSpending = tracking.totalSpent,
                                    budgetLimit = tracking.budgetLimit,
                                    percentageUsed = percentageUsed,
                                    timestamp = LocalDateTime.now()
                                )
                            )
                        }
                    }
                }
            }

            // Check monthly budget
            settings.monthlyBudget?.let { monthlyBudget ->
                val monthlyTracking = getCurrentBudgetTracking(userId, BudgetPeriod.MONTHLY)
                monthlyTracking?.let { tracking ->
                    val percentageUsed = tracking.totalSpent / tracking.budgetLimit
                    
                    when {
                        percentageUsed >= 1.0 -> {
                            alerts.add(
                                BudgetAlert(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    type = AlertType.BUDGET_EXCEEDED,
                                    title = "Monthly Budget Exceeded",
                                    message = "You've spent $${String.format("%.2f", tracking.totalSpent)} of your $${String.format("%.2f", tracking.budgetLimit)} monthly budget",
                                    currentSpending = tracking.totalSpent,
                                    budgetLimit = tracking.budgetLimit,
                                    percentageUsed = percentageUsed,
                                    timestamp = LocalDateTime.now()
                                )
                            )
                        }
                        percentageUsed >= settings.alertThreshold -> {
                            alerts.add(
                                BudgetAlert(
                                    id = UUID.randomUUID().toString(),
                                    userId = userId,
                                    type = AlertType.BUDGET_WARNING,
                                    title = "Monthly Budget Warning",
                                    message = "You've used ${String.format("%.0f", percentageUsed * 100)}% of your monthly budget",
                                    currentSpending = tracking.totalSpent,
                                    budgetLimit = tracking.budgetLimit,
                                    percentageUsed = percentageUsed,
                                    timestamp = LocalDateTime.now()
                                )
                            )
                        }
                    }
                }
            }

        } catch (e: Exception) {
            // Log error but don't fail
        }

        return alerts
    }

    override suspend fun estimateShoppingListCost(shoppingList: List<ShoppingListItem>): Result<Double> {
        return try {
            var totalCost = 0.0
            
            for (item in shoppingList) {
                val price = getIngredientPrice(item.name) 
                    ?: createEstimatedPrice(item.name)
                
                val itemCost = item.quantity * price.price
                totalCost += itemCost
            }
            
            Result.success(totalCost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun compareRecipeCosts(recipeIds: List<String>): List<RecipeCostComparison> {
        val comparisons = mutableListOf<RecipeCostComparison>()
        
        for (recipeId in recipeIds) {
            try {
                val recipe = recipeDao.getRecipeById(recipeId) ?: continue
                val mealCostResult = calculateMealCost(recipe, recipe.servings)
                
                if (mealCostResult.isSuccess) {
                    val mealCost = mealCostResult.getOrNull()!!
                    comparisons.add(
                        RecipeCostComparison(
                            recipeId = recipeId,
                            recipeName = recipe.name,
                            costPerServing = mealCost.costPerServing,
                            totalCost = mealCost.totalCost,
                            servings = recipe.servings,
                            isEstimated = mealCost.ingredientCosts.any { it.isEstimated }
                        )
                    )
                }
            } catch (e: Exception) {
                // Skip failed recipes
                continue
            }
        }
        
        return comparisons.sortedBy { it.costPerServing }
    }
}