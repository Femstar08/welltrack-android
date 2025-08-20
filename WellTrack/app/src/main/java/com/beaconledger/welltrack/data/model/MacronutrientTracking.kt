package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "macronutrient_targets")
data class MacronutrientTarget(
    @PrimaryKey val id: String,
    val userId: String,
    val date: LocalDate,
    val caloriesTarget: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val waterMl: Int,
    val customNutrients: Map<String, Double> = emptyMap(), // JSON stored as string
    val isActive: Boolean = true
)

@Entity(tableName = "macronutrient_intake")
data class MacronutrientIntake(
    @PrimaryKey val id: String,
    val userId: String,
    val date: LocalDate,
    val mealId: String?,
    val supplementId: String?,
    val calories: Int,
    val proteinGrams: Double,
    val carbsGrams: Double,
    val fatGrams: Double,
    val fiberGrams: Double,
    val waterMl: Int,
    val customNutrients: Map<String, Double> = emptyMap(),
    val source: NutrientSource,
    val timestamp: java.time.LocalDateTime
)

enum class NutrientSource {
    MEAL,
    SUPPLEMENT,
    MANUAL_ENTRY,
    RECIPE
}

data class MacronutrientSummary(
    val userId: String,
    val date: LocalDate,
    val targets: MacronutrientTarget?,
    val totalCalories: Int,
    val totalProtein: Double,
    val totalCarbs: Double,
    val totalFat: Double,
    val totalFiber: Double,
    val totalWater: Int,
    val customNutrientTotals: Map<String, Double>,
    val caloriesProgress: Float,
    val proteinProgress: Float,
    val carbsProgress: Float,
    val fatProgress: Float,
    val fiberProgress: Float,
    val waterProgress: Float,
    val customNutrientProgress: Map<String, Float>
)

data class NutrientGoal(
    val name: String,
    val targetValue: Double,
    val unit: String,
    val priority: NutrientPriority,
    val category: NutrientCategory
)

enum class NutrientPriority {
    CORE, // Carbs, Proteins, Fats, Fiber, Water
    IMPORTANT, // Vitamins, Minerals
    OPTIONAL // Additional tracking
}

enum class NutrientCategory {
    MACRONUTRIENT,
    VITAMIN,
    MINERAL,
    AMINO_ACID,
    FATTY_ACID,
    OTHER
}

data class ProteinTarget(
    val userId: String,
    val bodyWeightKg: Double,
    val activityLevel: ActivityLevel,
    val goal: FitnessGoal,
    val recommendedGramsPerKg: Double,
    val totalTargetGrams: Double
)

enum class ActivityLevel {
    SEDENTARY, // 1.2g/kg
    LIGHTLY_ACTIVE, // 1.4g/kg
    MODERATELY_ACTIVE, // 1.6g/kg
    VERY_ACTIVE, // 1.8g/kg
    EXTREMELY_ACTIVE // 2.0g/kg
}

data class FiberTarget(
    val userId: String,
    val age: Int,
    val gender: Gender,
    val recommendedGrams: Double // 25-30g/day for optimal health
)

enum class Gender {
    MALE, FEMALE, OTHER
}

@Entity(tableName = "custom_nutrients")
data class CustomNutrient(
    @PrimaryKey val id: String,
    val userId: String,
    val name: String,
    val unit: String,
    val targetValue: Double?,
    val category: NutrientCategory,
    val priority: NutrientPriority,
    val isActive: Boolean = true
)

data class NutrientTrend(
    val nutrientName: String,
    val dates: List<LocalDate>,
    val values: List<Double>,
    val targets: List<Double>,
    val averageIntake: Double,
    val averageTarget: Double,
    val adherenceRate: Float,
    val trend: TrendDirection
)

data class MacronutrientBalance(
    val date: LocalDate,
    val caloriesFromProtein: Int,
    val caloriesFromCarbs: Int,
    val caloriesFromFat: Int,
    val proteinPercentage: Float,
    val carbsPercentage: Float,
    val fatPercentage: Float,
    val isBalanced: Boolean,
    val recommendations: List<String>
)

enum class TrendDirection {
    INCREASING, DECREASING, STABLE
}

enum class FitnessGoal {
    WEIGHT_LOSS, MUSCLE_GAIN, MAINTENANCE, ENDURANCE, STRENGTH, VO2_MAX_IMPROVEMENT
}