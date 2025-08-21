package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.health.HealthConnectManager
import com.beaconledger.welltrack.data.health.HealthDataPrioritizer
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(MockitoJUnitRunner::class)
class HealthConnectRepositoryImplTest {

    @Mock
    private lateinit var healthMetricDao: HealthMetricDao

    @Mock
    private lateinit var healthConnectManager: HealthConnectManager

    @Mock
    private lateinit var healthDataPrioritizer: HealthDataPrioritizer

    private lateinit var repository: HealthConnectRepositoryImpl

    private val testHealthMetric = HealthMetric(
        id = "metric1",
        userId = "user1",
        type = HealthMetricType.HEART_RATE,
        value = 75.0,
        unit = "bpm",
        timestamp = LocalDateTime.now(),
        source = DataSource.HEALTH_CONNECT,
        confidence = 1.0f,
        isManualEntry = false
    )

    @Before
    fun setup() {
        repository = HealthConnectRepositoryImpl(
            healthMetricDao,
            healthConnectManager,
            healthDataPrioritizer
        )
    }

    @Test
    fun `syncHealthConnectData syncs data successfully`() = runTest {
        // Given
        val healthMetrics = listOf(testHealthMetric)
        whenever(healthConnectManager.syncAllData("user1")).thenReturn(Result.success(healthMetrics))

        // When
        val result = repository.syncHealthConnectData("user1")

        // Then
        assertTrue(result.isSuccess)
        verify(healthConnectManager).syncAllData("user1")
    }

    @Test
    fun `getHealthMetrics returns prioritized metrics`() = runTest {
        // Given
        val metrics = listOf(testHealthMetric)
        val prioritizedMetrics = listOf(testHealthMetric)
        whenever(healthMetricDao.getHealthMetricsForUser("user1", HealthMetricType.HEART_RATE))
            .thenReturn(flowOf(metrics))
        whenever(healthDataPrioritizer.prioritizeMetrics(metrics)).thenReturn(prioritizedMetrics)

        // When
        val result = repository.getHealthMetrics("user1", HealthMetricType.HEART_RATE)

        // Then
        result.collect { resultMetrics ->
            assertEquals(prioritizedMetrics, resultMetrics)
        }
    }

    @Test
    fun `saveHealthMetric saves metric to database`() = runTest {
        // When
        val result = repository.saveHealthMetric(testHealthMetric)

        // Then
        assertTrue(result.isSuccess)
        verify(healthMetricDao).insertHealthMetric(testHealthMetric)
    }

    @Test
    fun `manuallyEnterHealthMetric creates manual entry`() = runTest {
        // When
        val result = repository.manuallyEnterHealthMetric(
            userId = "user1",
            metricType = HealthMetricType.WEIGHT,
            value = 70.0,
            timestamp = LocalDateTime.now(),
            notes = "Morning weight"
        )

        // Then
        assertTrue(result.isSuccess)
        verify(healthMetricDao).insertHealthMetric(org.mockito.kotlin.any())
    }
}