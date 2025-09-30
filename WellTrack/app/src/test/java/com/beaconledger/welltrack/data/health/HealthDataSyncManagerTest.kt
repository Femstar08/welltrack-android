package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.data.cache.HealthDataCacheManager
import com.beaconledger.welltrack.data.cache.OfflineCacheManager
import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.sync.SyncService
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HealthDataSyncManagerTest {
    
    private lateinit var healthDataSyncManager: HealthDataSyncManager
    private lateinit var mockContext: Context
    private lateinit var mockHealthConnectManager: HealthConnectManager
    private lateinit var mockSamsungHealthManager: SamsungHealthManager
    private lateinit var mockGarminConnectManager: GarminConnectManager
    private lateinit var mockHealthDataPrioritizer: HealthDataPrioritizer
    private lateinit var mockHealthDataValidator: HealthDataValidator
    private lateinit var mockHealthMetricDao: HealthMetricDao
    private lateinit var mockSyncService: SyncService
    private lateinit var mockOfflineCacheManager: OfflineCacheManager
    private lateinit var mockHealthDataConflictResolver: HealthDataConflictResolver
    
    @Before
    fun setup() {
        mockContext = mockk()
        mockHealthConnectManager = mockk()
        mockSamsungHealthManager = mockk()
        mockGarminConnectManager = mockk()
        mockHealthDataPrioritizer = mockk()
        mockHealthDataValidator = mockk()
        mockHealthMetricDao = mockk()
        mockSyncService = mockk()
        mockOfflineCacheManager = mockk()
        mockHealthDataConflictResolver = mockk()
        
        healthDataSyncManager = HealthDataSyncManager(
            context = mockContext,
            healthConnectManager = mockHealthConnectManager,
            samsungHealthManager = mockSamsungHealthManager,
            garminConnectManager = mockGarminConnectManager,
            healthDataPrioritizer = mockHealthDataPrioritizer,
            healthDataValidator = mockHealthDataValidator,
            healthMetricDao = mockHealthMetricDao,
            syncService = mockSyncService,
            offlineCacheManager = mockOfflineCacheManager,
            healthDataConflictResolver = mockHealthDataConflictResolver
        )
    }
    
    @Test
    fun `performBidirectionalSync should successfully sync health data from all platforms`() = runTest {
        // Arrange
        val userId = "test-user-id"
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val syncTimeRange = startTime to endTime
        
        val healthConnectMetrics = listOf(
            createTestHealthMetric(userId, HealthMetricType.STEPS, 10000.0, DataSource.HEALTH_CONNECT)
        )
        val samsungHealthMetrics = listOf(
            createTestHealthMetric(userId, HealthMetricType.HEART_RATE, 75.0, DataSource.SAMSUNG_HEALTH)
        )
        val garminMetrics = listOf(
            createTestHealthMetric(userId, HealthMetricType.HRV, 45.0, DataSource.GARMIN)
        )
        
        // Mock platform availability and permissions
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockSamsungHealthManager.isAvailable() } returns true
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns true
        coEvery { mockGarminConnectManager.isAuthenticated() } returns true
        
        // Mock platform sync results
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(healthConnectMetrics)
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(samsungHealthMetrics)
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(garminMetrics)
        
        // Mock validation and prioritization
        val allMetrics = healthConnectMetrics + samsungHealthMetrics + garminMetrics
        allMetrics.forEach { metric ->
            every { mockHealthDataValidator.validateHealthMetric(metric) } returns ValidationResult(
                isValid = true,
                errors = emptyList(),
                warnings = emptyList(),
                confidence = 1.0f
            )
            every { mockHealthDataValidator.sanitizeHealthMetric(metric) } returns metric
        }
        
        coEvery { mockHealthMetricDao.getAllMetricsForUser(userId) } returns emptyList()
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(allMetrics) } returns allMetrics
        every { mockHealthDataConflictResolver.resolveConflicts(userId, allMetrics) } returns allMetrics
        
        // Mock cache and sync operations
        coEvery { mockOfflineCacheManager.cacheHealthMetrics(userId, allMetrics) } returns Unit
        coEvery { mockSyncService.markForUpload(any(), "HealthMetric") } returns Unit
        coEvery { mockSyncService.performFullSync() } returns SyncResult.Success
        coEvery { mockHealthMetricDao.insertHealthMetrics(allMetrics) } returns Unit
        
        // Act
        val result = healthDataSyncManager.performBidirectionalSync(userId, syncTimeRange)
        
        // Assert
        assertTrue("Sync should be successful", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should sync all metrics", 3, successResult.syncedMetricsCount)
        assertEquals("Should have 3 platform statuses", 3, successResult.platformSyncStatuses.size)
        
        // Verify all platforms were called
        verify { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) }
        verify { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) }
        verify { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) }
        
        // Verify validation and prioritization
        allMetrics.forEach { metric ->
            verify { mockHealthDataValidator.validateHealthMetric(metric) }
            verify { mockHealthDataValidator.sanitizeHealthMetric(metric) }
        }
        verify { mockHealthDataPrioritizer.prioritizeAndDeduplicate(allMetrics) }
        verify { mockHealthDataConflictResolver.resolveConflicts(userId, allMetrics) }
        
        // Verify sync operations
        coVerify { mockSyncService.performFullSync() }
        coVerify { mockHealthMetricDao.insertHealthMetrics(allMetrics) }
    }
    
    @Test
    fun `performBidirectionalSync should handle platform unavailability gracefully`() = runTest {
        // Arrange
        val userId = "test-user-id"
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val syncTimeRange = startTime to endTime
        
        // Mock only Health Connect as available
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockSamsungHealthManager.isAvailable() } returns false
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns false
        coEvery { mockGarminConnectManager.isAuthenticated() } returns false
        
        val healthConnectMetrics = listOf(
            createTestHealthMetric(userId, HealthMetricType.STEPS, 10000.0, DataSource.HEALTH_CONNECT)
        )
        
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(healthConnectMetrics)
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        
        // Mock validation and other operations
        healthConnectMetrics.forEach { metric ->
            every { mockHealthDataValidator.validateHealthMetric(metric) } returns ValidationResult(
                isValid = true,
                errors = emptyList(),
                warnings = emptyList(),
                confidence = 1.0f
            )
            every { mockHealthDataValidator.sanitizeHealthMetric(metric) } returns metric
        }
        
        coEvery { mockHealthMetricDao.getAllMetricsForUser(userId) } returns emptyList()
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(healthConnectMetrics) } returns healthConnectMetrics
        every { mockHealthDataConflictResolver.resolveConflicts(userId, healthConnectMetrics) } returns healthConnectMetrics
        
        coEvery { mockOfflineCacheManager.cacheHealthMetrics(userId, healthConnectMetrics) } returns Unit
        coEvery { mockSyncService.markForUpload(any(), "HealthMetric") } returns Unit
        coEvery { mockSyncService.performFullSync() } returns SyncResult.Success
        coEvery { mockHealthMetricDao.insertHealthMetrics(healthConnectMetrics) } returns Unit
        
        // Act
        val result = healthDataSyncManager.performBidirectionalSync(userId, syncTimeRange)
        
        // Assert
        assertTrue("Sync should be successful even with unavailable platforms", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should sync only Health Connect metrics", 1, successResult.syncedMetricsCount)
        
        // Verify platform statuses reflect availability
        val healthConnectStatus = successResult.platformSyncStatuses.find { it.platform == "Health Connect" }
        val samsungHealthStatus = successResult.platformSyncStatuses.find { it.platform == "Samsung Health" }
        val garminStatus = successResult.platformSyncStatuses.find { it.platform == "Garmin Connect" }
        
        assertNotNull("Health Connect status should be present", healthConnectStatus)
        assertNotNull("Samsung Health status should be present", samsungHealthStatus)
        assertNotNull("Garmin status should be present", garminStatus)
        
        assertEquals("Health Connect should be synced", SyncState.SYNCED, healthConnectStatus?.syncStatus)
        assertEquals("Samsung Health should be failed", SyncState.FAILED, samsungHealthStatus?.syncStatus)
        assertEquals("Garmin should be failed", SyncState.FAILED, garminStatus?.syncStatus)
    }
    
    @Test
    fun `performBidirectionalSync should handle validation errors`() = runTest {
        // Arrange
        val userId = "test-user-id"
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val syncTimeRange = startTime to endTime
        
        val validMetric = createTestHealthMetric(userId, HealthMetricType.STEPS, 10000.0, DataSource.HEALTH_CONNECT)
        val invalidMetric = createTestHealthMetric(userId, HealthMetricType.HEART_RATE, -50.0, DataSource.HEALTH_CONNECT) // Invalid heart rate
        
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockSamsungHealthManager.isAvailable() } returns false
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns false
        coEvery { mockGarminConnectManager.isAuthenticated() } returns false
        
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(validMetric, invalidMetric))
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        
        // Mock validation - valid metric passes, invalid metric fails
        every { mockHealthDataValidator.validateHealthMetric(validMetric) } returns ValidationResult(
            isValid = true,
            errors = emptyList(),
            warnings = emptyList(),
            confidence = 1.0f
        )
        every { mockHealthDataValidator.validateHealthMetric(invalidMetric) } returns ValidationResult(
            isValid = false,
            errors = listOf("Heart rate cannot be negative"),
            warnings = emptyList(),
            confidence = 0.0f
        )
        every { mockHealthDataValidator.sanitizeHealthMetric(validMetric) } returns validMetric
        
        coEvery { mockHealthMetricDao.getAllMetricsForUser(userId) } returns emptyList()
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(listOf(validMetric)) } returns listOf(validMetric)
        every { mockHealthDataConflictResolver.resolveConflicts(userId, listOf(validMetric)) } returns listOf(validMetric)
        
        coEvery { mockOfflineCacheManager.cacheHealthMetrics(userId, listOf(validMetric)) } returns Unit
        coEvery { mockSyncService.markForUpload(any(), "HealthMetric") } returns Unit
        coEvery { mockSyncService.performFullSync() } returns SyncResult.Success
        coEvery { mockHealthMetricDao.insertHealthMetrics(listOf(validMetric)) } returns Unit
        
        // Act
        val result = healthDataSyncManager.performBidirectionalSync(userId, syncTimeRange)
        
        // Assert
        assertTrue("Sync should be successful with valid metrics", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should sync only valid metrics", 1, successResult.syncedMetricsCount)
        
        // Verify only valid metric was processed
        verify { mockHealthDataValidator.validateHealthMetric(validMetric) }
        verify { mockHealthDataValidator.validateHealthMetric(invalidMetric) }
        verify { mockHealthDataValidator.sanitizeHealthMetric(validMetric) }
        verify(exactly = 0) { mockHealthDataValidator.sanitizeHealthMetric(invalidMetric) }
    }
    
    @Test
    fun `forceSyncForMetricTypes should sync only specified metric types`() = runTest {
        // Arrange
        val userId = "test-user-id"
        val startTime = Instant.now().minusSeconds(3600)
        val endTime = Instant.now()
        val timeRange = startTime to endTime
        val metricTypes = setOf(HealthMetricType.STEPS, HealthMetricType.HEART_RATE)
        
        val existingMetrics = listOf(
            createTestHealthMetric(userId, HealthMetricType.STEPS, 8000.0, DataSource.HEALTH_CONNECT),
            createTestHealthMetric(userId, HealthMetricType.WEIGHT, 70.0, DataSource.HEALTH_CONNECT)
        )
        
        coEvery { mockHealthMetricDao.getAllMetricsForUser(userId) } returns existingMetrics
        
        // Mock the bidirectional sync to return new metrics
        val newMetrics = listOf(
            createTestHealthMetric(userId, HealthMetricType.STEPS, 12000.0, DataSource.HEALTH_CONNECT),
            createTestHealthMetric(userId, HealthMetricType.HEART_RATE, 80.0, DataSource.SAMSUNG_HEALTH)
        )
        
        // Setup mocks for the sync process
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockSamsungHealthManager.isAvailable() } returns true
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns true
        coEvery { mockGarminConnectManager.isAuthenticated() } returns false
        
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(newMetrics[0]))
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(newMetrics[1]))
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        
        newMetrics.forEach { metric ->
            every { mockHealthDataValidator.validateHealthMetric(metric) } returns ValidationResult(
                isValid = true,
                errors = emptyList(),
                warnings = emptyList(),
                confidence = 1.0f
            )
            every { mockHealthDataValidator.sanitizeHealthMetric(metric) } returns metric
        }
        
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(any()) } returns newMetrics
        every { mockHealthDataConflictResolver.resolveConflicts(userId, newMetrics) } returns newMetrics
        
        coEvery { mockOfflineCacheManager.cacheHealthMetrics(userId, newMetrics) } returns Unit
        coEvery { mockSyncService.markForUpload(any(), "HealthMetric") } returns Unit
        coEvery { mockSyncService.performFullSync() } returns SyncResult.Success
        coEvery { mockHealthMetricDao.insertHealthMetrics(newMetrics) } returns Unit
        
        // Act
        val result = healthDataSyncManager.forceSyncForMetricTypes(userId, metricTypes, timeRange)
        
        // Assert
        assertTrue("Force sync should be successful", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertTrue("Should have synced new metrics", successResult.syncedMetricsCount > 0)
        assertTrue("Summary should mention force sync", successResult.summary.contains("Force sync completed"))
        
        // Verify that existing metrics were considered
        coVerify { mockHealthMetricDao.getAllMetricsForUser(userId) }
    }
    
    private fun createTestHealthMetric(
        userId: String,
        type: HealthMetricType,
        value: Double,
        source: DataSource
    ): HealthMetric {
        return HealthMetric(
            id = UUID.randomUUID().toString(),
            userId = userId,
            type = type,
            value = value,
            unit = when (type) {
                HealthMetricType.STEPS -> "steps"
                HealthMetricType.HEART_RATE -> "bpm"
                HealthMetricType.WEIGHT -> "kg"
                HealthMetricType.HRV -> "ms"
                else -> "unit"
            },
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = source,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = source == DataSource.MANUAL_ENTRY
        )
    }
}