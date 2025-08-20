package com.beaconledger.welltrack.data.model

import java.time.LocalDate
import java.time.LocalDateTime

data class TodaysSummary(
    val userId: String,
    val date: LocalDate,
    val mealsLogged: Int,
    val totalCalories: Int,
    val supplementsTaken: Int,
    val totalSupplements: Int,
    val waterIntakeMl: Int,
    val waterTargetMl: Int,
    val stepsCount: Int?,
    val activeMinutes: Int?,
    val sleepHours: Double?,
    val energyLevelAverage: Float,
    val moodAverage: Float,
    val completionPercentage: Float
)

data class NutritionTrend(
    val date: LocalDate,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val fiber: Double,
    val water: Int
)

data class FitnessStats(
    val date: LocalDate,
    val steps: Int?,
    val activeMinutes: Int?,
    val caloriesBurned: Int?,
    val heartRateAverage: Int?,
    val sleepHours: Double?,
    val workoutCount: Int
)

data class SupplementAdherence(
    val date: LocalDate,
    val supplementName: String,
    val taken: Boolean,
    val scheduledTime: LocalDateTime?,
    val actualTime: LocalDateTime?
)

data class WeeklyAnalytics(
    val userId: String,
    val weekStartDate: LocalDate,
    val nutritionTrends: List<NutritionTrend>,
    val fitnessStats: List<FitnessStats>,
    val supplementAdherence: List<SupplementAdherence>,
    val averageEnergyLevel: Float,
    val averageMood: Float,
    val totalMealsLogged: Int,
    val mealPlanAdherence: Float,
    val waterIntakeAverage: Int,
    val supplementAdherenceRate: Float
)

data class MonthlyAnalytics(
    val userId: String,
    val month: Int,
    val year: Int,
    val weeklyAnalytics: List<WeeklyAnalytics>,
    val nutritionAverages: NutritionAverages,
    val fitnessAverages: FitnessAverages,
    val topPerformingDays: List<LocalDate>,
    val improvementAreas: List<ImprovementArea>,
    val achievements: List<Achievement>
)

data class NutritionAverages(
    val dailyCalories: Double,
    val dailyProtein: Double,
    val dailyCarbs: Double,
    val dailyFat: Double,
    val dailyFiber: Double,
    val dailyWater: Double
)

data class FitnessAverages(
    val dailySteps: Double,
    val dailyActiveMinutes: Double,
    val dailyCaloriesBurned: Double,
    val averageHeartRate: Double,
    val averageSleepHours: Double,
    val workoutsPerWeek: Double
)

data class ImprovementArea(
    val category: String,
    val description: String,
    val currentValue: Double,
    val targetValue: Double,
    val priority: Priority
)

enum class Priority {
    LOW, MEDIUM, HIGH
}

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val dateAchieved: LocalDate,
    val category: AchievementCategory,
    val value: Double? = null
)

enum class AchievementCategory {
    NUTRITION,
    FITNESS,
    HYDRATION,
    SUPPLEMENTS,
    CONSISTENCY,
    GOALS
}

data class CalendarActivity(
    val date: LocalDate,
    val mealsLogged: Int,
    val supplementsTaken: Int,
    val workoutsCompleted: Int,
    val waterGoalMet: Boolean,
    val energyLevel: Int?,
    val mood: Int?,
    val notes: String?,
    val photos: List<String>,
    val overallRating: Float // 0.0 to 1.0
)

data class ChartDataPoint(
    val date: LocalDate,
    val value: Double,
    val label: String? = null
)

data class TrendAnalysis(
    val metric: String,
    val trend: TrendDirection,
    val changePercentage: Double,
    val description: String,
    val recommendation: String?
)

enum class TrendDirection {
    IMPROVING,
    DECLINING,
    STABLE
}

data class CorrelationInsight(
    val metric1: String,
    val metric2: String,
    val correlationStrength: Double, // -1.0 to 1.0
    val description: String,
    val actionableInsight: String?
)