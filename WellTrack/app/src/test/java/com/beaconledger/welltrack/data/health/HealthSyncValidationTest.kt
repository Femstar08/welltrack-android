package com.beaconledger.welltrack.data.health

import com.beaconledger.welltrack.data.model.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * Comprehensive validation tests for health sync scenarios
 * Tests data validation, edge cases, and error handling across platforms
 */
class HealthSyncValidationTest {

    private lateinit var healthDataValidator: HealthDataValidator
    private lateinit var healthDataPrioritizer: HealthDataPrioritizer
    private lateinit var healthDataConflictResolver: HealthDataConflictResolver

    @Before
    fun setup() {
        healthDataValidator = HealthDataValidator()
        healthDataPrioritizer = HealthDataPrioritizer()
        healthDataConflictResolver = mockk()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // =============================================================================
    // Data Validation Tests
    // =============================================================================

    @Test
    fun `test valid health metrics pass validation`() = runTest {
        // Given
        val validMetrics = listOf(
            createValidHealthMetric(HealthMetricType.STEPS, 10000.0, "steps"),
            createValidHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm"),
            createValidHealthMetric(HealthMetricType.WEIGHT, 70.5, "kg"),
            createValidHealthMetric(HealthMetricType.BLOOD_PRESSURE, 120.0, "mmHg"),
            createValidHealthMetric(HealthMetricType.HRV, 45.0, "ms"),
            createValidHealthMetric(HealthMetricType.VO2_MAX, 52.0, "ml/min/kg")
        )

        // When & Then
        validMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            assertTrue("Valid metric should pass validation: ${metric.type}", result.isValid)
            assertTrue("Valid metric should have no errors", result.errors.isEmpty())
            assertTrue("Valid metric should have high confidence", result.confidence >= 0.8f)
        }
    }

    @Test
    fun `test invalid health metrics fail validation`() = runTest {
        // Given
        val invalidMetrics = listOf(
            // Negative values
            createInvalidHealthMetric(HealthMetricType.STEPS, -100.0, "steps"),
            createInvalidHealthMetric(HealthMetricType.HEART_RATE, -50.0, "bpm"),
            createInvalidHealthMetric(HealthMetricType.WEIGHT, -10.0, "kg"),
            
            // Unrealistic values
            createInvalidHealthMetric(HealthMetricType.HEART_RATE, 300.0, "bpm"),
            createInvalidHealthMetric(HealthMetricType.WEIGHT, 500.0, "kg"),
            createInvalidHealthMetric(HealthMetricType.BLOOD_PRESSURE, 400.0, "mmHg"),
            
            // Zero values where inappropriate
            createInvalidHealthMetric(HealthMetricType.WEIGHT, 0.0, "kg"),
            createInvalidHealthMetric(HealthMetricType.VO2_MAX, 0.0, "ml/min/kg"),
            
            // Invalid units
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "invalid_unit"),
            createHealthMetric(HealthMetricType.WEIGHT, 70.0, "pounds") // Should be kg
        )

