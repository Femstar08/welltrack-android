package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface GoalRepository {
    
    // Goal management
    fun getActiveGoalsForUser(userId: String): Flow<List<Goal>>
    fun getAllGoalsForUser(userId: String): Flow<List<Goal>>
    suspend fun getGoalById(goalId: String): Goal?
    fun getGoalsByType(userId: String, type: GoalType): Flow<List<Goal>>
    fun getGoalsByCategory(userId: String, category: GoalCategory): Flow<List<Goal>>
    suspend fun createGoal(goal: Goal): Result<String>
    suspend fun updateGoal(goal: Goal): Result<Unit>
    suspend fun deactivateGoal(goalId: String): Result<Unit>
    suspend fun deleteGoal(goalId: String): Result<Unit>
    
    // Progress tracking
    fun getProgressForGoal(goalId: String): Flow<List<GoalProgress>>
    suspend fun addProgress(progress: GoalProgress): Result<String>
    suspend fun updateProgress(progress: GoalProgress): Result<Unit>
    suspend fun deleteProgress(progressId: String): Result<Unit>
    suspend fun getRecentProgress(goalId: String, limit: Int = 10): List<GoalProgress>
    
    // Milestone management
    fun getMilestonesForGoal(goalId: String): Flow<List<GoalMilestone>>
    suspend fun addMilestone(milestone: GoalMilestone): Result<String>
    suspend fun updateMilestone(milestone: GoalMilestone): Result<Unit>
    suspend fun completeMilestone(milestoneId: String): Result<Unit>
    suspend fun deleteMilestone(milestoneId: String): Result<Unit>
    suspend fun getNextMilestone(goalId: String): GoalMilestone?
    
    // Predictions and analytics
    suspend fun generatePrediction(goalId: String): Result<GoalPrediction>
    suspend fun getLatestPrediction(goalId: String): GoalPrediction?
    suspend fun calculateGoalStatistics(goalId: String): Result<GoalStatistics>
    
    // Complex operations
    suspend fun getGoalWithProgress(goalId: String): GoalWithProgress?
    suspend fun getActiveGoalsWithProgress(userId: String): List<GoalWithProgress>
    
    // Automatic progress updates
    suspend fun updateGoalFromHealthData(goalId: String, healthMetrics: List<HealthMetric>): Result<Unit>
    suspend fun updateGoalFromMealData(goalId: String, meals: List<Meal>): Result<Unit>
    suspend fun updateGoalFromHabitData(goalId: String, habits: List<CustomHabit>): Result<Unit>
    
    // Statistics and insights
    suspend fun getGoalCompletionRate(userId: String): Float
    suspend fun getOverdueGoalsCount(userId: String): Int
    suspend fun getGoalTrends(userId: String, period: Int = 30): Map<GoalType, TrendAnalysis>
    suspend fun getRecommendations(userId: String): List<String>
}