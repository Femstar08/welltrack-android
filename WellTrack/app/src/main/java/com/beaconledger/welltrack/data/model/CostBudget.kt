package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.time.LocalDateTime
import java.time.LocalDate

@Entity(
    tableName = "ingredient_prices",
    indices = [Index(value = ["ingredientName", "storeId"], unique = true)]
)
data class IngredientPrice(
    @PrimaryKey val id: String,
    val ingredientName: String,
    val price: Double, // Price per unit
    val unit: String, // e.g., "kg", "lb", "piece"
    val storeId: String? = null, // Optional store identifier
    val storeName: String? = null,
    val lastUpdated: LocalDateTime,
    val isEstimated: Boolean = false // True if price is estimated/average
)

@Entity(
    tableName = "meal_costs",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "date"])]
)
data class MealCost(
    @PrimaryKey val id: String,
    val userId: String,
    val mealId: String,
    val recipeName: String,
    val totalCost: Double,
    val costPerServing: Double,
    val servings: Int,
    val date: LocalDate,
    val mealType: MealType,
    val ingredientCosts: List<IngredientCostBreakdown>
)

data class IngredientCostBreakdown(
    val ingredientName: String,
    val quantity: Double,
    val unit: String,
    val unitPrice: Double,
    val totalCost: Double,
    val isEstimated: Boolean = false
)

@Entity(
    tableName = "budget_settings",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId"], unique = true)]
)
data class BudgetSettings(
    @PrimaryKey val id: String,
    val userId: String,
    val weeklyBudget: Double? = null,
    val monthlyBudget: Double? = null,
    val alertThreshold: Double = 0.8, // Alert when 80% of budget is reached
    val enableAlerts: Boolean = true,
    val currency: String = "USD",
    val lastUpdated: LocalDateTime
)

@Entity(
    tableName = "budget_tracking",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "period", "periodStart"])]
)
data class BudgetTracking(
    @PrimaryKey val id: String,
    val userId: String,
    val period: BudgetPeriod,
    val periodStart: LocalDate,
    val periodEnd: LocalDate,
    val budgetLimit: Double,
    val totalSpent: Double,
    val remainingBudget: Double,
    val mealCount: Int,
    val averageCostPerMeal: Double,
    val lastUpdated: LocalDateTime
)

enum class BudgetPeriod {
    WEEKLY,
    MONTHLY
}

data class CostOptimizationSuggestion(
    val type: OptimizationType,
    val title: String,
    val description: String,
    val potentialSavings: Double,
    val actionItems: List<String>,
    val priority: SuggestionPriority
)

enum class OptimizationType {
    INGREDIENT_SUBSTITUTION,
    BULK_BUYING,
    SEASONAL_INGREDIENTS,
    MEAL_PREP_OPTIMIZATION,
    STORE_COMPARISON,
    RECIPE_ALTERNATIVES
}

enum class SuggestionPriority {
    HIGH,
    MEDIUM,
    LOW
}

data class BudgetAlert(
    val id: String,
    val userId: String,
    val type: AlertType,
    val title: String,
    val message: String,
    val currentSpending: Double,
    val budgetLimit: Double,
    val percentageUsed: Double,
    val timestamp: LocalDateTime,
    val isRead: Boolean = false
)

enum class AlertType {
    BUDGET_WARNING, // 80% threshold
    BUDGET_EXCEEDED,
    WEEKLY_SUMMARY,
    MONTHLY_SUMMARY,
    COST_SPIKE_DETECTED
}

// Additional data classes for cost analysis
data class RecipeCostSummary(
    val recipeName: String,
    val avgCost: Double,
    val frequency: Int
)

data class MealTypeCostSummary(
    val mealType: MealType,
    val avgCost: Double,
    val frequency: Int
)

data class DailySpending(
    val date: java.time.LocalDate,
    val dailyTotal: Double
)

data class RecipeCostComparison(
    val recipeId: String,
    val recipeName: String,
    val costPerServing: Double,
    val totalCost: Double,
    val servings: Int,
    val isEstimated: Boolean
)