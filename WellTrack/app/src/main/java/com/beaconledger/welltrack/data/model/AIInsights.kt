package com.beaconledger.welltrack.data.model

import java.time.LocalDateTime

data class AIRecommendation(
    val id: String,
    val userId: String,
    val type: RecommendationType,
    val title: String,
    val description: String,
    val actionableSteps: List<String>,
    val confidence: Float, // 0.0 to 1.0
    val priority: RecommendationPriority,
    val category: RecommendationCategory,
    val basedOnData: List<String>, // Data sources used for recommendation
    val createdAt: LocalDateTime,
    val expiresAt: LocalDateTime?,
    val isRead: Boolean = false,
    val isActedUpon: Boolean = false,
    val metadata: String? = null // JSON for additional data
)

enum class RecommendationType {
    NUTRITION_OPTIMIZATION,
    MEAL_TIMING,
    SUPPLEMENT_ADJUSTMENT,
    HYDRATION_IMPROVEMENT,
    SLEEP_OPTIMIZATION,
    EXERCISE_RECOMMENDATION,
    STRESS_MANAGEMENT,
    HABIT_FORMATION,
    GOAL_ADJUSTMENT,
    HEALTH_ALERT
}

enum class RecommendationPriority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT
}

enum class RecommendationCategory {
    NUTRITION,
    FITNESS,
    WELLNESS,
    HABITS,
    GOALS,
    HEALTH
}

data class TrendPrediction(
    val metric: String,
    val currentValue: Double,
    val predictedValue: Double,
    val timeframe: Int, // days
    val confidence: Float,
    val factors: List<String>, // Contributing factors
    val recommendation: String?
)

data class HealthOptimizationSuggestion(
    val area: String,
    val currentScore: Float, // 0.0 to 1.0
    val potentialImprovement: Float, // 0.0 to 1.0
    val suggestions: List<String>,
    val estimatedTimeToSeeResults: Int, // days
    val difficulty: DifficultyLevel
)

enum class DifficultyLevel {
    EASY,
    MODERATE,
    CHALLENGING,
    EXPERT
}

data class NutritionOptimization(
    val currentIntake: Map<String, Double>, // nutrient -> amount
    val recommendedIntake: Map<String, Double>,
    val deficiencies: List<String>,
    val excesses: List<String>,
    val mealTimingRecommendations: List<MealTimingRecommendation>,
    val supplementSuggestions: List<SupplementSuggestion>
)

data class MealTimingRecommendation(
    val mealType: String,
    val recommendedTime: String,
    val reasoning: String,
    val macroDistribution: Map<String, Double>
)

data class SupplementSuggestion(
    val supplementName: String,
    val dosage: String,
    val timing: String,
    val reasoning: String,
    val confidence: Float
)

data class FitnessOptimization(
    val currentActivityLevel: ActivityLevel,
    val recommendedActivityLevel: ActivityLevel,
    val workoutRecommendations: List<WorkoutRecommendation>,
    val recoveryRecommendations: List<String>,
    val progressPrediction: String
)

enum class AIActivityLevel {
    SEDENTARY,
    LIGHTLY_ACTIVE,
    MODERATELY_ACTIVE,
    VERY_ACTIVE,
    EXTREMELY_ACTIVE
}

data class WorkoutRecommendation(
    val type: String,
    val duration: Int, // minutes
    val frequency: Int, // times per week
    val intensity: String,
    val reasoning: String
)

data class PatternRecognition(
    val patternType: String,
    val description: String,
    val frequency: String,
    val impact: String,
    val recommendation: String,
    val confidence: Float
)

data class HealthRiskAssessment(
    val riskFactors: List<RiskFactor>,
    val overallRiskScore: Float, // 0.0 to 1.0
    val recommendations: List<String>,
    val monitoringSuggestions: List<String>
)

data class RiskFactor(
    val factor: String,
    val severity: RiskSeverity,
    val description: String,
    val mitigation: String
)

enum class RiskSeverity {
    LOW,
    MODERATE,
    HIGH,
    CRITICAL
}

data class GoalProgressAnalysis(
    val goalId: String,
    val goalName: String,
    val currentProgress: Float, // 0.0 to 1.0
    val predictedCompletion: LocalDateTime?,
    val adjustmentRecommendations: List<String>,
    val motivationalMessage: String
)

data class AIInsightsSummary(
    val userId: String,
    val generatedAt: LocalDateTime,
    val recommendations: List<AIRecommendation>,
    val trendPredictions: List<TrendPrediction>,
    val healthOptimizations: List<HealthOptimizationSuggestion>,
    val nutritionOptimization: NutritionOptimization?,
    val fitnessOptimization: FitnessOptimization?,
    val patternRecognitions: List<PatternRecognition>,
    val healthRiskAssessment: HealthRiskAssessment?,
    val goalProgressAnalyses: List<GoalProgressAnalysis>
)