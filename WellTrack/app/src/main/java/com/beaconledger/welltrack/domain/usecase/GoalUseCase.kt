package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalUseCase @Inject constructor(
    private val goalRepository: GoalRepository
) {
    
    // Goal management operations
    fun getActiveGoals(userId: String): Flow<List<Goal>> {
        return goalRepository.getActiveGoalsForUser(userId)
    }
    
    fun getAllGoals(userId: String): Flow<List<Goal>> {
        return goalRepository.getAllGoalsForUser(userId)
    }
    
    suspend fun getGoalById(goalId: String): Goal? {
        return goalRepository.getGoalById(goalId)
    }
    
    fun getGoalsByCategory(userId: String, category: GoalCategory): Flow<List<Goal>> {
        return goalRepository.getGoalsByCategory(userId, category)
    }
    
    suspend fun createGoal(
        userId: String,
        type: GoalType,
        title: String,
        description: String?,
        targetValue: Double,
        unit: String,
        targetDate: LocalDate,
        priority: GoalPriority = GoalPriority.MEDIUM,
        milestones: List<CreateMilestoneRequest> = emptyList()
    ): Result<String> {
        val goal = Goal(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            title = title,
            description = description,
            targetValue = targetValue,
            currentValue = 0.0,
            unit = unit,
            startDate = LocalDate.now(),
            targetDate = targetDate,
            priority = priority,
            category = mapTypeToCategory(type),
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        
        val result = goalRepository.createGoal(goal)
        
        // Add milestones if provided
        if (result.isSuccess && milestones.isNotEmpty()) {
            milestones.forEachIndexed { index, milestoneRequest ->
                val milestone = GoalMilestone(
                    id = UUID.randomUUID().toString(),
                    goalId = result.getOrNull() ?: goal.id,
                    title = milestoneRequest.title,
                    description = milestoneRequest.description,
                    targetValue = milestoneRequest.targetValue,
                    targetDate = milestoneRequest.targetDate,
                    order = index + 1,
                    isCompleted = false,
                    completedAt = null
                )
                goalRepository.addMilestone(milestone)
            }
        }
        
        return result
    }
    
    suspend fun updateGoal(goal: Goal): Result<Unit> {
        return goalRepository.updateGoal(goal)
    }
    
    suspend fun deactivateGoal(goalId: String): Result<Unit> {
        return goalRepository.deactivateGoal(goalId)
    }
    
    suspend fun deleteGoal(goalId: String): Result<Unit> {
        return goalRepository.deleteGoal(goalId)
    }
    
    // Progress tracking operations
    fun getProgressForGoal(goalId: String): Flow<List<GoalProgress>> {
        return goalRepository.getProgressForGoal(goalId)
    }
    
    suspend fun addProgress(
        goalId: String,
        value: Double,
        notes: String? = null,
        source: ProgressSource = ProgressSource.MANUAL
    ): Result<String> {
        val progress = GoalProgress(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            value = value,
            notes = notes,
            recordedAt = LocalDateTime.now(),
            source = source
        )
        
        val result = goalRepository.addProgress(progress)
        
        // Check if milestone should be completed
        if (result.isSuccess) {
            checkAndCompleteMilestones(goalId, value)
            
            // Generate new prediction after progress update
            goalRepository.generatePrediction(goalId)
        }
        
        return result
    }
    
    suspend fun updateProgress(progress: GoalProgress): Result<Unit> {
        return goalRepository.updateProgress(progress)
    }
    
    // Milestone operations
    fun getMilestonesForGoal(goalId: String): Flow<List<GoalMilestone>> {
        return goalRepository.getMilestonesForGoal(goalId)
    }
    
    suspend fun addMilestone(
        goalId: String,
        title: String,
        description: String?,
        targetValue: Double,
        targetDate: LocalDate?,
        order: Int
    ): Result<String> {
        val milestone = GoalMilestone(
            id = UUID.randomUUID().toString(),
            goalId = goalId,
            title = title,
            description = description,
            targetValue = targetValue,
            targetDate = targetDate,
            order = order,
            isCompleted = false,
            completedAt = null
        )
        
        return goalRepository.addMilestone(milestone)
    }
    
    suspend fun completeMilestone(milestoneId: String): Result<Unit> {
        return goalRepository.completeMilestone(milestoneId)
    }
    
    suspend fun getNextMilestone(goalId: String): GoalMilestone? {
        return goalRepository.getNextMilestone(goalId)
    }
    
    // Analytics and predictions
    suspend fun getGoalStatistics(goalId: String): Result<GoalStatistics> {
        return goalRepository.calculateGoalStatistics(goalId)
    }
    
    suspend fun generatePrediction(goalId: String): Result<GoalPrediction> {
        return goalRepository.generatePrediction(goalId)
    }
    
    suspend fun getLatestPrediction(goalId: String): GoalPrediction? {
        return goalRepository.getLatestPrediction(goalId)
    }
    
    suspend fun getGoalWithProgress(goalId: String): GoalWithProgress? {
        return goalRepository.getGoalWithProgress(goalId)
    }
    
    suspend fun getActiveGoalsWithProgress(userId: String): List<GoalWithProgress> {
        return goalRepository.getActiveGoalsWithProgress(userId)
    }
    
    // Dashboard and overview operations
    suspend fun getGoalOverview(userId: String): GoalOverview {
        val activeGoals = goalRepository.getActiveGoalsWithProgress(userId)
        val completionRate = goalRepository.getGoalCompletionRate(userId)
        val overdueCount = goalRepository.getOverdueGoalsCount(userId)
        val trends = goalRepository.getGoalTrends(userId)
        val recommendations = goalRepository.getRecommendations(userId)
        
        return GoalOverview(
            totalActiveGoals = activeGoals.size,
            completedGoals = activeGoals.count { it.goal.currentValue >= it.goal.targetValue },
            overdueGoals = overdueCount,
            averageCompletionRate = completionRate,
            goalsByCategory = activeGoals.groupBy { it.goal.category }.mapValues { it.value.size },
            recentProgress = activeGoals.flatMap { it.progressEntries.take(5) },
            upcomingMilestones = activeGoals.flatMap { goalWithProgress ->
                goalWithProgress.milestones.filter { !it.isCompleted }.take(3)
            },
            trends = trends,
            recommendations = recommendations
        )
    }
    
    // Automatic updates from other systems
    suspend fun updateGoalsFromHealthData(userId: String, healthMetrics: List<HealthMetric>): Result<Unit> {
        return try {
            val healthGoals = goalRepository.getGoalsByCategory(userId, GoalCategory.HEALTH)
            val fitnessGoals = goalRepository.getGoalsByCategory(userId, GoalCategory.FITNESS)
            val weightGoals = goalRepository.getGoalsByCategory(userId, GoalCategory.WEIGHT)
            
            // Update each relevant goal
            // This would need to be implemented with proper flow collection
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGoalsFromMealData(userId: String, meals: List<Meal>): Result<Unit> {
        return try {
            val nutritionGoals = goalRepository.getGoalsByCategory(userId, GoalCategory.NUTRITION)
            
            // Update nutrition goals based on meal data
            // This would need proper implementation
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateGoalsFromHabitData(userId: String, habits: List<CustomHabit>): Result<Unit> {
        return try {
            val habitGoals = goalRepository.getGoalsByCategory(userId, GoalCategory.HABITS)
            
            // Update habit goals
            // This would need proper implementation
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Goal templates and suggestions
    suspend fun getGoalTemplates(category: GoalCategory): List<GoalTemplate> {
        return when (category) {
            GoalCategory.WEIGHT -> listOf(
                GoalTemplate(
                    type = GoalType.WEIGHT_LOSS,
                    title = "Lose Weight",
                    description = "Achieve your target weight through healthy eating and exercise",
                    unit = "kg",
                    suggestedTimeframe = 90,
                    milestoneTemplates = listOf(
                        MilestoneTemplate("Lose first 2kg", 2.0, 14),
                        MilestoneTemplate("Reach halfway point", 0.0, 45),
                        MilestoneTemplate("Final 2kg", 0.0, 75)
                    )
                ),
                GoalTemplate(
                    type = GoalType.WEIGHT_GAIN,
                    title = "Gain Weight",
                    description = "Build healthy weight through proper nutrition and strength training",
                    unit = "kg",
                    suggestedTimeframe = 120,
                    milestoneTemplates = listOf(
                        MilestoneTemplate("Gain first 1kg", 1.0, 21),
                        MilestoneTemplate("Reach halfway point", 0.0, 60),
                        MilestoneTemplate("Final push", 0.0, 100)
                    )
                )
            )
            GoalCategory.FITNESS -> listOf(
                GoalTemplate(
                    type = GoalType.FITNESS_PERFORMANCE,
                    title = "Daily Steps Goal",
                    description = "Achieve consistent daily step count",
                    unit = "steps",
                    suggestedTimeframe = 30,
                    milestoneTemplates = listOf(
                        MilestoneTemplate("First week consistency", 7.0, 7),
                        MilestoneTemplate("Two weeks strong", 14.0, 14),
                        MilestoneTemplate("Full month achievement", 30.0, 30)
                    )
                )
            )
            GoalCategory.NUTRITION -> listOf(
                GoalTemplate(
                    type = GoalType.NUTRITION_TARGET,
                    title = "Daily Protein Intake",
                    description = "Meet daily protein requirements for muscle building",
                    unit = "g",
                    suggestedTimeframe = 60,
                    milestoneTemplates = listOf(
                        MilestoneTemplate("First week success", 7.0, 7),
                        MilestoneTemplate("One month consistent", 30.0, 30),
                        MilestoneTemplate("Two months strong", 60.0, 60)
                    )
                )
            )
            GoalCategory.HABITS -> listOf(
                GoalTemplate(
                    type = GoalType.HABIT_FORMATION,
                    title = "Daily Meditation",
                    description = "Build a consistent meditation practice",
                    unit = "days",
                    suggestedTimeframe = 66,
                    milestoneTemplates = listOf(
                        MilestoneTemplate("First week", 7.0, 7),
                        MilestoneTemplate("21-day habit", 21.0, 21),
                        MilestoneTemplate("Habit formed", 66.0, 66)
                    )
                )
            )
            GoalCategory.HEALTH -> listOf(
                GoalTemplate(
                    type = GoalType.HEALTH_METRIC,
                    title = "Blood Pressure Control",
                    description = "Maintain healthy blood pressure levels",
                    unit = "mmHg",
                    suggestedTimeframe = 90,
                    milestoneTemplates = listOf(
                        MilestoneTemplate("First month improvement", 0.0, 30),
                        MilestoneTemplate("Sustained progress", 0.0, 60),
                        MilestoneTemplate("Target achieved", 0.0, 90)
                    )
                )
            )
        }
    }
    
    // Private helper methods
    private suspend fun checkAndCompleteMilestones(goalId: String, currentValue: Double) {
        val nextMilestone = goalRepository.getNextMilestone(goalId)
        if (nextMilestone != null && currentValue >= nextMilestone.targetValue) {
            val completedMilestone = nextMilestone.copy(
                isCompleted = true,
                completedAt = LocalDateTime.now()
            )
            goalRepository.updateMilestone(completedMilestone)
            
            // Check for next milestone
            checkAndCompleteMilestones(goalId, currentValue)
        }
    }
    
    private fun mapTypeToCategory(type: GoalType): GoalCategory {
        return when (type) {
            GoalType.WEIGHT_LOSS, GoalType.WEIGHT_GAIN -> GoalCategory.WEIGHT
            GoalType.MUSCLE_GAIN, GoalType.FITNESS_PERFORMANCE -> GoalCategory.FITNESS
            GoalType.NUTRITION_TARGET -> GoalCategory.NUTRITION
            GoalType.HABIT_FORMATION -> GoalCategory.HABITS
            GoalType.HEALTH_METRIC, GoalType.BODY_FAT_REDUCTION -> GoalCategory.HEALTH
            GoalType.CUSTOM -> GoalCategory.HEALTH // Default for custom goals
        }
    }
}

// Data classes for use case operations
data class CreateMilestoneRequest(
    val title: String,
    val description: String?,
    val targetValue: Double,
    val targetDate: LocalDate?
)

data class GoalOverview(
    val totalActiveGoals: Int,
    val completedGoals: Int,
    val overdueGoals: Int,
    val averageCompletionRate: Float,
    val goalsByCategory: Map<GoalCategory, Int>,
    val recentProgress: List<GoalProgress>,
    val upcomingMilestones: List<GoalMilestone>,
    val trends: Map<GoalType, GoalTrend>,
    val recommendations: List<String>
)

data class GoalTemplate(
    val type: GoalType,
    val title: String,
    val description: String,
    val unit: String,
    val suggestedTimeframe: Int, // days
    val milestoneTemplates: List<MilestoneTemplate>
)

data class MilestoneTemplate(
    val title: String,
    val targetValue: Double,
    val dayOffset: Int
)