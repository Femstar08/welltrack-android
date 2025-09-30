package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.GoalRepository
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime

class GoalUseCaseTest {
    
    private lateinit var goalRepository: GoalRepository
    private lateinit var goalUseCase: GoalUseCase
    
    private val testUserId = "test_user_id"
    private val testGoalId = "test_goal_id"
    
    @Before
    fun setup() {
        goalRepository = mockk()
        goalUseCase = GoalUseCase(goalRepository)
    }
    
    @Test
    fun `getActiveGoals returns flow of active goals`() = runTest {
        // Given
        val expectedGoals = listOf(
            createTestGoal(id = "goal1", title = "Weight Loss"),
            createTestGoal(id = "goal2", title = "Fitness Goal")
        )
        every { goalRepository.getActiveGoalsForUser(testUserId) } returns flowOf(expectedGoals)
        
        // When
        val result = goalUseCase.getActiveGoals(testUserId)
        
        // Then
        result.collect { goals ->
            assertEquals(2, goals.size)
            assertEquals("Weight Loss", goals[0].title)
            assertEquals("Fitness Goal", goals[1].title)
        }
        
        verify { goalRepository.getActiveGoalsForUser(testUserId) }
    }
    
    @Test
    fun `createGoal successfully creates goal with milestones`() = runTest {
        // Given
        val milestones = listOf(
            CreateMilestoneRequest(
                title = "First Milestone",
                description = "First milestone description",
                targetValue = 5.0,
                targetDate = LocalDate.now().plusDays(15)
            )
        )
        
        coEvery { goalRepository.createGoal(any()) } returns Result.success(testGoalId)
        coEvery { goalRepository.addMilestone(any()) } returns Result.success("milestone_id")
        
        // When
        val result = goalUseCase.createGoal(
            userId = testUserId,
            type = GoalType.WEIGHT_LOSS,
            title = "Lose 10kg",
            description = "Weight loss goal",
            targetValue = 10.0,
            unit = "kg",
            targetDate = LocalDate.now().plusDays(90),
            priority = GoalPriority.HIGH,
            milestones = milestones
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(testGoalId, result.getOrNull())
        
        coVerify { goalRepository.createGoal(any()) }
        coVerify { goalRepository.addMilestone(any()) }
    }
    
    @Test
    fun `addProgress successfully adds progress and checks milestones`() = runTest {
        // Given
        val progressValue = 5.0
        val notes = "Good progress today"
        
        coEvery { goalRepository.addProgress(any()) } returns Result.success("progress_id")
        coEvery { goalRepository.getNextMilestone(testGoalId) } returns createTestMilestone(
            targetValue = 5.0
        )
        coEvery { goalRepository.updateMilestone(any()) } returns Result.success(Unit)
        coEvery { goalRepository.generatePrediction(testGoalId) } returns Result.success(
            createTestPrediction()
        )
        
        // When
        val result = goalUseCase.addProgress(
            goalId = testGoalId,
            value = progressValue,
            notes = notes,
            source = ProgressSource.MANUAL
        )
        
        // Then
        assertTrue(result.isSuccess)
        
        coVerify { goalRepository.addProgress(any()) }
        coVerify { goalRepository.getNextMilestone(testGoalId) }
        coVerify { goalRepository.generatePrediction(testGoalId) }
    }
    
    @Test
    fun `getGoalStatistics returns calculated statistics`() = runTest {
        // Given
        val expectedStatistics = GoalStatistics(
            completionPercentage = 50.0f,
            daysRemaining = 30,
            averageDailyProgress = 0.5,
            requiredDailyProgress = 0.33,
            trendDirection = TrendAnalysis.ON_TRACK,
            milestoneCompletionRate = 25.0f
        )
        
        coEvery { goalRepository.calculateGoalStatistics(testGoalId) } returns Result.success(expectedStatistics)
        
        // When
        val result = goalUseCase.getGoalStatistics(testGoalId)
        
        // Then
        assertTrue(result.isSuccess)
        val statistics = result.getOrNull()!!
        assertEquals(50.0f, statistics.completionPercentage, 0.01f)
        assertEquals(30, statistics.daysRemaining)
        assertEquals(TrendAnalysis.ON_TRACK, statistics.trendDirection)
        
        coVerify { goalRepository.calculateGoalStatistics(testGoalId) }
    }
    
    @Test
    fun `generatePrediction creates prediction based on goal data`() = runTest {
        // Given
        val expectedPrediction = createTestPrediction()
        
        coEvery { goalRepository.generatePrediction(testGoalId) } returns Result.success(expectedPrediction)
        
        // When
        val result = goalUseCase.generatePrediction(testGoalId)
        
        // Then
        assertTrue(result.isSuccess)
        val prediction = result.getOrNull()!!
        assertEquals(TrendAnalysis.ON_TRACK, prediction.trendAnalysis)
        assertEquals(0.8f, prediction.confidenceScore, 0.01f)
        
        coVerify { goalRepository.generatePrediction(testGoalId) }
    }
    
    @Test
    fun `getGoalOverview returns comprehensive overview`() = runTest {
        // Given
        val activeGoals = listOf(
            GoalWithProgress(
                goal = createTestGoal(currentValue = 5.0, targetValue = 10.0),
                progressEntries = listOf(createTestProgress()),
                milestones = listOf(createTestMilestone()),
                prediction = createTestPrediction()
            )
        )
        
        coEvery { goalRepository.getActiveGoalsWithProgress(testUserId) } returns activeGoals
        coEvery { goalRepository.getGoalCompletionRate(testUserId) } returns 75.0f
        coEvery { goalRepository.getOverdueGoalsCount(testUserId) } returns 1
        coEvery { goalRepository.getGoalTrends(testUserId) } returns mapOf(
            GoalType.WEIGHT_LOSS to TrendAnalysis.ON_TRACK
        )
        coEvery { goalRepository.getRecommendations(testUserId) } returns listOf(
            "Keep up the great work!"
        )
        
        // When
        val overview = goalUseCase.getGoalOverview(testUserId)
        
        // Then
        assertEquals(1, overview.totalActiveGoals)
        assertEquals(0, overview.completedGoals) // 5.0 < 10.0
        assertEquals(1, overview.overdueGoals)
        assertEquals(75.0f, overview.averageCompletionRate, 0.01f)
        assertTrue(overview.recommendations.isNotEmpty())
        
        coVerify { goalRepository.getActiveGoalsWithProgress(testUserId) }
        coVerify { goalRepository.getGoalCompletionRate(testUserId) }
        coVerify { goalRepository.getOverdueGoalsCount(testUserId) }
    }
    
    @Test
    fun `getGoalTemplates returns appropriate templates for category`() = runTest {
        // When
        val weightTemplates = goalUseCase.getGoalTemplates(GoalCategory.WEIGHT)
        val fitnessTemplates = goalUseCase.getGoalTemplates(GoalCategory.FITNESS)
        val nutritionTemplates = goalUseCase.getGoalTemplates(GoalCategory.NUTRITION)
        
        // Then
        assertTrue(weightTemplates.isNotEmpty())
        assertTrue(fitnessTemplates.isNotEmpty())
        assertTrue(nutritionTemplates.isNotEmpty())
        
        // Check weight templates
        val weightLossTemplate = weightTemplates.find { it.type == GoalType.WEIGHT_LOSS }
        assertNotNull(weightLossTemplate)
        assertEquals("kg", weightLossTemplate!!.unit)
        assertEquals(90, weightLossTemplate.suggestedTimeframe)
        
        // Check fitness templates
        val fitnessTemplate = fitnessTemplates.find { it.type == GoalType.FITNESS_PERFORMANCE }
        assertNotNull(fitnessTemplate)
        assertEquals("steps", fitnessTemplate!!.unit)
        
        // Check nutrition templates
        val nutritionTemplate = nutritionTemplates.find { it.type == GoalType.NUTRITION_TARGET }
        assertNotNull(nutritionTemplate)
        assertEquals("g", nutritionTemplate!!.unit)
    }
    
    @Test
    fun `deactivateGoal successfully deactivates goal`() = runTest {
        // Given
        coEvery { goalRepository.deactivateGoal(testGoalId) } returns Result.success(Unit)
        
        // When
        val result = goalUseCase.deactivateGoal(testGoalId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { goalRepository.deactivateGoal(testGoalId) }
    }
    
    @Test
    fun `addMilestone successfully adds milestone to goal`() = runTest {
        // Given
        val milestoneTitle = "Test Milestone"
        val milestoneDescription = "Test Description"
        val targetValue = 5.0
        val targetDate = LocalDate.now().plusDays(30)
        val order = 1
        
        coEvery { goalRepository.addMilestone(any()) } returns Result.success("milestone_id")
        
        // When
        val result = goalUseCase.addMilestone(
            goalId = testGoalId,
            title = milestoneTitle,
            description = milestoneDescription,
            targetValue = targetValue,
            targetDate = targetDate,
            order = order
        )
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("milestone_id", result.getOrNull())
        
        coVerify { 
            goalRepository.addMilestone(
                match { milestone ->
                    milestone.goalId == testGoalId &&
                    milestone.title == milestoneTitle &&
                    milestone.targetValue == targetValue &&
                    milestone.order == order
                }
            )
        }
    }
    
    // Helper methods for creating test data
    private fun createTestGoal(
        id: String = testGoalId,
        title: String = "Test Goal",
        currentValue: Double = 0.0,
        targetValue: Double = 10.0
    ) = Goal(
        id = id,
        userId = testUserId,
        type = GoalType.WEIGHT_LOSS,
        title = title,
        description = "Test goal description",
        targetValue = targetValue,
        currentValue = currentValue,
        unit = "kg",
        startDate = LocalDate.now().minusDays(30),
        targetDate = LocalDate.now().plusDays(60),
        isActive = true,
        priority = GoalPriority.MEDIUM,
        category = GoalCategory.WEIGHT,
        createdAt = LocalDateTime.now().minusDays(30),
        updatedAt = LocalDateTime.now()
    )
    
    private fun createTestProgress() = GoalProgress(
        id = "progress_id",
        goalId = testGoalId,
        value = 2.5,
        notes = "Test progress",
        recordedAt = LocalDateTime.now(),
        source = ProgressSource.MANUAL
    )
    
    private fun createTestMilestone(
        targetValue: Double = 5.0
    ) = GoalMilestone(
        id = "milestone_id",
        goalId = testGoalId,
        title = "Test Milestone",
        description = "Test milestone description",
        targetValue = targetValue,
        targetDate = LocalDate.now().plusDays(30),
        isCompleted = false,
        completedAt = null,
        order = 1
    )
    
    private fun createTestPrediction() = GoalPrediction(
        id = "prediction_id",
        goalId = testGoalId,
        predictedCompletionDate = LocalDate.now().plusDays(45),
        confidenceScore = 0.8f,
        trendAnalysis = TrendAnalysis.ON_TRACK,
        recommendedAdjustments = listOf("Keep up the good work!"),
        calculatedAt = LocalDateTime.now()
    )
}