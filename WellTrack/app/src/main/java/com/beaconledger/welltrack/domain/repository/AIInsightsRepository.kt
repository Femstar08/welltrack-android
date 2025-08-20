package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface AIInsightsRepository {
    
    // AI Recommendations
    suspend fun generateRecommendations(userId: String): Result<List<AIRecommendation>>
    fun getRecommendations(userId: String): Flow<List<AIRecommendation>>
    fun getRecommendationsByType(userId: String, type: RecommendationType): Flow<List<AIRecommendation>>
    fun getRecommendationsByPriority(userId: String, priority: RecommendationPriority): Flow<List<AIRecommendation>>
    suspend fun markRecommendationAsRead(recommendationId: String): Result<Unit>
    suspend fun markRecommendationAsActedUpon(recommendationId: String): Result<Unit>
    
    // Trend Predictions
    suspend fun generateTrendPredictions(userId: String, metrics: List<String>, days: Int): Result<List<TrendPrediction>>
    fun getTrendPredictions(userId: String): Flow<List<TrendPrediction>>
    
    // Health Optimizations
    suspend fun generateHealthOptimizations(userId: String): Result<List<HealthOptimizationSuggestion>>
    fun getHealthOptimizations(userId: String): Flow<List<HealthOptimizationSuggestion>>
    
    // Nutrition Optimization
    suspend fun generateNutritionOptimization(userId: String): Result<NutritionOptimization>
    fun getNutritionOptimization(userId: String): Flow<NutritionOptimization?>
    
    // Fitness Optimization
    suspend fun generateFitnessOptimization(userId: String): Result<FitnessOptimization>
    fun getFitnessOptimization(userId: String): Flow<FitnessOptimization?>
    
    // Pattern Recognition
    suspend fun analyzePatterns(userId: String, days: Int): Result<List<PatternRecognition>>
    fun getPatternRecognitions(userId: String): Flow<List<PatternRecognition>>
    
    // Health Risk Assessment
    suspend fun assessHealthRisks(userId: String): Result<HealthRiskAssessment>
    fun getHealthRiskAssessment(userId: String): Flow<HealthRiskAssessment?>
    
    // Goal Progress Analysis
    suspend fun analyzeGoalProgress(userId: String): Result<List<GoalProgressAnalysis>>
    fun getGoalProgressAnalyses(userId: String): Flow<List<GoalProgressAnalysis>>
    
    // Comprehensive AI Insights
    suspend fun generateComprehensiveInsights(userId: String): Result<AIInsightsSummary>
    fun getAIInsightsSummary(userId: String): Flow<AIInsightsSummary?>
    
    // Data correlation and analysis
    suspend fun analyzeDataCorrelations(userId: String, days: Int): Result<List<CorrelationInsight>>
    suspend fun identifyAnomalies(userId: String, metric: String, days: Int): Result<List<String>>
    
    // Personalized recommendations based on user profile
    suspend fun generatePersonalizedRecommendations(
        userId: String,
        userProfile: UserProfile,
        preferences: UserPreferences
    ): Result<List<AIRecommendation>>
    
    // Learning and adaptation
    suspend fun updateRecommendationFeedback(
        recommendationId: String,
        feedback: RecommendationFeedback
    ): Result<Unit>
    
    suspend fun refreshAIInsights(userId: String): Result<Unit>
}

data class UserProfile(
    val age: Int?,
    val gender: String?,
    val weight: Double?,
    val height: Double?,
    val activityLevel: ActivityLevel,
    val healthGoals: List<String>,
    val dietaryRestrictions: List<String>,
    val medicalConditions: List<String>
)

data class UserPreferences(
    val preferredWorkoutTypes: List<String>,
    val mealPreferences: List<String>,
    val supplementPreferences: List<String>,
    val notificationPreferences: Map<String, Boolean>,
    val privacySettings: Map<String, Boolean>
)

data class RecommendationFeedback(
    val helpful: Boolean,
    val followed: Boolean,
    val effectiveness: Int, // 1-5 scale
    val comments: String?
)