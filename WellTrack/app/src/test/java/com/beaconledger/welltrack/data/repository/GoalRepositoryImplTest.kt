package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.GoalDao
import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalRepositoryImplTest {

    @Mock
    private lateinit var goalDao: GoalDao

    private lateinit var goalRepository: GoalRepositoryImpl

    private val testUserId = "test_user_123"
    private val testGoalId = "test_goal_123"

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        goalRepository = GoalRepositoryImpl(goalDao)
    }

    @Test
    fun `createGoal should generate ID and insert goal successfully`() = runTest {
        // Given
        val testGoal = createTestGoal()
        whenever(goalDao.insertGoal(any())).thenReturn(Unit)

        // When
        val result = goalRepository.createGoal(testGoal)

        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).insertGoal(any())
    }

    @Test
    fun `createGoal should handle database errors`() = runTest {
        // Given
        val testGoal = createTestGoal()
        whenever(goalDao.insertGoal(any())).thenThrow(RuntimeException("Database error"))

        // When
        val result = goalRepository.createGoal(testGoal)

        // Then
        assertTrue(result.isFailure)
    }

    @Test
    fun `addProgress should update goal current value`() = runTest {
        // Given
        val testGoal = createTestGoal()
        val testProgress = createTestProgress()

        whenever(goalDao.insertProgress(any())).thenReturn(1L)
        whenever(goalDao.getGoalById(testGoalId)).thenReturn(testGoal)
        whenever(goalDao.updateGoal(any())).thenReturn(Unit)

        // When
        val result = goalRepository.addProgress(testProgress)

        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).insertProgress(any())
        verify(goalDao).updateGoal(any())
    }

    @Test
    fun `generatePrediction should calculate based on progress history`() = runTest {
        // Given
        val testGoal = createTestGoal()
        val progressHistory = listOf(
            createTestProgress(value = 10.0),
            createTestProgress(value = 20.0),
            createTestProgress(value = 30.0)
        )

        whenever(goalDao.getGoalById(testGoalId)).thenReturn(testGoal)
        whenever(goalDao.getRecentProgressForGoal(testGoalId, 30)).thenReturn(progressHistory)
        whenever(goalDao.insertPrediction(any())).thenReturn(1L)

        // When
        val result = goalRepository.generatePrediction(testGoalId)

        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).insertPrediction(any())
    }

    @Test
    fun `calculateGoalStatistics should return accurate stats`() = runTest {
        // Given
        val testGoal = createTestGoal(currentValue = 50.0, targetValue = 100.0)
        val progressHistory = listOf(
            createTestProgress(value = 10.0),
            createTestProgress(value = 25.0),
            createTestProgress(value = 40.0),
            createTestProgress(value = 50.0)
        )

        whenever(goalDao.getGoalById(testGoalId)).thenReturn(testGoal)
        whenever(goalDao.getRecentProgressForGoal(testGoalId, 100)).thenReturn(progressHistory)
        whenever(goalDao.getCompletedMilestonesCount(testGoalId)).thenReturn(2)
        whenever(goalDao.getTotalMilestonesCount(testGoalId)).thenReturn(4)

        // When
        val result = goalRepository.calculateGoalStatistics(testGoalId)

        // Then
        assertTrue(result.isSuccess)
        val statistics = result.getOrNull()!!
        assertEquals(50.0f, statistics.completionPercentage)
        assertEquals(50.0f, statistics.milestoneCompletionRate)
    }

    @Test
    fun `updateGoalFromHealthData should update relevant goals`() = runTest {
        // Given
        val testGoal = createTestGoal(type = GoalType.WEIGHT_LOSS)
        val healthMetrics = listOf(
            createTestHealthMetric(type = HealthMetricType.WEIGHT, value = 70.0),
            createTestHealthMetric(type = HealthMetricType.STEPS, value = 10000.0)
        )

        whenever(goalDao.getGoalById(testGoalId)).thenReturn(testGoal)
        whenever(goalDao.insertProgress(any())).thenReturn(1L)

        // When
        val result = goalRepository.updateGoalFromHealthData(testGoalId, healthMetrics)

        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).insertProgress(any())
    }

    @Test
    fun `updateGoalFromMealData should update nutrition goals`() = runTest {
        // Given
        val testGoal = createTestGoal(type = GoalType.NUTRITION_TARGET, unit = "kcal")
        val meals = listOf(
            createTestMeal(calories = 500.0),
            createTestMeal(calories = 300.0)
        )

        whenever(goalDao.getGoalById(testGoalId)).thenReturn(testGoal)
        whenever(goalDao.insertProgress(any())).thenReturn(1L)
        whenever(goalDao.updateGoal(any())).thenReturn(Unit)

        // When
        val result = goalRepository.updateGoalFromMealData(testGoalId, meals)

        // Then
        assertTrue(result.isSuccess)
        verify(goalDao).insertProgress(any())
    }

    @Test
    fun `getGoalCompletionRate should calculate correctly`() = runTest {
        // Given
        val allGoals = listOf(
            createTestGoal(currentValue = 100.0, targetValue = 100.0), // Completed
            createTestGoal(currentValue = 50.0, targetValue = 100.0),  // Not completed
            createTestGoal(currentValue = 75.0, targetValue = 100.0)   // Not completed
        )

        whenever(goalDao.getCompletedGoalsCount(testUserId)).thenReturn(1)
        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(allGoals)

        // When
        val completionRate = goalRepository.getGoalCompletionRate(testUserId)

        // Then
        assertEquals(33.33f, completionRate, 0.1f)
    }

    @Test
    fun `getGoalTrends should analyze progress trends`() = runTest {
        // Given
        val goals = listOf(
            createTestGoal(type = GoalType.WEIGHT_LOSS),
            createTestGoal(type = GoalType.FITNESS_PERFORMANCE)
        )
        val progressData = listOf(
            createTestProgress(value = 10.0),
            createTestProgress(value = 15.0),
            createTestProgress(value = 20.0)
        )

        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(goals)
        whenever(goalDao.getRecentProgressForGoal(any(), any())).thenReturn(progressData)

        // When
        val trends = goalRepository.getGoalTrends(testUserId, 30)

        // Then
        assertTrue(trends.isNotEmpty())
        assertTrue(trends.containsKey(GoalType.WEIGHT_LOSS))
    }

    @Test
    fun `getRecommendations should provide relevant suggestions`() = runTest {
        // Given
        val overdueGoals = 2
        val allGoals = listOf(
            createTestGoal(targetDate = LocalDate.now().minusDays(5), currentValue = 50.0, targetValue = 100.0),
            createTestGoal(targetDate = LocalDate.now().minusDays(3), currentValue = 30.0, targetValue = 100.0),
            createTestGoal(targetDate = LocalDate.now().plusDays(10), currentValue = 80.0, targetValue = 100.0)
        )

        whenever(goalDao.getAllGoalsForUser(testUserId)).thenReturn(allGoals)
        whenever(goalDao.getOverdueGoalsCount(testUserId, LocalDate.now())).thenReturn(overdueGoals)
        whenever(goalDao.getRecentProgressForGoal(any(), eq(7))).thenReturn(emptyList())

        // When
        val recommendations = goalRepository.getRecommendations(testUserId)

        // Then
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.any { it.contains("overdue") })
    }

    // Helper methods to create test data
    private fun createTestGoal(
        id: String = testGoalId,
        userId: String = testUserId,
        type: GoalType = GoalType.WEIGHT_LOSS,
        currentValue: Double = 0.0,
        targetValue: Double = 100.0,
        unit: String = "kg",
        targetDate: LocalDate = LocalDate.now().plusDays(30)
    ) = Goal(
        id = id,
        userId = userId,
        type = type,
        title = "Test Goal",
        description = "Test goal description",
        targetValue = targetValue,
        currentValue = currentValue,
        unit = unit,
        startDate = LocalDate.now(),
        targetDate = targetDate,
        isActive = true,
        priority = GoalPriority.MEDIUM,
        category = GoalCategory.WEIGHT,
        milestones = emptyList(),
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )

    private fun createTestProgress(
        id: String = "progress_123",
        goalId: String = testGoalId,
        value: Double = 10.0
    ) = GoalProgress(
        id = id,
        goalId = goalId,
        value = value,
        notes = "Test progress",
        recordedAt = LocalDateTime.now(),
        source = ProgressSource.MANUAL
    )

    private fun createTestHealthMetric(
        type: HealthMetricType = HealthMetricType.WEIGHT,
        value: Double = 70.0
    ) = HealthMetric(
        id = "metric_123",
        userId = testUserId,
        type = type,
        value = value,
        unit = "kg",
        timestamp = LocalDateTime.now(),
        source = DataSource.MANUAL_ENTRY,
        metadata = null
    )

    private fun createTestMeal(
        calories: Double = 500.0
    ) = Meal(
        id = "meal_123",
        userId = testUserId,
        mealType = MealType.LUNCH,
        recipeName = "Test Meal",
        timestamp = LocalDateTime.now(),
        nutritionInfo = NutritionInfo(
            calories = calories,
            protein = 20.0,
            carbohydrates = 50.0,
            fat = 15.0
        ),
        status = MealStatus.COMPLETED,
        notes = null
    )
}