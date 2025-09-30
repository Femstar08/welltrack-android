package com.beaconledger.welltrack.data.health

import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HealthDataValidatorTest {
    
    private lateinit var healthDataValidator: HealthDataValidator
    
    @Before
    fun setup() {
        healthDataValidator = HealthDataValidator()
    }
    
    @Test
    fun `validateHealthMetric should pass for valid health metric`() {
        // Arrange
        val validMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "bpm",
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(validMetric)
        
        // Assert
        assertTrue("Valid metric should pass validation", result.isValid)
        assertTrue("Should have no errors", result.errors.isEmpty())
        assertEquals("Confidence should be high", 1.0f, result.confidence, 0.1f)
    }
    
    @Test
    fun `validateHealthMetric should fail for missing required fields`() {
        // Arrange
        val invalidMetric = HealthMetric(
            id = "", // Empty ID
            userId = "", // Empty user ID
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "", // Empty unit
            timestamp = "", // Empty timestamp
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(invalidMetric)
        
        // Assert
        assertFalse("Invalid metric should fail validation", result.isValid)
        assertTrue("Should have multiple errors", result.errors.size >= 4)
        assertTrue("Should contain ID error", result.errors.any { it.contains("ID cannot be blank") })
        assertTrue("Should contain user ID error", result.errors.any { it.contains("User ID cannot be blank") })
        assertTrue("Should contain unit error", result.errors.any { it.contains("Unit cannot be blank") })
        assertTrue("Should contain timestamp error", result.errors.any { it.contains("Timestamp cannot be blank") })
    }
    
    @Test
    fun `validateHealthMetric should fail for out-of-range values`() {
        // Arrange
        val invalidMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 300.0, // Invalid heart rate (too high)
            unit = "bpm",
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(invalidMetric)
        
        // Assert
        assertFalse("Out-of-range metric should fail validation", result.isValid)
        assertTrue("Should have range error", result.errors.any { it.contains("outside acceptable range") })
    }
    
    @Test
    fun `validateHealthMetric should warn for unusual but not invalid values from reliable sources`() {
        // Arrange
        val unusualMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 200.0, // High but possible heart rate
            unit = "bpm",
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.MANUAL_ENTRY, // Reliable source
            metadata = null,
            confidence = 1.0f,
            isManualEntry = true
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(unusualMetric)
        
        // Assert
        assertTrue("Unusual metric from reliable source should pass validation", result.isValid)
        assertTrue("Should have warnings", result.warnings.isNotEmpty())
        assertTrue("Should warn about unusual range", result.warnings.any { it.contains("outside typical range") })
    }
    
    @Test
    fun `validateHealthMetric should fail for negative values where not allowed`() {
        // Arrange
        val negativeMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.STEPS,
            value = -100.0, // Negative steps not allowed
            unit = "steps",
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(negativeMetric)
        
        // Assert
        assertFalse("Negative values should fail validation where not allowed", result.isValid)
        assertTrue("Should have negative value error", result.errors.any { it.contains("Negative values not allowed") })
    }
    
    @Test
    fun `validateHealthMetric should fail for invalid timestamp format`() {
        // Arrange
        val invalidTimestampMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "bpm",
            timestamp = "invalid-timestamp",
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(invalidTimestampMetric)
        
        // Assert
        assertFalse("Invalid timestamp should fail validation", result.isValid)
        assertTrue("Should have timestamp format error", result.errors.any { it.contains("Invalid timestamp format") })
    }
    
    @Test
    fun `validateHealthMetric should warn for future timestamps`() {
        // Arrange
        val futureMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "bpm",
            timestamp = LocalDateTime.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(futureMetric)
        
        // Assert
        assertTrue("Future timestamp should pass validation but warn", result.isValid)
        assertTrue("Should warn about future timestamp", result.warnings.any { it.contains("in the future") })
    }
    
    @Test
    fun `validateHealthMetric should warn for unexpected units`() {
        // Arrange
        val unexpectedUnitMetric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "beats", // Unexpected unit for heart rate
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val result = healthDataValidator.validateHealthMetric(unexpectedUnitMetric)
        
        // Assert
        assertTrue("Unexpected unit should pass validation but warn", result.isValid)
        assertTrue("Should warn about unexpected unit", result.warnings.any { it.contains("Unexpected unit") })
    }
    
    @Test
    fun `sanitizeHealthMetric should normalize timestamp format`() {
        // Arrange
        val metric = HealthMetric(
            id = "test-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.123456,
            unit = "BPM", // Mixed case
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        // Act
        val sanitized = healthDataValidator.sanitizeHealthMetric(metric)
        
        // Assert
        assertEquals("Should normalize unit to lowercase", "bpm", sanitized.unit)
        assertEquals("Should round heart rate to whole number", 75.0, sanitized.value, 0.01)
        assertNotNull("Should have normalized timestamp", sanitized.timestamp)
    }
    
    @Test
    fun `sanitizeHealthMetric should round values to appropriate precision`() {
        // Test different metric types and their precision requirements
        val testCases = listOf(
            HealthMetricType.STEPS to (12345.67 to 12345.0), // Steps should be whole numbers
            HealthMetricType.WEIGHT to (70.123 to 70.1), // Weight to 1 decimal
            HealthMetricType.BODY_FAT_PERCENTAGE to (15.456 to 15.5), // Body fat to 1 decimal
            HealthMetricType.BLOOD_GLUCOSE to (5.789 to 5.8), // Blood glucose to 1 decimal
            HealthMetricType.VO2_MAX to (45.678 to 45.7) // VO2 Max to 1 decimal
        )
        
        testCases.forEach { (metricType, valuePair) ->
            val (inputValue, expectedValue) = valuePair
            
            val metric = HealthMetric(
                id = "test-id",
                userId = "test-user",
                type = metricType,
                value = inputValue,
                unit = "unit",
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                source = DataSource.HEALTH_CONNECT,
                metadata = null,
                confidence = 1.0f,
                isManualEntry = false
            )
            
            val sanitized = healthDataValidator.sanitizeHealthMetric(metric)
            
            assertEquals(
                "Should round $metricType to appropriate precision",
                expectedValue,
                sanitized.value,
                0.01
            )
        }
    }
    
    @Test
    fun `validateHealthMetrics should validate multiple metrics in batch`() {
        // Arrange
        val validMetric = HealthMetric(
            id = "valid-id",
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "bpm",
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        val invalidMetric = HealthMetric(
            id = "", // Invalid - empty ID
            userId = "test-user",
            type = HealthMetricType.HEART_RATE,
            value = 75.0,
            unit = "bpm",
            timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            source = DataSource.HEALTH_CONNECT,
            metadata = null,
            confidence = 1.0f,
            isManualEntry = false
        )
        
        val metrics = listOf(validMetric, invalidMetric)
        
        // Act
        val result = healthDataValidator.validateHealthMetrics(metrics)
        
        // Assert
        assertEquals("Should validate all metrics", 2, result.totalMetrics)
        assertEquals("Should have 1 valid metric", 1, result.validMetrics)
        assertEquals("Should have 1 invalid metric", 1, result.invalidMetrics)
        assertTrue("Should have validation results for both metrics", result.validationResults.size == 2)
        assertTrue("Valid metric should pass", result.validationResults[validMetric.id]?.isValid == true)
        assertFalse("Invalid metric should fail", result.validationResults[invalidMetric.id]?.isValid == true)
    }
    
    @Test
    fun `validateHealthMetric should boost confidence for reliable sources`() {
        val testCases = listOf(
            DataSource.MANUAL_ENTRY to "Manual entry should boost confidence",
            DataSource.BLOOD_TEST to "Blood test should boost confidence",
            DataSource.GARMIN to "Garmin should boost confidence"
        )
        
        testCases.forEach { (source, message) ->
            val metric = HealthMetric(
                id = "test-id",
                userId = "test-user",
                type = HealthMetricType.HEART_RATE,
                value = 75.0,
                unit = "bpm",
                timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                source = source,
                metadata = null,
                confidence = 1.0f,
                isManualEntry = source == DataSource.MANUAL_ENTRY
            )
            
            val result = healthDataValidator.validateHealthMetric(metric)
            
            assertTrue(message, result.confidence >= 1.0f)
        }
    }
}