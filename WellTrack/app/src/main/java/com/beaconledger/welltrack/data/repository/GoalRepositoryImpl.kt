package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.GoalDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.GoalRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.max

@Singleton
class GoalRepositoryImpl @Inject constructor(
    private val goalDao: GoalDao,
    private val gson: Gson
) : GoalRepository {
    
    override fun getActiveGoalsForUser(userId: String): Flow<List<Goal>> {
        return goalDao.getActiveGoalsForUser(userId)
    }
    
    override fun getAllGoalsForUser(userId: String): Flow<List<Goal>> {
        return goalDao.getAllGoalsForUserFlow(userId)
    }
    
    override suspend fun getGoalById(goalId: String): Goal? {
        return goalDao.getGoalById(goalId)
    }
    
    override fun getGoalsByType(userId: String, type: GoalType): Flow<List<Goal>> {
        return goalDao.getGoalsByType(userId, type)
    }
    
    override fun getGoalsByCategory(userId: String, category: GoalCategory): Flow<List<Goal>> {
        return goalDao.getGoalsByCategory(userId, category)
    }
    
    override suspend fun createGoal(goal: Goal): Result<String> {
        return try {
            val goalWithId = goal.copy(
                id = UUID.randomUUID().toString(),
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
            goalDao.insertGoal(goalWithId)
            Result.success(goalWithId.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateGoal(goal: Goal): Result<Unit> {
        return try {
            val updatedGoal = goal.copy(updatedAt = LocalDateTime.now())
            goalDao.updateGoal(updatedGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deactivateGoal(goalId: String): Result<Unit> {
        return try {
            goalDao.deactivateGoal(goalId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteGoal(goalId: String): Result<Unit> {
        return try {
            goalDao.deleteGoal(goalId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getProgressForGoal(goalId: String): Flow<List<GoalProgress>> {
        return goalDao.getProgressForGoal(goalId)
    }
    
    override suspend fun addProgress(progress: GoalProgress): Result<String> {
        return try {
            val progressWithId = progress.copy(
                id = UUID.randomUUID().toString(),
                recordedAt = LocalDateTime.now()
            )
            goalDao.insertProgress(progressWithId)
            
            // Update goal's current value
            updateGoalCurrentValue(progress.goalId, progress.value)
            
            Result.success(progressWithId.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateProgress(progress: GoalProgress): Result<Unit> {
        return try {
            goalDao.updateProgress(progress)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteProgress(progressId: String): Result<Unit> {
        return try {
            val progress = goalDao.getProgressById(progressId)
            progress?.let { goalDao.deleteProgress(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getRecentProgress(goalId: String, limit: Int): List<GoalProgress> {
        return goalDao.getRecentProgressForGoal(goalId, limit)
    }
    
    override fun getMilestonesForGoal(goalId: String): Flow<List<GoalMilestone>> {
        return goalDao.getMilestonesForGoal(goalId)
    }
    
    override suspend fun addMilestone(milestone: GoalMilestone): Result<String> {
        return try {
            val milestoneWithId = milestone.copy(
                id = UUID.randomUUID().toString()
            )
            goalDao.insertMilestone(milestoneWithId)
            Result.success(milestoneWithId.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateMilestone(milestone: GoalMilestone): Result<Unit> {
        return try {
            goalDao.updateMilestone(milestone)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun completeMilestone(milestoneId: String): Result<Unit> {
        return try {
            val milestone = goalDao.getMilestoneById(milestoneId)

            milestone?.let {
                val completedMilestone = it.copy(
                    isCompleted = true,
                    completedAt = LocalDateTime.now()
                )
                goalDao.updateMilestone(completedMilestone)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteMilestone(milestoneId: String): Result<Unit> {
        return try {
            val milestone = goalDao.getMilestoneById(milestoneId)

            milestone?.let {
                goalDao.deleteMilestone(it)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getNextMilestone(goalId: String): GoalMilestone? {
        return goalDao.getNextMilestone(goalId)
    }
    
    override suspend fun generatePrediction(goalId: String): Result<GoalPrediction> {
        return try {
            val goal = goalDao.getGoalById(goalId) ?: return Result.failure(Exception("Goal not found"))
            val recentProgress = goalDao.getRecentProgressForGoal(goalId, 30)
            
            val prediction = calculatePrediction(goal, recentProgress)
            goalDao.insertPrediction(prediction)
            
            Result.success(prediction)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getLatestPrediction(goalId: String): GoalPrediction? {
        return goalDao.getLatestPredictionForGoal(goalId)
    }
    
    override suspend fun calculateGoalStatistics(goalId: String): Result<GoalStatistics> {
        return try {
            val goal = goalDao.getGoalById(goalId) ?: return Result.failure(Exception("Goal not found"))
            val progress = goalDao.getRecentProgressForGoal(goalId, 100)
            val completedMilestones = goalDao.getCompletedMilestonesCount(goalId)
            val totalMilestones = goalDao.getTotalMilestonesCount(goalId)
            
            val statistics = calculateStatistics(goal, progress, completedMilestones, totalMilestones)
            Result.success(statistics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGoalWithProgress(goalId: String): GoalWithProgress? {
        return goalDao.getGoalWithProgress(goalId)
    }
    
    override suspend fun getActiveGoalsWithProgress(userId: String): List<GoalWithProgress> {
        return goalDao.getActiveGoalsWithProgress(userId)
    }
    
    override suspend fun updateGoalFromHealthData(goalId: String, healthMetrics: List<HealthMetric>): Result<Unit> {
        return try {
            val goal = goalDao.getGoalById(goalId) ?: return Result.failure(Exception("Goal not found"))
            
            // Find relevant health metrics for this goal
            val relevantMetrics = healthMetrics.filter { metric ->
                isMetricRelevantToGoal(goal, metric)
            }
            
            // Update goal progress based on health data
            val progressEntries = relevantMetrics.map { metric ->
                GoalProgress(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    value = metric.value,
                    notes = "Auto-updated from ${metric.source}",
                    recordedAt = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    source = mapHealthSourceToProgressSource(metric.source)
                )
            }
            goalDao.insertAllProgress(progressEntries)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateGoalFromMealData(goalId: String, meals: List<Meal>): Result<Unit> {
        return try {
            val goal = goalDao.getGoalById(goalId) ?: return Result.failure(Exception("Goal not found"))

            when (goal.type) {
                GoalType.NUTRITION_TARGET -> {
                    // Calculate nutrition metrics from meals
                    val totalCalories = meals.sumOf { meal ->
                        try {
                            val nutritionInfo = gson.fromJson(meal.nutritionInfo, NutritionInfo::class.java)
                            nutritionInfo.calories
                        } catch (e: Exception) {
                            0.0
                        }
                    }
                    val totalProtein = meals.sumOf { meal ->
                        try {
                            val nutritionInfo = gson.fromJson(meal.nutritionInfo, NutritionInfo::class.java)
                            nutritionInfo.proteins
                        } catch (e: Exception) {
                            0.0
                        }
                    }

                    // Update progress based on goal's unit
                    val progressValue = when (goal.unit.lowercase()) {
                        "kcal", "calories" -> totalCalories
                        "protein", "g protein" -> totalProtein
                        else -> totalCalories
                    }

                    val progress = GoalProgress(
                        id = UUID.randomUUID().toString(),
                        goalId = goalId,
                        value = progressValue,
                        notes = "Auto-updated from meal data",
                        recordedAt = LocalDateTime.now(),
                        source = ProgressSource.MEAL_LOGGING
                    )
                    goalDao.insertAllProgress(listOf(progress)) // Use batch insert even for single item
                    updateGoalCurrentValue(goalId, progressValue)
                }
                else -> {
                    // No meal data relevant for this goal type
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateGoalFromHabitData(goalId: String, habits: List<CustomHabit>): Result<Unit> {
        return try {
            val goal = goalDao.getGoalById(goalId) ?: return Result.failure(Exception("Goal not found"))

            if (goal.type == GoalType.HABIT_FORMATION) {
                // Count active habits for today - should use HabitCompletion entities
                // For now, just count active habits
                val activeHabits = habits.count { it.isActive }

                val progress = GoalProgress(
                    id = UUID.randomUUID().toString(),
                    goalId = goalId,
                    value = activeHabits.toDouble(),
                    notes = "Auto-updated from habit tracking",
                    recordedAt = LocalDateTime.now(),
                    source = ProgressSource.HABIT_TRACKING
                )
                goalDao.insertAllProgress(listOf(progress)) // Use batch insert even for single item
                updateGoalCurrentValue(goalId, activeHabits.toDouble())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getGoalCompletionRate(userId: String): Float {
        return try {
            val completedGoals = goalDao.getCompletedGoalsCount(userId)
            val allGoals = goalDao.getAllGoalsForUser(userId)
            val totalGoals = allGoals.size

            if (totalGoals == 0) {
                0.0f
            } else {
                (completedGoals.toFloat() / totalGoals) * 100f
            }
        } catch (e: Exception) {
            0.0f
        }
    }
    
    override suspend fun getOverdueGoalsCount(userId: String): Int {
        return goalDao.getOverdueGoalsCount(userId, LocalDate.now())
    }
    
    override suspend fun getGoalTrends(userId: String, period: Int): Map<GoalType, GoalTrend> {
        return try {
            val goals = goalDao.getAllGoalsForUser(userId)
            val trendMap = mutableMapOf<GoalType, GoalTrend>()

            goals.groupBy { it.type }.forEach { (goalType, goalsOfType) ->
                val allProgress = goalsOfType.flatMap { goal ->
                    goalDao.getRecentProgressForGoal(goal.id, period)
                }

                val trend = if (allProgress.size >= 2) {
                    val recent = allProgress.take(period / 2).map { it.value }
                    val older = allProgress.drop(period / 2).map { it.value }

                    if (recent.isEmpty() || older.isEmpty()) {
                        GoalTrend.ON_TRACK
                    } else {
                        val recentAvg = recent.average()
                        val olderAvg = older.average()

                        when {
                            recentAvg > olderAvg * 1.1 -> GoalTrend.ACCELERATING
                            recentAvg < olderAvg * 0.9 -> GoalTrend.DECLINING
                            else -> GoalTrend.ON_TRACK
                        }
                    }
                } else {
                    GoalTrend.ON_TRACK
                }

                trendMap[goalType] = trend
            }

            trendMap
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    override suspend fun getRecommendations(userId: String): List<String> {
        return try {
            val goals = goalDao.getAllGoalsForUser(userId)
            val overdueCount = goalDao.getOverdueGoalsCount(userId, LocalDate.now())
            val recommendations = mutableListOf<String>()

            // Analyze goal patterns and provide recommendations
            if (overdueCount > 0) {
                recommendations.add("You have $overdueCount overdue goals. Consider adjusting their timelines or breaking them into smaller milestones.")
            }

            val stagnantGoals = goals.filter { goal ->
                val recentProgress = goalDao.getRecentProgressForGoal(goal.id, 7)
                recentProgress.isEmpty()
            }

            if (stagnantGoals.isNotEmpty()) {
                recommendations.add("${stagnantGoals.size} goals haven't been updated recently. Regular tracking helps maintain momentum.")
            }

            val highPriorityGoals = goals.filter { it.priority == GoalPriority.HIGH || it.priority == GoalPriority.CRITICAL }
            if (highPriorityGoals.size > 3) {
                recommendations.add("You have ${highPriorityGoals.size} high-priority goals. Focus on 2-3 at a time for better results.")
            }

            // Add goal-type specific recommendations
            goals.groupBy { it.type }.forEach { (type, goalsOfType) ->
                when (type) {
                    GoalType.WEIGHT_LOSS -> {
                        recommendations.add("For weight loss goals, combine consistent meal logging with regular exercise tracking.")
                    }
                    GoalType.HABIT_FORMATION -> {
                        recommendations.add("Habit goals succeed with daily consistency. Set up reminders and track completion rates.")
                    }
                    GoalType.FITNESS_PERFORMANCE -> {
                        recommendations.add("Sync your fitness goals with health platforms for automatic progress tracking.")
                    }
                    else -> {}
                }
            }

            if (recommendations.isEmpty()) {
                recommendations.add("Great job maintaining your goals! Keep up the consistent tracking.")
            }

            recommendations.take(5) // Limit to 5 recommendations
        } catch (e: Exception) {
            listOf("Unable to generate recommendations at this time. Please try again later.")
        }
    }
    
    // Private helper methods
    private suspend fun updateGoalCurrentValue(goalId: String, newValue: Double) {
        val goal = goalDao.getGoalById(goalId)
        goal?.let {
            val updatedGoal = it.copy(
                currentValue = newValue,
                updatedAt = LocalDateTime.now()
            )
            goalDao.updateGoal(updatedGoal)
        }
    }
    
    private fun calculatePrediction(goal: Goal, progressHistory: List<GoalProgress>): GoalPrediction {
        val remainingValue = goal.targetValue - goal.currentValue
        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.targetDate)
        
        val trend = if (progressHistory.isNotEmpty()) {
            val recentProgress = progressHistory.takeLast(7)
            val avgRecent = recentProgress.map { it.value }.average()
            
            when {
                avgRecent > goal.targetValue / daysRemaining * 1.1 -> GoalTrend.AHEAD_OF_SCHEDULE
                goal.currentValue >= goal.targetValue * 0.9 -> GoalTrend.ON_TRACK
                else -> GoalTrend.BEHIND_SCHEDULE
            }
        } else {
            GoalTrend.ON_TRACK
        }
        
        val averageDailyProgress = if (progressHistory.isNotEmpty()) {
            progressHistory.map { it.value }.average() / max(1, progressHistory.size)
        } else {
            0.0
        }
        
        val predictedDays = if (averageDailyProgress > 0) {
            (remainingValue / averageDailyProgress).toInt()
        } else {
            daysRemaining.toInt()
        }
        
        val predictedCompletionDate = LocalDate.now().plusDays(predictedDays.toLong())
        val confidenceScore = calculateConfidenceScore(progressHistory, trend)
        
        return GoalPrediction(
            id = UUID.randomUUID().toString(),
            goalId = goal.id,
            predictedCompletionDate = predictedCompletionDate,
            confidenceScore = confidenceScore,
            trendAnalysis = trend,
            recommendedAdjustments = generateRecommendations(goal, trend),
            calculatedAt = LocalDateTime.now()
        )
    }
    
    private fun calculateStatistics(
        goal: Goal,
        progress: List<GoalProgress>,
        completedMilestones: Int,
        totalMilestones: Int
    ): GoalStatistics {
        val completionPercentage = if (goal.targetValue > 0) {
            (goal.currentValue / goal.targetValue * 100).toFloat().coerceIn(0f, 100f)
        } else {
            0f
        }
        
        val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), goal.targetDate).toInt()
        val averageDailyProgress = if (progress.isNotEmpty()) {
            progress.map { it.value }.average()
        } else {
            0.0
        }
        
        val remainingValue = goal.targetValue - goal.currentValue
        val requiredDailyProgress = if (daysRemaining > 0) {
            remainingValue / daysRemaining
        } else {
            0.0
        }
        
        val milestoneCompletionRate = if (totalMilestones > 0) {
            (completedMilestones.toFloat() / totalMilestones * 100)
        } else {
            0f
        }
        
        val trendDirection = determineTrendDirection(progress)
        
        return GoalStatistics(
            completionPercentage = completionPercentage,
            daysRemaining = daysRemaining,
            averageDailyProgress = averageDailyProgress,
            requiredDailyProgress = requiredDailyProgress,
            trendDirection = trendDirection,
            milestoneCompletionRate = milestoneCompletionRate
        )
    }
    
    private fun calculateConfidenceScore(progress: List<GoalProgress>, trend: GoalTrend): Float {
        val dataPoints = progress.size
        val consistency = calculateConsistency(progress)
        
        return when {
            dataPoints < 3 -> 0.3f
            trend == GoalTrend.STAGNANT -> 0.4f
            trend == GoalTrend.DECLINING -> 0.2f
            consistency > 0.8f -> 0.9f
            consistency > 0.6f -> 0.7f
            else -> 0.5f
        }
    }
    
    private fun calculateConsistency(progress: List<GoalProgress>): Float {
        if (progress.size < 2) return 0f
        
        val values = progress.map { it.value }
        val mean = values.average()
        val variance = values.map { (it - mean) * (it - mean) }.average()
        val standardDeviation = kotlin.math.sqrt(variance)
        
        return if (mean > 0) {
            1f - (standardDeviation / mean).toFloat().coerceIn(0f, 1f)
        } else {
            0f
        }
    }
    
    private fun generateRecommendations(goal: Goal, trend: GoalTrend): List<String> {
        return when (trend) {
            GoalTrend.BEHIND_SCHEDULE -> listOf(
                "Consider increasing daily effort",
                "Review and adjust your strategy",
                "Set smaller, more achievable milestones"
            )
            GoalTrend.STAGNANT -> listOf(
                "Try a different approach",
                "Seek support or guidance",
                "Break down the goal into smaller steps"
            )
            GoalTrend.DECLINING -> listOf(
                "Reassess your goal and timeline",
                "Consider external factors affecting progress",
                "Take a break and restart with renewed motivation"
            )
            else -> listOf(
                "Keep up the great work!",
                "Stay consistent with your current approach"
            )
        }
    }
    
    private fun determineTrendDirection(progress: List<GoalProgress>): GoalTrend {
        if (progress.size < 2) return GoalTrend.ON_TRACK
        
        val recent = progress.take(5).map { it.value }
        val older = progress.drop(5).take(5).map { it.value }
        
        if (recent.isEmpty() || older.isEmpty()) return GoalTrend.ON_TRACK
        
        val recentAvg = recent.average()
        val olderAvg = older.average()
        
        return when {
            recentAvg > olderAvg * 1.1 -> GoalTrend.ACCELERATING
            recentAvg < olderAvg * 0.9 -> GoalTrend.DECLINING
            else -> GoalTrend.ON_TRACK
        }
    }
    
    private fun isMetricRelevantToGoal(goal: Goal, metric: HealthMetric): Boolean {
        return when (goal.type) {
            GoalType.WEIGHT_LOSS, GoalType.WEIGHT_GAIN -> metric.type == HealthMetricType.WEIGHT
            GoalType.BODY_FAT_REDUCTION -> metric.type == HealthMetricType.BODY_FAT_PERCENTAGE
            GoalType.FITNESS_PERFORMANCE -> metric.type in listOf(
                HealthMetricType.STEPS,
                HealthMetricType.HEART_RATE,
                HealthMetricType.CALORIES_BURNED
            )
            else -> false
        }
    }
    
    private fun mapHealthSourceToProgressSource(source: DataSource): ProgressSource {
        return when (source) {
            DataSource.HEALTH_CONNECT -> ProgressSource.HEALTH_CONNECT
            DataSource.GARMIN_CONNECT -> ProgressSource.GARMIN
            DataSource.SAMSUNG_HEALTH -> ProgressSource.SAMSUNG_HEALTH
            DataSource.MANUAL_ENTRY -> ProgressSource.MANUAL
            else -> ProgressSource.AUTOMATIC
        }
    }
}
