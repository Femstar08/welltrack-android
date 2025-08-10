package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "meal_plans")
data class MealPlan(
    @PrimaryKey
    val id: String,
    val userId: String,
    val weekStartDate: String, // ISO 8601 date string (YYYY-MM-DD)
    val weekEndDate: String,   // ISO 8601 date string (YYYY-MM-DD)
    val isActive: Boolean = true,
    val generatedAt: String = LocalDateTime.now().toString(),
    val lastModified: String = LocalDateTime.now().toString(),
    val preferences: String = "{}", // JSON string of MealPlanPreferences
    val notes: String? = null
)

@Entity(tableName = "planned_meals")
data class PlannedMeal(
    @PrimaryKey
    val id: String,
    val mealPlanId: String,
    val userId: String,
    val date: String, // ISO 8601 date string (YYYY-MM-DD)
    val mealType: MealType,
    val recipeId: String? = null,
    val customMealName: String? = null,
    val servings: Int = 1,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val status: PlannedMealStatus = PlannedMealStatus.PLANNED,
    val notes: String? = null,
    val nutritionInfo: String? = null, // JSON string of calculated nutrition
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "planned_supplements")
data class PlannedSupplement(
    @PrimaryKey
    val id: String,
    val mealPlanId: String,
    val userId: String,
    val date: String, // ISO 8601 date string (YYYY-MM-DD)
    val supplementName: String,
    val dosage: String,
    val timing: SupplementTiming = SupplementTiming.WITH_MEAL,
    val isCompleted: Boolean = false,
    val completedAt: String? = null,
    val notes: String? = null,
    val createdAt: String = LocalDateTime.now().toString()
)

data class MealPlanPreferences(
    val targetCalories: Int? = null,
    val targetProtein: Int? = null,
    val targetCarbs: Int? = null,
    val targetFat: Int? = null,
    val preferredIngredients: List<String> = emptyList(),
    val avoidedIngredients: List<String> = emptyList(),
    val dietaryRestrictions: List<String> = emptyList(),
    val mealPrepDays: List<String> = emptyList(), // Days of week for meal prep
    val cookingTimePreference: CookingTimePreference = CookingTimePreference.MODERATE,
    val varietyLevel: VarietyLevel = VarietyLevel.MODERATE
)

data class WeeklyMealPlan(
    val mealPlan: MealPlan,
    val plannedMeals: List<PlannedMeal>,
    val plannedSupplements: List<PlannedSupplement>,
    val recipes: List<Recipe> = emptyList()
)

data class DailyMealPlan(
    val date: LocalDate,
    val breakfast: PlannedMeal? = null,
    val lunch: PlannedMeal? = null,
    val dinner: PlannedMeal? = null,
    val snacks: List<PlannedMeal> = emptyList(),
    val supplements: List<PlannedSupplement> = emptyList(),
    val totalNutrition: NutritionInfo? = null
)

enum class PlannedMealStatus {
    PLANNED,
    IN_PROGRESS,
    COMPLETED,
    SKIPPED,
    SUBSTITUTED
}

enum class SupplementTiming {
    MORNING,
    WITH_BREAKFAST,
    WITH_MEAL,
    WITH_LUNCH,
    WITH_DINNER,
    EVENING,
    BEFORE_BED
}

enum class CookingTimePreference {
    QUICK,      // Under 30 minutes
    MODERATE,   // 30-60 minutes
    EXTENDED    // Over 60 minutes
}

enum class VarietyLevel {
    LOW,        // Repeat meals frequently
    MODERATE,   // Some repetition
    HIGH        // Maximum variety
}

// Request/Response models for meal plan generation
data class MealPlanGenerationRequest(
    val userId: String,
    val weekStartDate: LocalDate,
    val preferences: MealPlanPreferences,
    val existingMeals: List<PlannedMeal> = emptyList() // For partial regeneration
)

data class MealPlanGenerationResult(
    val success: Boolean,
    val mealPlan: WeeklyMealPlan? = null,
    val error: String? = null,
    val warnings: List<String> = emptyList()
)