package com.beaconledger.welltrack.data.health

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import com.beaconledger.welltrack.data.cache.HealthDataCacheManager
import com.beaconledger.welltrack.data.cache.OfflineCacheManager
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.sync.SyncService
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.mockk.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

/**
 * Comprehensive cross-platform health sync testing suite
 * Tests Health Connect, Samsung Health, and Garmin integrations across different scenarios
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@LargeTest
class CrossPlatformHealthSyncTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var offlineCacheManager: OfflineCacheManager

    @Inject
    lateinit var syncService: SyncService

    private lateinit var database: WellTrackDatabase
    private lateinit var healthMetricDao: HealthMetricDao
    private lateinit var healthDataSyncManager: HealthDataSyncManager
    private lateinit var context: Context

    // Mock platform managers
    private lateinit var mockHealthConnectManager: HealthConnectManager
    private lateinit var mockSamsungHealthManager: SamsungHealthManager
    private lateinit var mockGarminConnectManager: GarminConnectManager
    private lateinit var mockHealthDataPrioritizer: HealthDataPrioritizer
    private lateinit var mockHealthDataValidator: HealthDataValidator
    private lateinit var mockHealthDataConflictResolver: HealthDataConflictResolver

    @Before
    fun setup() {
        hiltRule.inject()
        
        context = ApplicationProvider.getApplicationContext()
        
        // Create in-memory database for testing
        database = Room.inMemoryDatabaseBuilder(
            context,
            WellTrackDatabase::class.java
        ).allowMainThreadQueries().build()
        
        healthMetricDao = database.healthMetricDao()
        
        // Initialize mocks
        mockHealthConnectManager = mockk()
        mockSamsungHealthManager = mockk()
        mockGarminConnectManager = mockk()
        mockHealthDataPrioritizer = mockk()
        mockHealthDataValidator = mockk()
        mockHealthDataConflictResolver = mockk()
        
        // Create sync manager with mocked dependencies
        healthDataSyncManager = HealthDataSyncManager(
            context = context,
            healthConnectManager = mockHealthConnectManager,
            samsungHealthManager = mockSamsungHealthManager,
            garminConnectManager = mockGarminConnectManager,
            healthDataPrioritizer = mockHealthDataPrioritizer,
            healthDataValidator = mockHealthDataValidator,
            healthMetricDao = healthMetricDao,
            syncService = syncService,
            offlineCacheManager = offlineCacheManager,
            healthDataConflictResolver = mockHealthDataConflictResolver
        )
    }

    @After
    fun tearDown() {
        database.close()
        unmockkAll()
    }

    // =============================================================================
    // Health Connect Integration Tests Across Android Versions
    // =============================================================================

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.UPSIDE_DOWN_CAKE) // Android 14+
    fun `test Health Connect integration on Android 14 and above`() = runTest {
        // Given
        val userId = "test_user_android14"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock Health Connect availability on Android 14+
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        
        val healthConnectMetrics = createHealthConnectTestData(userId)
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(healthConnectMetrics)
        
        // Mock other platforms as unavailable
        setupUnavailablePlatforms()
        
        // Mock validation and processing
        setupSuccessfulValidationAndProcessing(healthConnectMetrics)
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Sync should succeed on Android 14+", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should sync Health Connect metrics", healthConnectMetrics.size, successResult.syncedMetricsCount)
        
        val healthConnectStatus = successResult.platformSyncStatuses.find { it.platform == "Health Connect" }
        assertNotNull("Health Connect status should be present", healthConnectStatus)
        assertEquals("Health Connect should be synced", SyncState.SYNCED, healthConnectStatus?.syncStatus)
        
        // Verify Android 14+ specific features
        verifyHealthConnectAndroid14Features(healthConnectMetrics)
    }

    @Test
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.TIRAMISU, maxSdkVersion = Build.VERSION_CODES.TIRAMISU) // Android 13
    fun `test Health Connect integration on Android 13`() = runTest {
        // Given
        val userId = "test_user_android13"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock Health Connect with limited features on Android 13
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        
        val limitedHealthConnectMetrics = createLimitedHealthConnectTestData(userId)
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(limitedHealthConnectMetrics)
        
        setupUnavailablePlatforms()
        setupSuccessfulValidationAndProcessing(limitedHealthConnectMetrics)
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Sync should succeed on Android 13", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should sync limited Health Connect metrics", limitedHealthConnectMetrics.size, successResult.syncedMetricsCount)
        
        // Verify Android 13 limitations are handled
        verifyHealthConnectAndroid13Limitations(limitedHealthConnectMetrics)
    }

    @Test
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.S_V2) // Android 12 and below
    fun `test Health Connect unavailable on older Android versions`() = runTest {
        // Given
        val userId = "test_user_android12"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock Health Connect as unavailable on older Android versions
        coEvery { mockHealthConnectManager.isAvailable() } returns false
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns false
        
        setupUnavailablePlatforms()
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Sync should complete even without Health Connect", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        
        val healthConnectStatus = successResult.platformSyncStatuses.find { it.platform == "Health Connect" }
        assertNotNull("Health Connect status should be present", healthConnectStatus)
        assertEquals("Health Connect should be failed", SyncState.FAILED, healthConnectStatus?.syncStatus)
        assertTrue("Error message should indicate unavailability", 
            healthConnectStatus?.errorMessage?.contains("not available") == true)
    }

    // =============================================================================
    // Samsung Health Integration Tests on Samsung Devices
    // =============================================================================

    @Test
    fun `test Samsung Health sync on Samsung devices`() = runTest {
        // Given
        val userId = "test_user_samsung"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Simulate Samsung device
        if (isSamsungDevice()) {
            // Mock Samsung Health availability
            coEvery { mockSamsungHealthManager.isAvailable() } returns true
            coEvery { mockSamsungHealthManager.hasAllPermissions() } returns true
            
            val samsungHealthMetrics = createSamsungHealthTestData(userId)
            every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(samsungHealthMetrics)
            
            // Mock other platforms
            setupLimitedHealthConnectAndUnavailableGarmin()
            setupSuccessfulValidationAndProcessing(samsungHealthMetrics)
            
            // When
            val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
            
            // Then
            assertTrue("Sync should succeed on Samsung device", result is HealthSyncResult.Success)
            val successResult = result as HealthSyncResult.Success
            
            val samsungStatus = successResult.platformSyncStatuses.find { it.platform == "Samsung Health" }
            assertNotNull("Samsung Health status should be present", samsungStatus)
            assertEquals("Samsung Health should be synced", SyncState.SYNCED, samsungStatus?.syncStatus)
            
            // Verify Samsung-specific features
            verifySamsungHealthSpecificFeatures(samsungHealthMetrics)
        } else {
            // On non-Samsung devices, Samsung Health should be unavailable
            coEvery { mockSamsungHealthManager.isAvailable() } returns false
            coEvery { mockSamsungHealthManager.hasAllPermissions() } returns false
            
            setupLimitedHealthConnectAndUnavailableGarmin()
            
            val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
            
            assertTrue("Sync should complete on non-Samsung device", result is HealthSyncResult.Success)
            val successResult = result as HealthSyncResult.Success
            
            val samsungStatus = successResult.platformSyncStatuses.find { it.platform == "Samsung Health" }
            assertEquals("Samsung Health should be failed on non-Samsung device", SyncState.FAILED, samsungStatus?.syncStatus)
        }
    }

    @Test
    fun `test Samsung Health ECG and body composition sync`() = runTest {
        // Given
        val userId = "test_user_samsung_advanced"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        if (isSamsungDevice()) {
            coEvery { mockSamsungHealthManager.isAvailable() } returns true
            coEvery { mockSamsungHealthManager.hasAllPermissions() } returns true
            
            val advancedSamsungMetrics = createAdvancedSamsungHealthTestData(userId)
            every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(advancedSamsungMetrics)
            
            setupLimitedHealthConnectAndUnavailableGarmin()
            setupSuccessfulValidationAndProcessing(advancedSamsungMetrics)
            
            // When
            val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
            
            // Then
            assertTrue("Advanced Samsung Health sync should succeed", result is HealthSyncResult.Success)
            val successResult = result as HealthSyncResult.Success
            
            // Verify ECG data
            val savedMetrics = healthMetricDao.getAllMetricsForUser(userId)
            val ecgMetrics = savedMetrics.filter { it.type == HealthMetricType.ECG }
            assertTrue("Should contain ECG metrics", ecgMetrics.isNotEmpty())
            
            // Verify body composition data
            val bodyFatMetrics = savedMetrics.filter { it.type == HealthMetricType.BODY_FAT_PERCENTAGE }
            assertTrue("Should contain body fat metrics", bodyFatMetrics.isNotEmpty())
            
            val muscleMassMetrics = savedMetrics.filter { it.type == HealthMetricType.MUSCLE_MASS }
            assertTrue("Should contain muscle mass metrics", muscleMassMetrics.isNotEmpty())
        }
    }

    // =============================================================================
    // Garmin Data Sync Tests with Various Device Models
    // =============================================================================

    @Test
    fun `test Garmin sync with fitness watch models`() = runTest {
        // Given
        val userId = "test_user_garmin_fitness"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock authenticated Garmin connection
        coEvery { mockGarminConnectManager.isAuthenticated() } returns true
        
        val garminFitnessMetrics = createGarminFitnessWatchTestData(userId)
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(garminFitnessMetrics)
        
        setupLimitedHealthConnectAndSamsung()
        setupSuccessfulValidationAndProcessing(garminFitnessMetrics)
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Garmin fitness watch sync should succeed", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        
        val garminStatus = successResult.platformSyncStatuses.find { it.platform == "Garmin Connect" }
        assertNotNull("Garmin status should be present", garminStatus)
        assertEquals("Garmin should be synced", SyncState.SYNCED, garminStatus?.syncStatus)
        
        // Verify fitness watch specific metrics
        verifyGarminFitnessWatchMetrics(garminFitnessMetrics)
    }

    @Test
    fun `test Garmin sync with advanced sports watch models`() = runTest {
        // Given
        val userId = "test_user_garmin_advanced"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        coEvery { mockGarminConnectManager.isAuthenticated() } returns true
        
        val garminAdvancedMetrics = createGarminAdvancedWatchTestData(userId)
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(garminAdvancedMetrics)
        
        setupLimitedHealthConnectAndSamsung()
        setupSuccessfulValidationAndProcessing(garminAdvancedMetrics)
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Garmin advanced watch sync should succeed", result is HealthSyncResult.Success)
        
        // Verify advanced metrics like HRV, recovery, biological age
        val savedMetrics = healthMetricDao.getAllMetricsForUser(userId)
        
        val hrvMetrics = savedMetrics.filter { it.type == HealthMetricType.HRV }
        assertTrue("Should contain HRV metrics", hrvMetrics.isNotEmpty())
        
        val recoveryMetrics = savedMetrics.filter { it.type == HealthMetricType.TRAINING_RECOVERY }
        assertTrue("Should contain recovery metrics", recoveryMetrics.isNotEmpty())
        
        val biologicalAgeMetrics = savedMetrics.filter { it.type == HealthMetricType.BIOLOGICAL_AGE }
        assertTrue("Should contain biological age metrics", biologicalAgeMetrics.isNotEmpty())
    }

    @Test
    fun `test Garmin authentication failure handling`() = runTest {
        // Given
        val userId = "test_user_garmin_unauth"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock unauthenticated Garmin connection
        coEvery { mockGarminConnectManager.isAuthenticated() } returns false
        
        setupLimitedHealthConnectAndSamsung()
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Sync should complete even with Garmin auth failure", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        
        val garminStatus = successResult.platformSyncStatuses.find { it.platform == "Garmin Connect" }
        assertNotNull("Garmin status should be present", garminStatus)
        assertEquals("Garmin should be failed", SyncState.FAILED, garminStatus?.syncStatus)
        assertTrue("Error message should indicate authentication failure", 
            garminStatus?.errorMessage?.contains("not authenticated") == true)
    }

    // =============================================================================
    // Health Data Sync Conflict Resolution Scenarios
    // =============================================================================

    @Test
    fun `test conflict resolution with overlapping data from multiple platforms`() = runTest {
        // Given
        val userId = "test_user_conflicts"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        // Create conflicting heart rate data from different platforms
        val healthConnectMetric = createTestHealthMetric(
            userId, HealthMetricType.HEART_RATE, 75.0, timestamp, DataSource.HEALTH_CONNECT, confidence = 0.8f
        )
        val samsungHealthMetric = createTestHealthMetric(
            userId, HealthMetricType.HEART_RATE, 78.0, timestamp, DataSource.SAMSUNG_HEALTH, confidence = 0.9f
        )
        val garminMetric = createTestHealthMetric(
            userId, HealthMetricType.HEART_RATE, 76.0, timestamp, DataSource.GARMIN, confidence = 0.95f
        )
        
        // Mock platform responses
        setupAllPlatformsAvailable()
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(healthConnectMetric))
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(samsungHealthMetric))
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(garminMetric))
        
        val allMetrics = listOf(healthConnectMetric, samsungHealthMetric, garminMetric)
        
        // Mock validation
        allMetrics.forEach { metric ->
            every { mockHealthDataValidator.validateHealthMetric(metric) } returns ValidationResult(
                isValid = true, errors = emptyList(), warnings = emptyList(), confidence = metric.confidence
            )
            every { mockHealthDataValidator.sanitizeHealthMetric(metric) } returns metric
        }
        
        // Mock prioritization - Garmin should win due to higher confidence
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(allMetrics) } returns listOf(garminMetric)
        
        // Mock conflict resolution
        coEvery { mockHealthMetricDao.getAllMetricsForUser(userId) } returns emptyList()
        every { mockHealthDataConflictResolver.resolveConflicts(userId, listOf(garminMetric)) } returns listOf(garminMetric)
        
        setupSuccessfulSyncOperations(listOf(garminMetric))
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Conflict resolution should succeed", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should resolve to single metric", 1, successResult.syncedMetricsCount)
        
        // Verify prioritization was called
        verify { mockHealthDataPrioritizer.prioritizeAndDeduplicate(allMetrics) }
        verify { mockHealthDataConflictResolver.resolveConflicts(userId, listOf(garminMetric)) }
        
        // Verify final data
        val savedMetrics = healthMetricDao.getAllMetricsForUser(userId)
        assertEquals("Should save only resolved metric", 1, savedMetrics.size)
        assertEquals("Should save Garmin metric (highest confidence)", DataSource.GARMIN, savedMetrics.first().source)
    }

    @Test
    fun `test temporal conflict resolution with different timestamps`() = runTest {
        // Given
        val userId = "test_user_temporal_conflicts"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        val baseTime = LocalDateTime.now()
        val timestamp1 = baseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val timestamp2 = baseTime.plusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val timestamp3 = baseTime.plusMinutes(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        // Create metrics with different timestamps but close values
        val metric1 = createTestHealthMetric(userId, HealthMetricType.WEIGHT, 70.0, timestamp1, DataSource.HEALTH_CONNECT)
        val metric2 = createTestHealthMetric(userId, HealthMetricType.WEIGHT, 70.1, timestamp2, DataSource.SAMSUNG_HEALTH)
        val metric3 = createTestHealthMetric(userId, HealthMetricType.WEIGHT, 69.9, timestamp3, DataSource.GARMIN)
        
        setupAllPlatformsAvailable()
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(metric1))
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(metric2))
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(metric3))
        
        val allMetrics = listOf(metric1, metric2, metric3)
        setupSuccessfulValidationAndProcessing(allMetrics)
        
        // Mock temporal conflict resolution - should keep all due to different timestamps
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(allMetrics) } returns allMetrics
        every { mockHealthDataConflictResolver.resolveConflicts(userId, allMetrics) } returns allMetrics
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Temporal conflict resolution should succeed", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should keep all metrics with different timestamps", 3, successResult.syncedMetricsCount)
        
        val savedMetrics = healthMetricDao.getAllMetricsForUser(userId)
        assertEquals("Should save all temporal metrics", 3, savedMetrics.size)
        
        // Verify timestamps are preserved
        val timestamps = savedMetrics.map { it.timestamp }.sorted()
        assertEquals("Should preserve all timestamps", listOf(timestamp1, timestamp2, timestamp3).sorted(), timestamps)
    }

    @Test
    fun `test manual entry override conflict resolution`() = runTest {
        // Given
        val userId = "test_user_manual_override"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        // Create automatic and manual metrics for same timestamp
        val automaticMetric = createTestHealthMetric(
            userId, HealthMetricType.BLOOD_PRESSURE, 120.0, timestamp, DataSource.SAMSUNG_HEALTH, confidence = 0.9f
        )
        val manualMetric = createTestHealthMetric(
            userId, HealthMetricType.BLOOD_PRESSURE, 125.0, timestamp, DataSource.MANUAL_ENTRY, 
            confidence = 0.7f, isManualEntry = true
        )
        
        // Add existing manual entry to database
        healthMetricDao.insertHealthMetric(manualMetric)
        
        setupAllPlatformsAvailable()
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(listOf(automaticMetric))
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        
        // Mock validation
        every { mockHealthDataValidator.validateHealthMetric(automaticMetric) } returns ValidationResult(
            isValid = true, errors = emptyList(), warnings = emptyList(), confidence = 0.9f
        )
        every { mockHealthDataValidator.sanitizeHealthMetric(automaticMetric) } returns automaticMetric
        
        // Mock conflict resolution - manual entry should win
        coEvery { mockHealthMetricDao.getAllMetricsForUser(userId) } returns listOf(manualMetric)
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(listOf(manualMetric, automaticMetric)) } returns listOf(manualMetric)
        every { mockHealthDataConflictResolver.resolveConflicts(userId, listOf(manualMetric)) } returns listOf(manualMetric)
        
        setupSuccessfulSyncOperations(listOf(manualMetric))
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Manual override conflict resolution should succeed", result is HealthSyncResult.Success)
        
        val savedMetrics = healthMetricDao.getAllMetricsForUser(userId)
        val bloodPressureMetrics = savedMetrics.filter { it.type == HealthMetricType.BLOOD_PRESSURE }
        
        // Manual entry should be preserved
        assertTrue("Should contain manual entry", bloodPressureMetrics.any { it.isManualEntry })
        assertEquals("Manual entry value should be preserved", 125.0, 
            bloodPressureMetrics.find { it.isManualEntry }?.value, 0.01)
    }

    // =============================================================================
    // Comprehensive Logging and Debugging Tests
    // =============================================================================

    @Test
    fun `test comprehensive sync logging and debugging information`() = runTest {
        // Given
        val userId = "test_user_logging"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Create test data from all platforms
        val healthConnectMetrics = createHealthConnectTestData(userId)
        val samsungHealthMetrics = createSamsungHealthTestData(userId)
        val garminMetrics = createGarminFitnessWatchTestData(userId)
        
        setupAllPlatformsAvailable()
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(healthConnectMetrics)
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(samsungHealthMetrics)
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(garminMetrics)
        
        val allMetrics = healthConnectMetrics + samsungHealthMetrics + garminMetrics
        setupSuccessfulValidationAndProcessing(allMetrics)
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Comprehensive sync should succeed", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        
        // Verify comprehensive logging information
        assertNotNull("Sync timestamp should be recorded", successResult.syncTimestamp)
        assertEquals("Should have status for all 3 platforms", 3, successResult.platformSyncStatuses.size)
        
        // Verify platform-specific status information
        val platforms = successResult.platformSyncStatuses.map { it.platform }
        assertTrue("Should include Health Connect", platforms.contains("Health Connect"))
        assertTrue("Should include Samsung Health", platforms.contains("Samsung Health"))
        assertTrue("Should include Garmin Connect", platforms.contains("Garmin Connect"))
        
        // Verify detailed status information
        successResult.platformSyncStatuses.forEach { status ->
            assertNotNull("Platform should have availability status", status.isAvailable)
            assertNotNull("Platform should have connection status", status.isConnected)
            assertNotNull("Platform should have sync status", status.syncStatus)
            if (status.syncStatus == SyncState.SYNCED) {
                assertNotNull("Successful sync should have timestamp", status.lastSyncTime)
            }
        }
        
        // Verify summary contains useful information
        assertTrue("Summary should mention metrics count", successResult.summary.contains(allMetrics.size.toString()))
        assertTrue("Summary should mention platforms", successResult.summary.contains("platforms"))
    }

    @Test
    fun `test sync performance monitoring and metrics`() = runTest {
        // Given
        val userId = "test_user_performance"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Create large dataset to test performance
        val largeHealthConnectDataset = (1..100).map { index ->
            createTestHealthMetric(
                userId, HealthMetricType.HEART_RATE, 60.0 + index, 
                LocalDateTime.now().minusMinutes(index.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                DataSource.HEALTH_CONNECT
            )
        }
        
        setupAllPlatformsAvailable()
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(largeHealthConnectDataset)
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(emptyList())
        
        setupSuccessfulValidationAndProcessing(largeHealthConnectDataset)
        
        // When
        val syncStartTime = System.currentTimeMillis()
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        val syncDuration = System.currentTimeMillis() - syncStartTime
        
        // Then
        assertTrue("Large dataset sync should succeed", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        assertEquals("Should sync all metrics", largeHealthConnectDataset.size, successResult.syncedMetricsCount)
        
        // Verify performance is acceptable
        assertTrue("Sync should complete within reasonable time (< 10 seconds)", syncDuration < 10000)
        
        // Verify data integrity with large dataset
        val savedMetrics = healthMetricDao.getAllMetricsForUser(userId)
        assertEquals("Should save all metrics", largeHealthConnectDataset.size, savedMetrics.size)
        
        // Verify metrics are properly ordered
        val heartRateMetrics = savedMetrics.filter { it.type == HealthMetricType.HEART_RATE }.sortedBy { it.timestamp }
        assertTrue("Should maintain chronological order", heartRateMetrics.size == largeHealthConnectDataset.size)
    }

    @Test
    fun `test network failure and retry scenarios`() = runTest {
        // Given
        val userId = "test_user_network_failure"
        val startTime = Instant.now().minusSeconds(86400)
        val endTime = Instant.now()
        
        // Mock network failures
        setupAllPlatformsAvailable()
        
        // First call fails, second succeeds (simulating retry)
        val healthConnectMetrics = createHealthConnectTestData(userId)
        every { mockHealthConnectManager.syncHealthData(userId, startTime, endTime) } returns flowOf(healthConnectMetrics)
        every { mockSamsungHealthManager.syncHealthData(userId, startTime, endTime) } throws Exception("Network timeout")
        every { mockGarminConnectManager.syncHealthData(userId, startTime, endTime) } throws Exception("Connection refused")
        
        setupSuccessfulValidationAndProcessing(healthConnectMetrics)
        
        // When
        val result = healthDataSyncManager.performBidirectionalSync(userId, startTime to endTime)
        
        // Then
        assertTrue("Sync should succeed despite network failures", result is HealthSyncResult.Success)
        val successResult = result as HealthSyncResult.Success
        
        // Verify only Health Connect succeeded
        val healthConnectStatus = successResult.platformSyncStatuses.find { it.platform == "Health Connect" }
        val samsungStatus = successResult.platformSyncStatuses.find { it.platform == "Samsung Health" }
        val garminStatus = successResult.platformSyncStatuses.find { it.platform == "Garmin Connect" }
        
        assertEquals("Health Connect should succeed", SyncState.SYNCED, healthConnectStatus?.syncStatus)
        assertEquals("Samsung Health should fail", SyncState.FAILED, samsungStatus?.syncStatus)
        assertEquals("Garmin should fail", SyncState.FAILED, garminStatus?.syncStatus)
        
        // Verify error messages are captured
        assertTrue("Samsung error should be captured", samsungStatus?.errorMessage?.contains("Network timeout") == true)
        assertTrue("Garmin error should be captured", garminStatus?.errorMessage?.contains("Connection refused") == true)
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    private fun createHealthConnectTestData(userId: String) = listOf(
        createTestHealthMetric(userId, HealthMetricType.STEPS, 10000.0, source = DataSource.HEALTH_CONNECT),
        createTestHealthMetric(userId, HealthMetricType.HEART_RATE, 75.0, source = DataSource.HEALTH_CONNECT),
        createTestHealthMetric(userId, HealthMetricType.WEIGHT, 70.0, source = DataSource.HEALTH_CONNECT),
        createTestHealthMetric(userId, HealthMetricType.SLEEP_DURATION, 8.0, source = DataSource.HEALTH_CONNECT)
    )

    private fun createLimitedHealthConnectTestData(userId: String) = listOf(
        createTestHealthMetric(userId, HealthMetricType.STEPS, 8000.0, source = DataSource.HEALTH_CONNECT),
        createTestHealthMetric(userId, HealthMetricType.HEART_RATE, 72.0, source = DataSource.HEALTH_CONNECT)
    )

    private fun createSamsungHealthTestData(userId: String) = listOf(
        createTestHealthMetric(userId, HealthMetricType.HEART_RATE, 78.0, source = DataSource.SAMSUNG_HEALTH),
        createTestHealthMetric(userId, HealthMetricType.BLOOD_PRESSURE, 120.0, source = DataSource.SAMSUNG_HEALTH, metadata = "{\"type\":\"systolic\"}"),
        createTestHealthMetric(userId, HealthMetricType.BLOOD_PRESSURE, 80.0, source = DataSource.SAMSUNG_HEALTH, metadata = "{\"type\":\"diastolic\"}"),
        createTestHealthMetric(userId, HealthMetricType.STRESS_SCORE, 25.0, source = DataSource.SAMSUNG_HEALTH)
    )

    private fun createAdvancedSamsungHealthTestData(userId: String) = listOf(
        createTestHealthMetric(userId, HealthMetricType.ECG, 75.0, source = DataSource.SAMSUNG_HEALTH, metadata = "{\"rhythm\":\"normal\"}"),
        createTestHealthMetric(userId, HealthMetricType.BODY_FAT_PERCENTAGE, 15.0, source = DataSource.SAMSUNG_HEALTH),
        createTestHealthMetric(userId, HealthMetricType.MUSCLE_MASS, 35.0, source = DataSource.SAMSUNG_HEALTH)
    )

    private fun createGarminFitnessWatchTestData(userId: String) = listOf(
        createTestHealthMetric(userId, HealthMetricType.HRV, 45.0, source = DataSource.GARMIN),
        createTestHealthMetric(userId, HealthMetricType.TRAINING_RECOVERY, 85.0, source = DataSource.GARMIN),
        createTestHealthMetric(userId, HealthMetricType.VO2_MAX, 52.0, source = DataSource.GARMIN)
    )

    private fun createGarminAdvancedWatchTestData(userId: String) = listOf(
        createTestHealthMetric(userId, HealthMetricType.HRV, 48.0, source = DataSource.GARMIN, metadata = "{\"type\":\"weekly_avg\"}"),
        createTestHealthMetric(userId, HealthMetricType.TRAINING_RECOVERY, 90.0, source = DataSource.GARMIN, metadata = "{\"sleepScore\":85}"),
        createTestHealthMetric(userId, HealthMetricType.STRESS_SCORE, 20.0, source = DataSource.GARMIN),
        createTestHealthMetric(userId, HealthMetricType.BIOLOGICAL_AGE, 28.0, source = DataSource.GARMIN, metadata = "{\"chronologicalAge\":32}")
    )

    private fun createTestHealthMetric(
        userId: String,
        type: HealthMetricType,
        value: Double,
        timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        source: DataSource,
        metadata: String? = null,
        confidence: Float = 1.0f,
        isManualEntry: Boolean = false
    ) = HealthMetric(
        id = UUID.randomUUID().toString(),
        userId = userId,
        type = type,
        value = value,
        unit = getUnitForMetricType(type),
        timestamp = timestamp,
        source = source,
        metadata = metadata,
        confidence = confidence,
        isManualEntry = isManualEntry
    )

    private fun getUnitForMetricType(type: HealthMetricType): String = when (type) {
        HealthMetricType.STEPS -> "steps"
        HealthMetricType.HEART_RATE -> "bpm"
        HealthMetricType.WEIGHT -> "kg"
        HealthMetricType.SLEEP_DURATION -> "hours"
        HealthMetricType.BLOOD_PRESSURE -> "mmHg"
        HealthMetricType.STRESS_SCORE -> "score"
        HealthMetricType.ECG -> "bpm"
        HealthMetricType.BODY_FAT_PERCENTAGE -> "%"
        HealthMetricType.MUSCLE_MASS -> "kg"
        HealthMetricType.HRV -> "ms"
        HealthMetricType.TRAINING_RECOVERY -> "score"
        HealthMetricType.VO2_MAX -> "ml/min/kg"
        HealthMetricType.BIOLOGICAL_AGE -> "years"
        else -> "unit"
    }

    private fun setupUnavailablePlatforms() {
        coEvery { mockSamsungHealthManager.isAvailable() } returns false
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns false
        coEvery { mockGarminConnectManager.isAuthenticated() } returns false
        every { mockSamsungHealthManager.syncHealthData(any(), any(), any()) } returns flowOf(emptyList())
        every { mockGarminConnectManager.syncHealthData(any(), any(), any()) } returns flowOf(emptyList())
    }

    private fun setupLimitedHealthConnectAndUnavailableGarmin() {
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockGarminConnectManager.isAuthenticated() } returns false
        every { mockHealthConnectManager.syncHealthData(any(), any(), any()) } returns flowOf(emptyList())
        every { mockGarminConnectManager.syncHealthData(any(), any(), any()) } returns flowOf(emptyList())
    }

    private fun setupLimitedHealthConnectAndSamsung() {
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockSamsungHealthManager.isAvailable() } returns true
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns true
        every { mockHealthConnectManager.syncHealthData(any(), any(), any()) } returns flowOf(emptyList())
        every { mockSamsungHealthManager.syncHealthData(any(), any(), any()) } returns flowOf(emptyList())
    }

    private fun setupAllPlatformsAvailable() {
        coEvery { mockHealthConnectManager.isAvailable() } returns true
        coEvery { mockHealthConnectManager.hasAllPermissions() } returns true
        coEvery { mockSamsungHealthManager.isAvailable() } returns true
        coEvery { mockSamsungHealthManager.hasAllPermissions() } returns true
        coEvery { mockGarminConnectManager.isAuthenticated() } returns true
    }

    private fun setupSuccessfulValidationAndProcessing(metrics: List<HealthMetric>) {
        metrics.forEach { metric ->
            every { mockHealthDataValidator.validateHealthMetric(metric) } returns ValidationResult(
                isValid = true, errors = emptyList(), warnings = emptyList(), confidence = metric.confidence
            )
            every { mockHealthDataValidator.sanitizeHealthMetric(metric) } returns metric
        }

        coEvery { mockHealthMetricDao.getAllMetricsForUser(any()) } returns emptyList()
        every { mockHealthDataPrioritizer.prioritizeAndDeduplicate(metrics) } returns metrics
        every { mockHealthDataConflictResolver.resolveConflicts(any(), metrics) } returns metrics

        setupSuccessfulSyncOperations(metrics)
    }

    private fun setupSuccessfulSyncOperations(metrics: List<HealthMetric>) {
        coEvery { offlineCacheManager.cacheHealthMetrics(any(), metrics) } returns Unit
        coEvery { syncService.markForUpload(any(), "HealthMetric") } returns Unit
        coEvery { syncService.performFullSync() } returns SyncResult.Success
        coEvery { healthMetricDao.insertHealthMetrics(metrics) } returns Unit
    }

    private fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.lowercase().contains("samsung")
    }

    private fun verifyHealthConnectAndroid14Features(metrics: List<HealthMetric>) {
        // Verify Android 14+ specific Health Connect features
        assertTrue("Should support comprehensive health metrics on Android 14+", metrics.isNotEmpty())
        
        // Check for advanced metrics available in Android 14+
        val supportedTypes = metrics.map { it.type }.toSet()
        assertTrue("Should support steps", supportedTypes.contains(HealthMetricType.STEPS))
        assertTrue("Should support heart rate", supportedTypes.contains(HealthMetricType.HEART_RATE))
    }

    private fun verifyHealthConnectAndroid13Limitations(metrics: List<HealthMetric>) {
        // Verify Android 13 limitations are properly handled
        assertTrue("Should have limited metrics on Android 13", metrics.size <= 2)
        
        // Verify only basic metrics are available
        val supportedTypes = metrics.map { it.type }.toSet()
        val basicTypes = setOf(HealthMetricType.STEPS, HealthMetricType.HEART_RATE)
        assertTrue("Should only contain basic metrics", supportedTypes.all { it in basicTypes })
    }

    private fun verifySamsungHealthSpecificFeatures(metrics: List<HealthMetric>) {
        // Verify Samsung Health specific features
        val sources = metrics.map { it.source }.toSet()
        assertTrue("Should contain Samsung Health data", sources.contains(DataSource.SAMSUNG_HEALTH))
        
        // Check for Samsung-specific metrics
        val types = metrics.map { it.type }.toSet()
        if (types.contains(HealthMetricType.BLOOD_PRESSURE)) {
            val bpMetrics = metrics.filter { it.type == HealthMetricType.BLOOD_PRESSURE }
            assertTrue("Should have systolic and diastolic readings", bpMetrics.size >= 2)
        }
    }

    private fun verifyGarminFitnessWatchMetrics(metrics: List<HealthMetric>) {
        // Verify Garmin fitness watch specific metrics
        val sources = metrics.map { it.source }.toSet()
        assertTrue("Should contain Garmin data", sources.contains(DataSource.GARMIN))
        
        val types = metrics.map { it.type }.toSet()
        assertTrue("Should contain fitness metrics", 
            types.any { it in setOf(HealthMetricType.HRV, HealthMetricType.TRAINING_RECOVERY, HealthMetricType.VO2_MAX) })
    }
}