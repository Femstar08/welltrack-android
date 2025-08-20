package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.domain.repository.HealthConnectRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HealthConnectUseCaseTest {
    
    private lateinit var healthConnectRepository: HealthConnectRepository
    private lateinit var healthConnectUseCase: HealthConnectUseCase
    
    private val testUserId = "test-user-id"
    private val testDate = LocalDate.of(2024, 1, 15)
    
    @Before
    fun setup() {
        healthConnectRepository = mockk()
        healthConnectUseCase = HealthConnectUseCase(healthConnectRepository)
    }
    
    @Test
    fun `isHealthConnectAvailable returns repository result`() = runTest {
        // Given
        coEvery { healthConnectRepository.isHealthConnectAvailable() } returns true
        
        // When
        val result = healthConnectUseCase.isHealthConnectAvailable()
        
        // Then
        assertTrue(result)
        coVerify { healthConnectRepository.isHealthConnectAvailable() }
    }
    
    @Test
    fun `hasAllPermissions returns repository result`() = runTest {
        // Given
        coEvery { healthConnectRepository.hasAllPermissions() } returns false
        
        // When
        val result = healthConnectUseCase.hasAllPermissions()
        
        // Then
        assertFalse(result)
        coVerify { healthConnectRepository.hasAllPermissions() }
    }
    
    @Test
    fun `getRequiredPermissions returns repository result`() = runTest {
        // Given
        val expectedPermissions = setOf("permission1", "permission2")
        coEvery { healthConnectRepository.getRequiredPermissions() } returns expectedPermissions
        
        // When
        val result = healthConnectUseCase.getRequiredPermissions()
        
        // Then
        assertEquals(expectedPermissions, result)
        coVerify { healthConnectRepository.getRequiredPermissions() }
    }
    
    @Test
    fun `syncHealthData calls repository with correct parameters`() = runTest {
        // Given
        val days = 7
        coEvery { healthConnectRepository.syncHealthData(testUserId, days) } returns Result.success(Unit)
        
        // When
        val result = healthConnectUseCase.syncHealthData(testUserId, days)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { healthConnectRepository.syncHealthData(testUserId, days) }
    }
    
    @Test
    fun `getStepsForDate returns repository result`() = runTest {
        // Given
        val expectedSteps = 10000
        coEvery { healthConnectRepository.getStepsForDate(testUserId, testDate) } returns expectedSteps
        
        // When
        val result = healthConnectUseCase.getStepsForDate(testUserId, testDate)
        
        // Then
        assertEquals(expectedSteps, result)
        coVerify { healthConnectRepository.getStepsForDate(testUserId, testDate) }
    }
    
    @Test
    fun `getLatestWeight returns repository result`() = runTest {
        // Given
        val expectedWeight = 75.5
        coEvery { healthConnectRepository.getLatestWeight(testUserId) } returns expectedWeight
        
        // When
        val result = healthConnectUseCase.getLatestWeight(testUserId)
        
        // Then
        assertEquals(expectedWeight, result)
        coVerify { healthConnectRepository.getLatestWeight(testUserId) }
    }
    
    @Test
    fun `getHealthSummaryForToday aggregates data correctly`() = runTest {
        // Given
        val today = LocalDate.now()
        val expectedSteps = 8500
        val expectedCalories = 2200.0
        val expectedSleep = 7.5
        val expectedHydration = 2.1
        val expectedHeartRate = 72.0
        val expectedWeight = 75.5
        
        coEvery { healthConnectRepository.getStepsForDate(testUserId, today) } returns expectedSteps
        coEvery { healthConnectRepository.getTotalCaloriesBurned(testUserId, today) } returns expectedCalories
        coEvery { healthConnectRepository.getSleepDuration(testUserId, today) } returns expectedSleep
        coEvery { healthConnectRepository.getHydrationTotal(testUserId, today) } returns expectedHydration
        coEvery { healthConnectRepository.getAverageHeartRate(testUserId, today, today) } returns expectedHeartRate
        coEvery { healthConnectRepository.getLatestWeight(testUserId) } returns expectedWeight
        
        // When
        val result = healthConnectUseCase.getHealthSummaryForToday(testUserId)
        
        // Then
        assertEquals(today, result.date)
        assertEquals(expectedSteps, result.steps)
        assertEquals(expectedCalories, result.caloriesBurned, 0.01)
        assertEquals(expectedSleep, result.sleepHours)
        assertEquals(expectedHydration, result.hydrationLiters, 0.01)
        assertEquals(expectedHeartRate, result.averageHeartRate)
        assertEquals(expectedWeight, result.weight)
    }
    
    @Test
    fun `getWeeklyStepsAverage calculates correctly with data`() = runTest {
        // Given
        val today = LocalDate.now()
        val stepsData = listOf(8000, 9000, 7500, 10000, 8500, 9500, 7000)
        
        stepsData.forEachIndexed { index, steps ->
            val date = today.minusDays(6 - index.toLong())
            coEvery { healthConnectRepository.getStepsForDate(testUserId, date) } returns steps
        }
        
        // When
        val result = healthConnectUseCase.getWeeklyStepsAverage(testUserId)
        
        // Then
        val expectedAverage = stepsData.average().toInt()
        assertEquals(expectedAverage, result)
    }
    
    @Test
    fun `getWeeklyStepsAverage returns zero when no data`() = runTest {
        // Given
        val today = LocalDate.now()
        for (i in 0..6) {
            val date = today.minusDays(i.toLong())
            coEvery { healthConnectRepository.getStepsForDate(testUserId, date) } returns 0
        }
        
        // When
        val result = healthConnectUseCase.getWeeklyStepsAverage(testUserId)
        
        // Then
        assertEquals(0, result)
    }
    
    @Test
    fun `getHealthTrends collects data for specified days`() = runTest {
        // Given
        val days = 7
        val today = LocalDate.now()
        
        // Mock data for each day
        for (i in 0 until days) {
            val date = today.minusDays(days - 1 - i.toLong())
            coEvery { healthConnectRepository.getStepsForDate(testUserId, date) } returns 8000 + i * 100
            coEvery { healthConnectRepository.getTotalCaloriesBurned(testUserId, date) } returns 2000.0 + i * 50
            coEvery { healthConnectRepository.getSleepDuration(testUserId, date) } returns 7.0 + i * 0.1
            coEvery { healthConnectRepository.getHydrationTotal(testUserId, date) } returns 2.0 + i * 0.1
        }
        
        // When
        val result = healthConnectUseCase.getHealthTrends(testUserId, days)
        
        // Then
        assertEquals(days, result.stepsData.size)
        assertEquals(days, result.caloriesData.size)
        assertEquals(days, result.sleepData.size)
        assertEquals(days, result.hydrationData.size)
        
        // Verify first and last data points
        assertEquals(8000, result.stepsData.first().second)
        assertEquals(8600, result.stepsData.last().second)
    }
    
    @Test
    fun `insertHealthMetric calls repository correctly`() = runTest {
        // Given
        val healthMetric = HealthMetric(
            id = "test-id",
            userId = testUserId,
            type = HealthMetricType.WEIGHT,
            value = 75.5,
            unit = "kg",
            timestamp = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            source = DataSource.MANUAL_ENTRY
        )
        
        coEvery { healthConnectRepository.insertHealthMetric(healthMetric) } returns Result.success("test-id")
        
        // When
        val result = healthConnectUseCase.insertHealthMetric(healthMetric)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals("test-id", result.getOrNull())
        coVerify { healthConnectRepository.insertHealthMetric(healthMetric) }
    }
    
    @Test
    fun `deleteHealthMetric calls repository correctly`() = runTest {
        // Given
        val metricId = "test-metric-id"
        coEvery { healthConnectRepository.deleteHealthMetric(metricId) } returns Result.success(Unit)
        
        // When
        val result = healthConnectUseCase.deleteHealthMetric(metricId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { healthConnectRepository.deleteHealthMetric(metricId) }
    }
    
    @Test
    fun `getHealthMetrics returns flow from repository`() = runTest {
        // Given
        val startDate = testDate
        val endDate = testDate.plusDays(7)
        val expectedMetrics = listOf(
            HealthMetric(
                id = "1",
                userId = testUserId,
                type = HealthMetricType.STEPS,
                value = 10000.0,
                unit = "steps",
                timestamp = testDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                source = DataSource.HEALTH_CONNECT
            )
        )
        
        coEvery { 
            healthConnectRepository.getHealthMetrics(testUserId, HealthMetricType.STEPS, startDate, endDate) 
        } returns flowOf(expectedMetrics)
        
        // When
        val resultFlow = healthConnectUseCase.getHealthMetrics(testUserId, HealthMetricType.STEPS, startDate, endDate)
        
        // Then
        resultFlow.collect { metrics ->
            assertEquals(expectedMetrics, metrics)
        }
        
        coVerify { healthConnectRepository.getHealthMetrics(testUserId, HealthMetricType.STEPS, startDate, endDate) }
    }
}