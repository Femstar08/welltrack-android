package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.GoalDao
import com.beaconledger.welltrack.data.model.*
import com.google.gson.Gson
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class GoalRepositoryImplTest {

    private lateinit var goalDao: GoalDao
    private lateinit var gson: Gson
    private lateinit var goalRepository: GoalRepositoryImpl

    private val testUserId = "test_user_id"
    private val testGoalId = "test_goal_id"

    @Before
    fun setup() {
        goalDao = mockk(relaxed = true)
        gson = mockk(relaxed = true)
        goalRepository = GoalRepositoryImpl(goalDao, gson)
    }

    // Helper function to access private methods for testing
    private fun GoalRepositoryImpl.callCalculatePrediction(goal: Goal, progressHistory: List<GoalProgress>): GoalPrediction {
        val method = GoalRepositoryImpl::class.java.getDeclaredMethod("calculatePrediction", Goal::class.java, List::class.java)
        method.isAccessible = true
        return method.invoke(goalRepository, goal, progressHistory) as GoalPrediction
    }

    private fun GoalRepositoryImpl.callCalculateStatistics(
        goal: Goal,
        progress: List<GoalProgress>,
        completedMilestones: Int,
        totalMilestones: Int
    ): GoalStatistics {
        val method = GoalRepositoryImpl::class.java.getDeclaredMethod(
            "calculateStatistics",
            Goal::class.java,
            List::class.java,
            Int::class.java,
            Int::class.java
        )
        method.isAccessible = true
        return method.invoke(goalRepository, goal, progress, completedMilestones, totalMilestones) as GoalStatistics
    }

    private fun GoalRepositoryImpl.callCalculateConfidenceScore(progress: List<GoalProgress>, trend: TrendAnalysis): Float {
        val method = GoalRepositoryImpl::class.java.getDeclaredMethod("calculateConfidenceScore", List::class.java, TrendAnalysis::class.java)
        method.isAccessible = true
        return method.invoke(goalRepository, goal, trend) as Float
    }

    private fun GoalRepositoryImpl.callCalculateConsistency(progress: List<GoalProgress>): Float {
        val method = GoalRepositoryImpl::class.java.getDeclaredMethod("calculateConsistency", List::class.java)
        method.isAccessible = true
        return method.invoke(goalRepository, progress) as Float
    }

    private fun GoalRepositoryImpl.callGenerateRecommendations(goal: Goal, trend: TrendAnalysis): List<String> {
        val method = GoalRepositoryImpl::class.java.getDeclaredMethod("generateRecommendations", Goal::class.java, TrendAnalysis::class.java)
        method.isAccessible = true
        return method.invoke(goalRepository, goal, trend) as List<String>
    }

    private fun GoalRepositoryImpl.callDetermineTrendDirection(progress: List<GoalProgress>): TrendAnalysis {
        val method = GoalRepositoryImpl::class.java.getDeclaredMethod("determineTrendDirection", List::class.java)
        method.isAccessible = true
        return method.invoke(goalRepository, progress) as TrendAnalysis
    }

    // --- Test calculatePrediction ---
    @Test
    fun `calculatePrediction should return correct prediction for consistent progress`() = runTest {
        val goal = createTestGoal(currentValue = 5.0, targetValue = 100.0, targetDate = LocalDate.now().plusDays(100))
        val progress = (1..10).map { i -> createTestProgress(value = 1.0, recordedAt = LocalDateTime.now().minusDays(10 - i.toLong())) }

        val prediction = goalRepository.callCalculatePrediction(goal, progress)

        assertNotNull(prediction)
        assertEquals(TrendAnalysis.ON_TRACK, prediction.trendAnalysis)
        assertTrue(prediction.confidenceScore > 0.5f)
    }

    @Test
    fun `calculatePrediction should return accelerating trend for increasing progress`() = runTest {
        val goal = createTestGoal(currentValue = 10.0, targetValue = 100.0, targetDate = LocalDate.now().plusDays(100))
        val progress = listOf(
            createTestProgress(value = 1.0, recordedAt = LocalDateTime.now().minusDays(10)),
            createTestProgress(value = 1.1, recordedAt = LocalDateTime.now().minusDays(9)),
            createTestProgress(value = 1.2, recordedAt = LocalDateTime.now().minusDays(8)),
            createTestProgress(value = 1.3, recordedAt = LocalDateTime.now().minusDays(7)),
            createTestProgress(value = 1.4, recordedAt = LocalDateTime.now().minusDays(6)),
            createTestProgress(value = 1.5, recordedAt = LocalDateTime.now().minusDays(5)),
            createTestProgress(value = 1.6, recordedAt = LocalDateTime.now().minusDays(4)),
            createTestProgress(value = 1.7, recordedAt = LocalDateTime.now().minusDays(3)),
            createTestProgress(value = 1.8, recordedAt = LocalDateTime.now().minusDays(2)),
            createTestProgress(value = 1.9, recordedAt = LocalDateTime.now().minusDays(1))
        )

        val prediction = goalRepository.callCalculatePrediction(goal, progress)
        assertEquals(TrendAnalysis.ACCELERATING, prediction.trendAnalysis)
    }

    @Test
    fun `calculatePrediction should return declining trend for decreasing progress`() = runTest {
        val goal = createTestGoal(currentValue = 20.0, targetValue = 100.0, targetDate = LocalDate.now().plusDays(100))
        val progress = listOf(
            createTestProgress(value = 2.0, recordedAt = LocalDateTime.now().minusDays(10)),
            createTestProgress(value = 1.9, recordedAt = LocalDateTime.now().minusDays(9)),
            createTestProgress(value = 1.8, recordedAt = LocalDateTime.now().minusDays(8)),
            createTestProgress(value = 1.7, recordedAt = LocalDateTime.now().minusDays(7)),
            createTestProgress(value = 1.6, recordedAt = LocalDateTime.now().minusDays(6)),
            createTestProgress(value = 1.5, recordedAt = LocalDateTime.now().minusDays(5)),
            createTestProgress(value = 1.4, recordedAt = LocalDateTime.now().minusDays(4)),
            createTestProgress(value = 1.3, recordedAt = LocalDateTime.now().minusDays(3)),
            createTestProgress(value = 1.2, recordedAt = LocalDateTime.now().minusDays(2)),
            createTestProgress(value = 1.1, recordedAt = LocalDateTime.now().minusDays(1))
        )

        val prediction = goalRepository.callCalculatePrediction(goal, progress)
        assertEquals(TrendAnalysis.DECLINING, prediction.trendAnalysis)
    }

    // --- Test calculateStatistics ---
    @Test
    fun `calculateStatistics should return correct completion percentage`() = runTest {
        val goal = createTestGoal(currentValue = 50.0, targetValue = 100.0)
        val statistics = goalRepository.callCalculateStatistics(goal, emptyList(), 0, 0)
        assertEquals(50.0f, statistics.completionPercentage, 0.01f)
    }

    @Test
    fun `calculateStatistics should return correct days remaining`() = runTest {
        val goal = createTestGoal(targetDate = LocalDate.now().plusDays(10))
        val statistics = goalRepository.callCalculateStatistics(goal, emptyList(), 0, 0)
        assertEquals(10, statistics.daysRemaining)
    }

    // --- Test calculateConfidenceScore ---
    @Test
    fun `calculateConfidenceScore should be low for few data points`() = runTest {
        val progress = listOf(createTestProgress())
        val score = goalRepository.callCalculateConfidenceScore(progress, TrendAnalysis.ON_TRACK)
        assertEquals(0.3f, score, 0.01f)
    }

    @Test
    fun `calculateConfidenceScore should be high for consistent progress`() = runTest {
        val progress = (1..10).map { createTestProgress(value = 1.0) }
        val score = goalRepository.callCalculateConfidenceScore(progress, TrendAnalysis.ON_TRACK)
        assertTrue(score > 0.8f)
    }

    // --- Test calculateConsistency ---
    @Test
    fun `calculateConsistency should be 0 for less than 2 data points`() = runTest {
        val progress = listOf(createTestProgress())
        val consistency = goalRepository.callCalculateConsistency(progress)
        assertEquals(0f, consistency, 0.01f)
    }

    @Test
    fun `calculateConsistency should be high for consistent values`() = runTest {
        val progress = (1..5).map { createTestProgress(value = 10.0) }
        val consistency = goalRepository.callCalculateConsistency(progress)
        assertEquals(1f, consistency, 0.01f)
    }

    // --- Test generateRecommendations ---
    @Test
    fun `generateRecommendations should suggest adjustments for behind schedule trend`() = runTest {
        val goal = createTestGoal()
        val recommendations = goalRepository.callGenerateRecommendations(goal, TrendAnalysis.BEHIND_SCHEDULE)
        assertTrue(recommendations.isNotEmpty())
        assertTrue(recommendations.any { it.contains("adjust your strategy") })
    }

    // --- Test determineTrendDirection ---
    @Test
    fun `determineTrendDirection should be on track for insufficient data`() = runTest {
        val progress = listOf(createTestProgress())
        val trend = goalRepository.callDetermineTrendDirection(progress)
        assertEquals(TrendAnalysis.ON_TRACK, trend)
    }

    @Test
    fun `determineTrendDirection should be accelerating for increasing recent progress`() = runTest {
        val progress = listOf(
            createTestProgress(value = 1.0, recordedAt = LocalDateTime.now().minusDays(10)),
            createTestProgress(value = 1.1, recordedAt = LocalDateTime.now().minusDays(9)),
            createTestProgress(value = 1.2, recordedAt = LocalDateTime.now().minusDays(8)),
            createTestProgress(value = 1.3, recordedAt = LocalDateTime.now().minusDays(7)),
            createTestProgress(value = 1.4, recordedAt = LocalDateTime.now().minusDays(6)),
            createTestProgress(value = 2.0, recordedAt = LocalDateTime.now().minusDays(5)),
            createTestProgress(value = 2.1, recordedAt = LocalDateTime.now().minusDays(4)),
            createTestProgress(value = 2.2, recordedAt = LocalDateTime.now().minusDays(3)),
            createTestProgress(value = 2.3, recordedAt = LocalDateTime.now().minusDays(2)),
            createTestProgress(value = 2.4, recordedAt = LocalDateTime.now().minusDays(1))
        )
        val trend = goalRepository.callDetermineTrendDirection(progress)
        assertEquals(TrendAnalysis.ACCELERATING, trend)
    }

    // Helper methods for creating test data
    private fun createTestGoal(
        id: String = testGoalId,
        title: String = "Test Goal",
        currentValue: Double = 0.0,
        targetValue: Double = 10.0,
        targetDate: LocalDate = LocalDate.now().plusDays(90)
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
        targetDate = targetDate,
        isActive = true,
        priority = GoalPriority.MEDIUM,
        category = GoalCategory.WEIGHT,
        createdAt = LocalDateTime.now().minusDays(30),
        updatedAt = LocalDateTime.now()
    )

    private fun createTestProgress(
        goalId: String = testGoalId,
        value: Double = 1.0,
        recordedAt: LocalDateTime = LocalDateTime.now()
    ) = GoalProgress(
        id = "progress_${UUID.randomUUID()}",
        goalId = goalId,
        value = value,
        notes = "Test progress",
        recordedAt = recordedAt,
        source = ProgressSource.MANUAL
    )
}
