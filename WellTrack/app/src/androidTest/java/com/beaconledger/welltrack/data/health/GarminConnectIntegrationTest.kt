package com.beaconledger.welltrack.data.health

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.data.repository.HealthConnectRepositoryImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
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
import javax.inject.Inject

/**
 * Integration tests for Garmin Connect data import scenarios
 * Tests real database operations and data flow
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class GarminConnectIntegrationTest {

    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var environmentConfig: EnvironmentConfig

    @Inject
    lateinit var secureConfigLoader: SecureConfigLoader

    private lateinit var database: WellTrackDatabase
    private lateinit var healthMetricDao: HealthMetricDao
    private lateinit var garminConnectManager: GarminConnectManager
    private lateinit var healthConnectRepository: HealthConnectRepositoryImpl
    private lateinit var context: Context

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
        
        // Initialize managers
        garminConnectManager = GarminConnectManager(
            context,
            environmentConfig,
            secureConfigLoader
        )
        
        healthConnectRepository = HealthConnectRepositoryImpl(
            healthMetricDao,
            garminConnectManager,
            mockk(), // Samsung Health Manager
            mockk()  // Health Connect Manager
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    // =============================================================================
    // Data Import Integration Tests
    // =============================================================================

    @Test
    fun testGarminDataImportFlow() = runTest {
        // Given
        val userId = "test_user_id"
        val testMetrics = createTestGarminMetrics(userId)
        
        // When - Save metrics to database
        testMetrics.forEach { metric ->
            healthMetricDao.insertHealthMetric(metric)
        }
        
        // Then - Verify data was saved correctly
        val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
        assertEquals("Should save all test metrics", testMetrics.size, savedMetrics.size)
        
        // Verify HRV data
        val hrvMetric = savedMetrics.find { it.type == HealthMetricType.HRV }
        assertNotNull("Should contain HRV metric", hrvMetric)
        assertEquals("HRV source should be Garmin", DataSource.GARMIN, hrvMetric!!.source)
        assertTrue("HRV metadata should contain additional data", hrvMetric.metadata?.isNotEmpty() == true)
        
        // Verify Recovery data
        val recoveryMetric = savedMetrics.find { it.type == HealthMetricType.TRAINING_RECOVERY }
        assertNotNull("Should contain recovery metric", recoveryMetric)
        assertEquals("Recovery source should be Garmin", DataSource.GARMIN, recoveryMetric!!.source)
        
        // Verify Stress data
        val stressMetric = savedMetrics.find { it.type == HealthMetricType.STRESS_SCORE }
        assertNotNull("Should contain stress metric", stressMetric)
        assertEquals("Stress source should be Garmin", DataSource.GARMIN, stressMetric!!.source)
        
        // Verify Biological Age data
        val biologicalAgeMetric = savedMetrics.find { it.type == HealthMetricType.BIOLOGICAL_AGE }
        assertNotNull("Should contain biological age metric", biologicalAgeMetric)
        assertEquals("Biological age source should be Garmin", DataSource.GARMIN, biologicalAgeMetric!!.source)
    }

    @Test
    fun testGarminDataDeduplication() = runTest {
        // Given
        val userId = "test_user_id"
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        // Create duplicate HRV metrics with same timestamp
        val metric1 = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HRV,
            value = 45.5,
            timestamp = timestamp,
            source = DataSource.GARMIN
        )
        
        val metric2 = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HRV,
            value = 46.0, // Slightly different value
            timestamp = timestamp,
            source = DataSource.GARMIN
        )
        
        // When - Save both metrics
        healthMetricDao.insertHealthMetric(metric1)
        healthMetricDao.insertHealthMetric(metric2)
        
        // Then - Verify both are saved (deduplication should happen at sync level)
        val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
        val hrvMetrics = savedMetrics.filter { it.type == HealthMetricType.HRV }
        assertEquals("Should save both metrics (deduplication at sync level)", 2, hrvMetrics.size)
    }

    @Test
    fun testGarminDataPrioritization() = runTest {
        // Given
        val userId = "test_user_id"
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        
        // Create metrics from different sources for same timestamp
        val garminMetric = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            timestamp = timestamp,
            source = DataSource.GARMIN,
            confidence = 0.9f
        )
        
        val healthConnectMetric = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HEART_RATE,
            value = 78.0,
            timestamp = timestamp,
            source = DataSource.HEALTH_CONNECT,
            confidence = 0.8f
        )
        
        val manualMetric = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HEART_RATE,
            value = 80.0,
            timestamp = timestamp,
            source = DataSource.MANUAL_ENTRY,
            confidence = 0.7f,
            isManualEntry = true
        )
        
        // When - Save all metrics
        healthMetricDao.insertHealthMetric(garminMetric)
        healthMetricDao.insertHealthMetric(healthConnectMetric)
        healthMetricDao.insertHealthMetric(manualMetric)
        
        // Then - Verify all are saved with correct confidence scores
        val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
        val heartRateMetrics = savedMetrics.filter { it.type == HealthMetricType.HEART_RATE }
        assertEquals("Should save all heart rate metrics", 3, heartRateMetrics.size)
        
        // Verify confidence scores are preserved
        val garminSaved = heartRateMetrics.find { it.source == DataSource.GARMIN }
        assertEquals("Garmin confidence should be preserved", 0.9f, garminSaved!!.confidence, 0.01f)
        
        val manualSaved = heartRateMetrics.find { it.source == DataSource.MANUAL_ENTRY }
        assertTrue("Manual entry flag should be preserved", manualSaved!!.isManualEntry)
    }

    @Test
    fun testGarminMetadataPreservation() = runTest {
        // Given
        val userId = "test_user_id"
        val complexMetadata = """
            {
                "type": "weekly_avg",
                "lastNightAvg": 42.3,
                "lastNight5MinHigh": 48.7,
                "baseline": {
                    "lowUpper": 35.0,
                    "balancedLower": 36.0,
                    "balancedUpper": 55.0,
                    "markerValue": 45.0
                }
            }
        """.trimIndent()
        
        val metric = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HRV,
            value = 45.5,
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.GARMIN,
            metadata = complexMetadata
        )
        
        // When
        healthMetricDao.insertHealthMetric(metric)
        
        // Then
        val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
        val savedMetric = savedMetrics.first()
        
        assertNotNull("Metadata should be preserved", savedMetric.metadata)
        assertTrue("Metadata should contain baseline data", savedMetric.metadata!!.contains("baseline"))
        assertTrue("Metadata should contain lastNightAvg", savedMetric.metadata!!.contains("lastNightAvg"))
        assertTrue("Metadata should contain type", savedMetric.metadata!!.contains("weekly_avg"))
    }

    @Test
    fun testGarminDataValidation() = runTest {
        // Given
        val userId = "test_user_id"
        
        // Test various edge cases
        val validMetrics = listOf(
            // Valid HRV value
            createTestHealthMetric(userId, HealthMetricType.HRV, 45.5, source = DataSource.GARMIN),
            // Valid recovery score
            createTestHealthMetric(userId, HealthMetricType.TRAINING_RECOVERY, 85.0, source = DataSource.GARMIN),
            // Valid stress score
            createTestHealthMetric(userId, HealthMetricType.STRESS_SCORE, 25.0, source = DataSource.GARMIN),
            // Valid biological age
            createTestHealthMetric(userId, HealthMetricType.BIOLOGICAL_AGE, 28.5, source = DataSource.GARMIN)
        )
        
        val edgeCaseMetrics = listOf(
            // Zero values (should be handled gracefully)
            createTestHealthMetric(userId, HealthMetricType.HRV, 0.0, source = DataSource.GARMIN),
            // Very high values
            createTestHealthMetric(userId, HealthMetricType.TRAINING_RECOVERY, 100.0, source = DataSource.GARMIN),
            // Very low values
            createTestHealthMetric(userId, HealthMetricType.STRESS_SCORE, 1.0, source = DataSource.GARMIN)
        )
        
        // When - Save all metrics
        (validMetrics + edgeCaseMetrics).forEach { metric ->
            healthMetricDao.insertHealthMetric(metric)
        }
        
        // Then - Verify all metrics are saved
        val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
        assertEquals("Should save all metrics including edge cases", 
            validMetrics.size + edgeCaseMetrics.size, savedMetrics.size)
        
        // Verify data types are preserved
        savedMetrics.forEach { metric ->
            assertTrue("Value should be valid double", metric.value.isFinite())
            assertNotNull("Timestamp should not be null", metric.timestamp)
            assertEquals("Source should be Garmin", DataSource.GARMIN, metric.source)
        }
    }

    @Test
    fun testGarminDataQueryPerformance() = runTest {
        // Given - Large dataset
        val userId = "test_user_id"
        val largeDataset = (1..1000).map { index ->
            createTestHealthMetric(
                userId = userId,
                type = HealthMetricType.values()[index % HealthMetricType.values().size],
                value = index.toDouble(),
                timestamp = LocalDateTime.now().minusDays(index.toLong()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                source = DataSource.GARMIN
            )
        }
        
        // When - Save large dataset
        val startTime = System.currentTimeMillis()
        largeDataset.forEach { metric ->
            healthMetricDao.insertHealthMetric(metric)
        }
        val insertTime = System.currentTimeMillis() - startTime
        
        // Query data
        val queryStartTime = System.currentTimeMillis()
        val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
        val queryTime = System.currentTimeMillis() - queryStartTime
        
        // Then - Verify performance is acceptable
        assertEquals("Should save all metrics", largeDataset.size, savedMetrics.size)
        assertTrue("Insert time should be reasonable (< 5 seconds)", insertTime < 5000)
        assertTrue("Query time should be reasonable (< 1 second)", queryTime < 1000)
        
        // Verify data integrity
        val garminMetrics = savedMetrics.filter { it.source == DataSource.GARMIN }
        assertEquals("All metrics should be from Garmin", largeDataset.size, garminMetrics.size)
    }

    // =============================================================================
    // Error Handling Integration Tests
    // =============================================================================

    @Test
    fun testDatabaseConstraintHandling() = runTest {
        // Given
        val userId = "test_user_id"
        val metric = createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HRV,
            value = 45.5,
            source = DataSource.GARMIN
        )
        
        // When - Save metric twice (same ID)
        healthMetricDao.insertHealthMetric(metric)
        
        // Try to insert again with same ID but different value
        val duplicateMetric = metric.copy(value = 50.0)
        
        // Then - Should handle gracefully (replace or ignore based on implementation)
        try {
            healthMetricDao.insertHealthMetric(duplicateMetric)
            
            val savedMetrics = healthMetricDao.getHealthMetricsByUserId(userId).first()
            assertEquals("Should have only one metric", 1, savedMetrics.size)
            
        } catch (e: Exception) {
            // If constraint violation is thrown, that's also acceptable behavior
            assertTrue("Should be a constraint violation", 
                e.message?.contains("UNIQUE constraint") == true ||
                e.message?.contains("PRIMARY KEY constraint") == true)
        }
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    private fun createTestGarminMetrics(userId: String) = listOf(
        createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.HRV,
            value = 45.5,
            source = DataSource.GARMIN,
            metadata = """{"type": "weekly_avg", "lastNightAvg": 42.3}"""
        ),
        createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.TRAINING_RECOVERY,
            value = 85.0,
            source = DataSource.GARMIN,
            metadata = """{"sleepScore": 78.0, "hrvScore": 92.0}"""
        ),
        createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.STRESS_SCORE,
            value = 25.0,
            source = DataSource.GARMIN,
            metadata = """{"restStressLevel": 20.0, "activityStressLevel": 35.0}"""
        ),
        createTestHealthMetric(
            userId = userId,
            type = HealthMetricType.BIOLOGICAL_AGE,
            value = 28.5,
            source = DataSource.GARMIN,
            metadata = """{"chronologicalAge": 32.0, "type": "fitness_age"}"""
        )
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
    ) = com.beaconledger.welltrack.data.model.HealthMetric(
        id = "${userId}_${type}_${timestamp}_${source}",
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
        HealthMetricType.HRV -> "ms"
        HealthMetricType.TRAINING_RECOVERY -> "score"
        HealthMetricType.STRESS_SCORE -> "score"
        HealthMetricType.BIOLOGICAL_AGE -> "years"
        HealthMetricType.HEART_RATE -> "bpm"
        HealthMetricType.VO2_MAX -> "ml/min/kg"
        else -> "unit"
    }
}