        // When & Then
        invalidMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            assertFalse("Invalid metric should fail validation: ${metric.type} = ${metric.value}", result.isValid)
            assertTrue("Invalid metric should have errors", result.errors.isNotEmpty())
            assertTrue("Invalid metric should have low confidence", result.confidence < 0.5f)
        }
    }

    @Test
    fun `test edge case values are handled correctly`() = runTest {
        // Given
        val edgeCaseMetrics = listOf(
            // Boundary values
            createHealthMetric(HealthMetricType.HEART_RATE, 30.0, "bpm"), // Very low but possible
            createHealthMetric(HealthMetricType.HEART_RATE, 220.0, "bpm"), // Very high but possible
            createHealthMetric(HealthMetricType.WEIGHT, 30.0, "kg"), // Very low but possible
            createHealthMetric(HealthMetricType.WEIGHT, 200.0, "kg"), // Very high but possible
            createHealthMetric(HealthMetricType.BLOOD_PRESSURE, 60.0, "mmHg"), // Low blood pressure
            createHealthMetric(HealthMetricType.BLOOD_PRESSURE, 200.0, "mmHg"), // High blood pressure
            
            // Decimal precision
            createHealthMetric(HealthMetricType.WEIGHT, 70.123456789, "kg"),
            createHealthMetric(HealthMetricType.HRV, 45.987654321, "ms"),
            
            // Very small values
            createHealthMetric(HealthMetricType.HRV, 0.1, "ms"),
            createHealthMetric(HealthMetricType.BIOLOGICAL_AGE, 1.0, "years")
        )

        // When & Then
        edgeCaseMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            
            // Edge cases should either pass with warnings or fail gracefully
            if (result.isValid) {
                assertTrue("Edge case should have warnings or moderate confidence", 
                    result.warnings.isNotEmpty() || result.confidence < 1.0f)
            } else {
                assertTrue("Failed edge case should have clear error message", 
                    result.errors.isNotEmpty())
            }
        }
    }

    @Test
    fun `test timestamp validation`() = runTest {
        // Given
        val baseMetric = createValidHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm")
        
        val timestampVariations = listOf(
            // Valid timestamps
            baseMetric.copy(timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            baseMetric.copy(timestamp = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            baseMetric.copy(timestamp = LocalDateTime.now().minusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)),
            
            // Invalid timestamps
            baseMetric.copy(timestamp = "invalid-timestamp"),
            baseMetric.copy(timestamp = "2024-13-45T25:70:80"), // Invalid date/time
            baseMetric.copy(timestamp = ""), // Empty timestamp
            baseMetric.copy(timestamp = LocalDateTime.now().plusYears(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)) // Future timestamp
        )

        // When & Then
        timestampVariations.forEachIndexed { index, metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            
            when (index) {
                0, 1, 2 -> assertTrue("Valid timestamp should pass: ${metric.timestamp}", result.isValid)
                else -> assertFalse("Invalid timestamp should fail: ${metric.timestamp}", result.isValid)
            }
        }
    }

    @Test
    fun `test metadata validation`() = runTest {
        // Given
        val baseMetric = createValidHealthMetric(HealthMetricType.BLOOD_PRESSURE, 120.0, "mmHg")
        
        val metadataVariations = listOf(
            // Valid metadata
            baseMetric.copy(metadata = null),
            baseMetric.copy(metadata = "{}"),
            baseMetric.copy(metadata = "{\"type\":\"systolic\"}"),
            baseMetric.copy(metadata = "{\"measurement_context\":\"resting\",\"device\":\"omron\"}"),
            
            // Invalid metadata
            baseMetric.copy(metadata = "invalid-json"),
            baseMetric.copy(metadata = "{\"unclosed\":\"json\""),
            baseMetric.copy(metadata = "{'single_quotes': 'invalid'}"),
            baseMetric.copy(metadata = "a".repeat(10000)) // Extremely long metadata
        )

        // When & Then
        metadataVariations.forEachIndexed { index, metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            
            when (index) {
                0, 1, 2, 3 -> assertTrue("Valid metadata should pass: ${metric.metadata}", result.isValid)
                else -> {
                    // Invalid metadata should either fail validation or pass with warnings
                    if (!result.isValid) {
                        assertTrue("Invalid metadata should have error", result.errors.isNotEmpty())
                    } else {
                        assertTrue("Invalid metadata should have warnings", result.warnings.isNotEmpty())
                    }
                }
            }
        }
    }

    // =============================================================================
    // Data Prioritization Tests
    // =============================================================================

    @Test
    fun `test data source prioritization`() = runTest {
        // Given
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val conflictingMetrics = listOf(
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm", timestamp, DataSource.HEALTH_CONNECT, confidence = 0.8f),
            createHealthMetric(HealthMetricType.HEART_RATE, 78.0, "bpm", timestamp, DataSource.SAMSUNG_HEALTH, confidence = 0.9f),
            createHealthMetric(HealthMetricType.HEART_RATE, 76.0, "bpm", timestamp, DataSource.GARMIN, confidence = 0.95f),
            createHealthMetric(HealthMetricType.HEART_RATE, 80.0, "bpm", timestamp, DataSource.MANUAL_ENTRY, confidence = 0.7f, isManualEntry = true)
        )

        // When
        val prioritizedMetrics = healthDataPrioritizer.prioritizeAndDeduplicate(conflictingMetrics)

        // Then
        assertEquals("Should resolve to single metric", 1, prioritizedMetrics.size)
        
        // Manual entry should win if present, otherwise highest confidence
        val winningMetric = prioritizedMetrics.first()
        if (conflictingMetrics.any { it.isManualEntry }) {
            assertTrue("Manual entry should win", winningMetric.isManualEntry)
            assertEquals("Manual entry value should be preserved", 80.0, winningMetric.value, 0.01)
        } else {
            assertEquals("Highest confidence source should win", DataSource.GARMIN, winningMetric.source)
            assertEquals("Garmin value should be preserved", 76.0, winningMetric.value, 0.01)
        }
    }

    @Test
    fun `test temporal deduplication`() = runTest {
        // Given
        val baseTime = LocalDateTime.now()
        val closeTimestamps = listOf(
            baseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            baseTime.plusMinutes(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            baseTime.plusMinutes(2).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            baseTime.plusMinutes(10).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) // Outside deduplication window
        )
        
        val temporalMetrics = closeTimestamps.mapIndexed { index, timestamp ->
            createHealthMetric(HealthMetricType.STEPS, 1000.0 + index, "steps", timestamp, DataSource.HEALTH_CONNECT)
        }

        // When
        val deduplicatedMetrics = healthDataPrioritizer.prioritizeAndDeduplicate(temporalMetrics)

        // Then
        assertTrue("Should deduplicate close timestamps", deduplicatedMetrics.size < temporalMetrics.size)
        assertTrue("Should keep at least one metric", deduplicatedMetrics.isNotEmpty())
        
        // Should keep the metric outside the deduplication window
        val timestamps = deduplicatedMetrics.map { it.timestamp }
        assertTrue("Should keep temporally distant metric", 
            timestamps.contains(closeTimestamps.last()))
    }

    @Test
    fun `test confidence-based prioritization`() = runTest {
        // Given
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        val confidenceMetrics = listOf(
            createHealthMetric(HealthMetricType.WEIGHT, 70.0, "kg", timestamp, DataSource.HEALTH_CONNECT, confidence = 0.6f),
            createHealthMetric(HealthMetricType.WEIGHT, 70.5, "kg", timestamp, DataSource.SAMSUNG_HEALTH, confidence = 0.9f),
            createHealthMetric(HealthMetricType.WEIGHT, 69.8, "kg", timestamp, DataSource.GARMIN, confidence = 0.95f)
        )

        // When
        val prioritizedMetrics = healthDataPrioritizer.prioritizeAndDeduplicate(confidenceMetrics)

        // Then
        assertEquals("Should resolve to single metric", 1, prioritizedMetrics.size)
        val winningMetric = prioritizedMetrics.first()
        assertEquals("Highest confidence metric should win", 0.95f, winningMetric.confidence, 0.01f)
        assertEquals("Garmin value should be preserved", 69.8, winningMetric.value, 0.01)
    }

    // =============================================================================
    // Cross-Platform Compatibility Tests
    // =============================================================================

    @Test
    fun `test Health Connect data format compatibility`() = runTest {
        // Given
        val healthConnectMetrics = listOf(
            createHealthMetric(HealthMetricType.STEPS, 12000.0, "steps", source = DataSource.HEALTH_CONNECT),
            createHealthMetric(HealthMetricType.HEART_RATE, 72.0, "bpm", source = DataSource.HEALTH_CONNECT),
            createHealthMetric(HealthMetricType.WEIGHT, 68.5, "kg", source = DataSource.HEALTH_CONNECT),
            createHealthMetric(HealthMetricType.SLEEP_DURATION, 7.5, "hours", source = DataSource.HEALTH_CONNECT),
            createHealthMetric(HealthMetricType.BLOOD_PRESSURE, 115.0, "mmHg", source = DataSource.HEALTH_CONNECT, 
                metadata = "{\"type\":\"systolic\"}")
        )

        // When & Then
        healthConnectMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            assertTrue("Health Connect metric should be valid: ${metric.type}", result.isValid)
            assertEquals("Health Connect source should be preserved", DataSource.HEALTH_CONNECT, metric.source)
        }
    }

    @Test
    fun `test Samsung Health data format compatibility`() = runTest {
        // Given
        val samsungHealthMetrics = listOf(
            createHealthMetric(HealthMetricType.HEART_RATE, 78.0, "bpm", source = DataSource.SAMSUNG_HEALTH),
            createHealthMetric(HealthMetricType.ECG, 75.0, "bpm", source = DataSource.SAMSUNG_HEALTH, 
                metadata = "{\"rhythm\":\"normal\",\"quality\":\"good\"}"),
            createHealthMetric(HealthMetricType.BODY_FAT_PERCENTAGE, 15.5, "%", source = DataSource.SAMSUNG_HEALTH),
            createHealthMetric(HealthMetricType.MUSCLE_MASS, 35.2, "kg", source = DataSource.SAMSUNG_HEALTH),
            createHealthMetric(HealthMetricType.STRESS_SCORE, 28.0, "score", source = DataSource.SAMSUNG_HEALTH)
        )

        // When & Then
        samsungHealthMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            assertTrue("Samsung Health metric should be valid: ${metric.type}", result.isValid)
            assertEquals("Samsung Health source should be preserved", DataSource.SAMSUNG_HEALTH, metric.source)
        }
    }

    @Test
    fun `test Garmin data format compatibility`() = runTest {
        // Given
        val garminMetrics = listOf(
            createHealthMetric(HealthMetricType.HRV, 45.5, "ms", source = DataSource.GARMIN,
                metadata = "{\"type\":\"weekly_avg\",\"lastNightAvg\":42.3}"),
            createHealthMetric(HealthMetricType.TRAINING_RECOVERY, 85.0, "score", source = DataSource.GARMIN,
                metadata = "{\"sleepScore\":78.0,\"hrvScore\":92.0}"),
            createHealthMetric(HealthMetricType.STRESS_SCORE, 22.0, "score", source = DataSource.GARMIN,
                metadata = "{\"restStressLevel\":20.0,\"activityStressLevel\":35.0}"),
            createHealthMetric(HealthMetricType.BIOLOGICAL_AGE, 28.5, "years", source = DataSource.GARMIN,
                metadata = "{\"chronologicalAge\":32.0,\"type\":\"fitness_age\"}"),
            createHealthMetric(HealthMetricType.VO2_MAX, 52.0, "ml/min/kg", source = DataSource.GARMIN,
                metadata = "{\"fitnessLevel\":\"excellent\"}")
        )

        // When & Then
        garminMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            assertTrue("Garmin metric should be valid: ${metric.type}", result.isValid)
            assertEquals("Garmin source should be preserved", DataSource.GARMIN, metric.source)
            
            // Verify Garmin-specific metadata is preserved
            if (metric.metadata != null) {
                assertTrue("Garmin metadata should be valid JSON", 
                    isValidJson(metric.metadata!!))
            }
        }
    }

    // =============================================================================
    // Error Handling and Recovery Tests
    // =============================================================================

    @Test
    fun `test graceful handling of corrupted data`() = runTest {
        // Given
        val corruptedMetrics = listOf(
            // Null/empty values
            createHealthMetric(HealthMetricType.HEART_RATE, Double.NaN, "bpm"),
            createHealthMetric(HealthMetricType.WEIGHT, Double.POSITIVE_INFINITY, "kg"),
            createHealthMetric(HealthMetricType.STEPS, Double.NEGATIVE_INFINITY, "steps"),
            
            // Corrupted IDs
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm").copy(id = ""),
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm").copy(id = "invalid-uuid-format"),
            
            // Corrupted user IDs
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm").copy(userId = ""),
            
            // Extremely long values
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "bpm").copy(
                metadata = "x".repeat(100000)
            )
        )

        // When & Then
        corruptedMetrics.forEach { metric ->
            val result = healthDataValidator.validateHealthMetric(metric)
            
            // Should either fail validation or sanitize the data
            if (result.isValid) {
                val sanitized = healthDataValidator.sanitizeHealthMetric(metric)
                assertNotNull("Sanitized metric should not be null", sanitized)
                assertTrue("Sanitized metric should have finite values", sanitized.value.isFinite())
            } else {
                assertTrue("Corrupted data should have validation errors", result.errors.isNotEmpty())
            }
        }
    }

    @Test
    fun `test data sanitization`() = runTest {
        // Given
        val unsanitizedMetrics = listOf(
            // Precision issues
            createHealthMetric(HealthMetricType.WEIGHT, 70.123456789012345, "kg"),
            createHealthMetric(HealthMetricType.HRV, 45.987654321098765, "ms"),
            
            // Whitespace issues
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, " bpm "),
            createHealthMetric(HealthMetricType.STEPS, 10000.0, "steps").copy(
                metadata = " { \"key\" : \"value\" } "
            ),
            
            // Case sensitivity issues
            createHealthMetric(HealthMetricType.HEART_RATE, 75.0, "BPM"),
            createHealthMetric(HealthMetricType.WEIGHT, 70.0, "KG")
        )

        // When & Then
        unsanitizedMetrics.forEach { metric ->
            val sanitized = healthDataValidator.sanitizeHealthMetric(metric)
            
            assertNotNull("Sanitized metric should not be null", sanitized)
            
            // Check precision is reasonable
            assertTrue("Value precision should be reasonable", 
                sanitized.value.toString().length <= 10)
            
            // Check units are normalized
            assertEquals("Units should be normalized", 
                sanitized.unit.trim().lowercase(), sanitized.unit)
            
            // Check metadata is cleaned
            if (sanitized.metadata != null) {
                assertFalse("Metadata should not have leading/trailing whitespace",
                    sanitized.metadata!!.startsWith(" ") || sanitized.metadata!!.endsWith(" "))
            }
        }
    }

    @Test
    fun `test batch validation performance`() = runTest {
        // Given
        val largeMetricsBatch = (1..1000).map { index ->
            createValidHealthMetric(
                HealthMetricType.values()[index % HealthMetricType.values().size],
                index.toDouble(),
                "unit"
            )
        }

        // When
        val startTime = System.currentTimeMillis()
        val validationResults = largeMetricsBatch.map { metric ->
            healthDataValidator.validateHealthMetric(metric)
        }
        val validationDuration = System.currentTimeMillis() - startTime

        // Then
        assertEquals("Should validate all metrics", largeMetricsBatch.size, validationResults.size)
        assertTrue("Batch validation should complete quickly (< 5 seconds)", validationDuration < 5000)
        
        val validCount = validationResults.count { it.isValid }
        assertTrue("Most metrics should be valid", validCount > largeMetricsBatch.size * 0.8)
    }

    // =============================================================================
    // Helper Methods
    // =============================================================================

    private fun createValidHealthMetric(
        type: HealthMetricType,
        value: Double,
        unit: String
    ): HealthMetric {
        return createHealthMetric(type, value, unit)
    }

    private fun createInvalidHealthMetric(
        type: HealthMetricType,
        value: Double,
        unit: String
    ): HealthMetric {
        return createHealthMetric(type, value, unit)
    }

    private fun createHealthMetric(
        type: HealthMetricType,
        value: Double,
        unit: String,
        timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        source: DataSource = DataSource.HEALTH_CONNECT,
        metadata: String? = null,
        confidence: Float = 1.0f,
        isManualEntry: Boolean = false
    ): HealthMetric {
        return HealthMetric(
            id = UUID.randomUUID().toString(),
            userId = "test-user-id",
            type = type,
            value = value,
            unit = unit,
            timestamp = timestamp,
            source = source,
            metadata = metadata,
            confidence = confidence,
            isManualEntry = isManualEntry
        )
    }

    private fun isValidJson(jsonString: String): Boolean {
        return try {
            org.json.JSONObject(jsonString)
            true
        } catch (e: Exception) {
            try {
                org.json.JSONArray(jsonString)
                true
            } catch (e: Exception) {
                false
            }
        }
    }
}