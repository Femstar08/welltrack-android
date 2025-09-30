package com.beaconledger.welltrack.data.health

import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Comprehensive health data validator that ensures data quality and consistency
 */
@Singleton
class HealthDataValidator @Inject constructor() {
    
    companion object {
        // Validation ranges for different health metrics
        private val METRIC_RANGES = mapOf(
            HealthMetricType.STEPS to (0.0 to 100000.0),
            HealthMetricType.HEART_RATE to (30.0 to 220.0),
            HealthMetricType.WEIGHT to (20.0 to 500.0), // kg
            HealthMetricType.CALORIES_BURNED to (0.0 to 10000.0),
            HealthMetricType.BLOOD_PRESSURE to (50.0 to 250.0), // mmHg
            HealthMetricType.BLOOD_GLUCOSE to (2.0 to 30.0), // mmol/L
            HealthMetricType.BODY_FAT_PERCENTAGE to (3.0 to 50.0),
            HealthMetricType.MUSCLE_MASS to (10.0 to 100.0), // kg
            HealthMetricType.SLEEP_DURATION to (0.0 to 24.0), // hours
            HealthMetricType.EXERCISE_DURATION to (0.0 to 720.0), // minutes
            HealthMetricType.HYDRATION to (0.0 to 10.0), // liters
            HealthMetricType.VO2_MAX to (10.0 to 90.0), // ml/min/kg
            HealthMetricType.HRV to (5.0 to 200.0), // ms
            HealthMetricType.TRAINING_RECOVERY to (0.0 to 100.0), // score
            HealthMetricType.STRESS_SCORE to (0.0 to 100.0), // score
            HealthMetricType.BIOLOGICAL_AGE to (10.0 to 120.0), // years
            
            // Biomarkers - Hormonal (typical ranges, may vary by lab)
            HealthMetricType.TESTOSTERONE to (0.1 to 50.0), // nmol/L
            HealthMetricType.ESTRADIOL to (0.0 to 2000.0), // pmol/L
            HealthMetricType.CORTISOL to (100.0 to 800.0), // nmol/L
            HealthMetricType.THYROID_TSH to (0.4 to 10.0), // mIU/L
            HealthMetricType.THYROID_T3 to (3.0 to 8.0), // pmol/L
            HealthMetricType.THYROID_T4 to (9.0 to 25.0), // pmol/L
            
            // Biomarkers - Micronutrients
            HealthMetricType.VITAMIN_D3 to (10.0 to 250.0), // nmol/L
            HealthMetricType.VITAMIN_B12 to (150.0 to 900.0), // pmol/L
            HealthMetricType.VITAMIN_B6 to (20.0 to 200.0), // nmol/L
            HealthMetricType.FOLATE to (7.0 to 45.0), // nmol/L
            HealthMetricType.IRON to (6.0 to 35.0), // μmol/L
            HealthMetricType.FERRITIN to (12.0 to 300.0), // μg/L
            HealthMetricType.ZINC to (10.0 to 25.0), // μmol/L
            HealthMetricType.MAGNESIUM to (0.7 to 1.1), // mmol/L
            
            // Biomarkers - General Health
            HealthMetricType.LIPID_PANEL_TOTAL_CHOLESTEROL to (2.0 to 10.0), // mmol/L
            HealthMetricType.LIPID_PANEL_HDL to (0.5 to 3.0), // mmol/L
            HealthMetricType.LIPID_PANEL_LDL to (1.0 to 8.0), // mmol/L
            HealthMetricType.LIPID_PANEL_TRIGLYCERIDES to (0.4 to 5.0), // mmol/L
            HealthMetricType.HBA1C to (4.0 to 15.0), // %
            HealthMetricType.RBC_COUNT to (3.5 to 6.5), // 10^12/L
            HealthMetricType.HEMOGLOBIN to (100.0 to 200.0) // g/L
        )
        
        // Expected units for each metric type
        private val EXPECTED_UNITS = mapOf(
            HealthMetricType.STEPS to setOf("steps", "count"),
            HealthMetricType.HEART_RATE to setOf("bpm", "beats/min"),
            HealthMetricType.WEIGHT to setOf("kg", "lbs", "pounds"),
            HealthMetricType.CALORIES_BURNED to setOf("cal", "kcal", "calories"),
            HealthMetricType.BLOOD_PRESSURE to setOf("mmHg", "mm Hg"),
            HealthMetricType.BLOOD_GLUCOSE to setOf("mmol/L", "mg/dL"),
            HealthMetricType.BODY_FAT_PERCENTAGE to setOf("%", "percent"),
            HealthMetricType.MUSCLE_MASS to setOf("kg", "lbs"),
            HealthMetricType.SLEEP_DURATION to setOf("hours", "h", "minutes", "min"),
            HealthMetricType.EXERCISE_DURATION to setOf("minutes", "min", "hours", "h"),
            HealthMetricType.HYDRATION to setOf("L", "liters", "ml", "milliliters"),
            HealthMetricType.VO2_MAX to setOf("ml/min/kg", "ml/kg/min"),
            HealthMetricType.HRV to setOf("ms", "milliseconds"),
            HealthMetricType.TRAINING_RECOVERY to setOf("score", "points"),
            HealthMetricType.STRESS_SCORE to setOf("score", "points"),
            HealthMetricType.BIOLOGICAL_AGE to setOf("years", "y")
        )
        
        // Data sources that are considered more reliable for validation
        private val RELIABLE_SOURCES = setOf(
            DataSource.MANUAL_ENTRY,
            DataSource.BLOOD_TEST,
            DataSource.GARMIN
        )
    }
    
    /**
     * Validates a health metric for data quality and consistency
     */
    fun validateHealthMetric(metric: HealthMetric): ValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()
        
        // Validate basic fields
        validateBasicFields(metric, errors)
        
        // Validate timestamp
        validateTimestamp(metric, errors, warnings)
        
        // Validate value range
        validateValueRange(metric, errors, warnings)
        
        // Validate unit consistency
        validateUnit(metric, warnings)
        
        // Validate data source consistency
        validateDataSource(metric, warnings)
        
        // Validate metadata format
        validateMetadata(metric, warnings)
        
        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            confidence = calculateConfidence(metric, errors, warnings)
        )
    }
    
    /**
     * Sanitizes health metric data to ensure consistency
     */
    fun sanitizeHealthMetric(metric: HealthMetric): HealthMetric {
        return metric.copy(
            // Normalize timestamp format
            timestamp = normalizeTimestamp(metric.timestamp),
            
            // Round value to appropriate precision
            value = roundValueToPrecision(metric.value, metric.type),
            
            // Normalize unit
            unit = normalizeUnit(metric.unit, metric.type),
            
            // Clean metadata
            metadata = sanitizeMetadata(metric.metadata)
        )
    }
    
    /**
     * Validates basic required fields
     */
    private fun validateBasicFields(metric: HealthMetric, errors: MutableList<String>) {
        if (metric.id.isBlank()) {
            errors.add("Health metric ID cannot be blank")
        }
        
        if (metric.userId.isBlank()) {
            errors.add("User ID cannot be blank")
        }
        
        if (metric.unit.isBlank()) {
            errors.add("Unit cannot be blank")
        }
        
        if (metric.timestamp.isBlank()) {
            errors.add("Timestamp cannot be blank")
        }
    }
    
    /**
     * Validates timestamp format and reasonableness
     */
    private fun validateTimestamp(
        metric: HealthMetric,
        errors: MutableList<String>,
        warnings: MutableList<String>
    ) {
        try {
            val timestamp = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val now = LocalDateTime.now()
            
            // Check if timestamp is in the future
            if (timestamp.isAfter(now)) {
                warnings.add("Timestamp is in the future: ${metric.timestamp}")
            }
            
            // Check if timestamp is too far in the past (more than 10 years)
            if (timestamp.isBefore(now.minusYears(10))) {
                warnings.add("Timestamp is more than 10 years old: ${metric.timestamp}")
            }
            
        } catch (e: DateTimeParseException) {
            errors.add("Invalid timestamp format: ${metric.timestamp}")
        }
    }
    
    /**
     * Validates value is within expected range for the metric type
     */
    private fun validateValueRange(
        metric: HealthMetric,
        errors: MutableList<String>,
        warnings: MutableList<String>
    ) {
        val range = METRIC_RANGES[metric.type]
        if (range != null) {
            val (min, max) = range
            
            if (metric.value < min || metric.value > max) {
                if (metric.source in RELIABLE_SOURCES) {
                    warnings.add("Value ${metric.value} ${metric.unit} is outside typical range [$min-$max] for ${metric.type}")
                } else {
                    errors.add("Value ${metric.value} ${metric.unit} is outside acceptable range [$min-$max] for ${metric.type}")
                }
            }
        }
        
        // Check for obviously invalid values
        if (metric.value.isNaN() || metric.value.isInfinite()) {
            errors.add("Value is not a valid number: ${metric.value}")
        }
        
        if (metric.value < 0 && !isNegativeValueAllowed(metric.type)) {
            errors.add("Negative values not allowed for ${metric.type}: ${metric.value}")
        }
    }
    
    /**
     * Validates unit consistency for the metric type
     */
    private fun validateUnit(metric: HealthMetric, warnings: MutableList<String>) {
        val expectedUnits = EXPECTED_UNITS[metric.type]
        if (expectedUnits != null && metric.unit.lowercase() !in expectedUnits.map { it.lowercase() }) {
            warnings.add("Unexpected unit '${metric.unit}' for ${metric.type}. Expected one of: ${expectedUnits.joinToString()}")
        }
    }
    
    /**
     * Validates data source consistency
     */
    private fun validateDataSource(metric: HealthMetric, warnings: MutableList<String>) {
        // Check if the data source is appropriate for the metric type
        when (metric.type) {
            HealthMetricType.ECG -> {
                if (metric.source != DataSource.SAMSUNG_HEALTH && metric.source != DataSource.MANUAL_ENTRY) {
                    warnings.add("ECG data typically comes from Samsung Health or manual entry")
                }
            }
            HealthMetricType.HRV, HealthMetricType.TRAINING_RECOVERY, HealthMetricType.BIOLOGICAL_AGE -> {
                if (metric.source != DataSource.GARMIN && metric.source != DataSource.MANUAL_ENTRY) {
                    warnings.add("${metric.type} data typically comes from Garmin or manual entry")
                }
            }
            in listOf(
                HealthMetricType.TESTOSTERONE, HealthMetricType.ESTRADIOL, HealthMetricType.CORTISOL,
                HealthMetricType.VITAMIN_D3, HealthMetricType.VITAMIN_B12, HealthMetricType.HBA1C
            ) -> {
                if (metric.source != DataSource.BLOOD_TEST && metric.source != DataSource.MANUAL_ENTRY) {
                    warnings.add("Biomarker ${metric.type} should typically come from blood tests or manual entry")
                }
            }
        }
    }
    
    /**
     * Validates metadata format if present
     */
    private fun validateMetadata(metric: HealthMetric, warnings: MutableList<String>) {
        metric.metadata?.let { metadata ->
            if (metadata.isNotBlank()) {
                try {
                    // Try to parse as JSON to validate format
                    kotlinx.serialization.json.Json.parseToJsonElement(metadata)
                } catch (e: Exception) {
                    warnings.add("Metadata is not valid JSON format")
                }
            }
        }
    }
    
    /**
     * Calculates confidence score based on validation results
     */
    private fun calculateConfidence(
        metric: HealthMetric,
        errors: List<String>,
        warnings: List<String>
    ): Float {
        var confidence = 1.0f
        
        // Reduce confidence for each error and warning
        confidence -= errors.size * 0.3f
        confidence -= warnings.size * 0.1f
        
        // Boost confidence for reliable sources
        if (metric.source in RELIABLE_SOURCES) {
            confidence += 0.1f
        }
        
        // Boost confidence for manual entry
        if (metric.source == DataSource.MANUAL_ENTRY) {
            confidence += 0.1f
        }
        
        // Boost confidence for blood test data
        if (metric.source == DataSource.BLOOD_TEST) {
            confidence += 0.2f
        }
        
        return confidence.coerceIn(0.0f, 1.0f)
    }
    
    /**
     * Normalizes timestamp to consistent format
     */
    private fun normalizeTimestamp(timestamp: String): String {
        return try {
            val parsed = LocalDateTime.parse(timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            parsed.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            timestamp // Return original if parsing fails
        }
    }
    
    /**
     * Rounds value to appropriate precision for the metric type
     */
    private fun roundValueToPrecision(value: Double, type: HealthMetricType): Double {
        return when (type) {
            HealthMetricType.STEPS -> value.toInt().toDouble()
            HealthMetricType.HEART_RATE -> kotlin.math.round(value).toDouble()
            HealthMetricType.WEIGHT -> kotlin.math.round(value * 10) / 10.0
            HealthMetricType.BODY_FAT_PERCENTAGE -> kotlin.math.round(value * 10) / 10.0
            HealthMetricType.BLOOD_GLUCOSE -> kotlin.math.round(value * 10) / 10.0
            HealthMetricType.VO2_MAX -> kotlin.math.round(value * 10) / 10.0
            HealthMetricType.HRV -> kotlin.math.round(value).toDouble()
            else -> kotlin.math.round(value * 100) / 100.0 // 2 decimal places default
        }
    }
    
    /**
     * Normalizes unit to standard format
     */
    private fun normalizeUnit(unit: String, type: HealthMetricType): String {
        val normalizedUnit = unit.lowercase().trim()
        
        return when (type) {
            HealthMetricType.STEPS -> "steps"
            HealthMetricType.HEART_RATE -> "bpm"
            HealthMetricType.WEIGHT -> if (normalizedUnit in setOf("lbs", "pounds")) "lbs" else "kg"
            HealthMetricType.CALORIES_BURNED -> "cal"
            HealthMetricType.BLOOD_PRESSURE -> "mmHg"
            HealthMetricType.BODY_FAT_PERCENTAGE -> "%"
            HealthMetricType.SLEEP_DURATION -> if (normalizedUnit in setOf("min", "minutes")) "minutes" else "hours"
            HealthMetricType.EXERCISE_DURATION -> "minutes"
            HealthMetricType.HYDRATION -> if (normalizedUnit in setOf("ml", "milliliters")) "ml" else "L"
            HealthMetricType.VO2_MAX -> "ml/min/kg"
            HealthMetricType.HRV -> "ms"
            HealthMetricType.BIOLOGICAL_AGE -> "years"
            else -> unit // Keep original for other types
        }
    }
    
    /**
     * Sanitizes metadata JSON
     */
    private fun sanitizeMetadata(metadata: String?): String? {
        return metadata?.let { meta ->
            if (meta.isBlank()) null
            else {
                try {
                    // Parse and re-serialize to clean up formatting
                    val jsonElement = kotlinx.serialization.json.Json.parseToJsonElement(meta)
                    kotlinx.serialization.json.Json.encodeToString(jsonElement)
                } catch (e: Exception) {
                    meta // Return original if parsing fails
                }
            }
        }
    }
    
    /**
     * Checks if negative values are allowed for a metric type
     */
    private fun isNegativeValueAllowed(type: HealthMetricType): Boolean {
        return when (type) {
            // Most health metrics don't allow negative values
            HealthMetricType.STEPS, HealthMetricType.HEART_RATE, HealthMetricType.WEIGHT,
            HealthMetricType.CALORIES_BURNED, HealthMetricType.BLOOD_PRESSURE,
            HealthMetricType.BLOOD_GLUCOSE, HealthMetricType.BODY_FAT_PERCENTAGE,
            HealthMetricType.MUSCLE_MASS, HealthMetricType.SLEEP_DURATION,
            HealthMetricType.EXERCISE_DURATION, HealthMetricType.HYDRATION,
            HealthMetricType.VO2_MAX, HealthMetricType.HRV -> false
            
            // Some custom metrics might allow negative values
            HealthMetricType.CUSTOM_HABIT -> true
            
            else -> false
        }
    }
    
    /**
     * Batch validates multiple health metrics
     */
    fun validateHealthMetrics(metrics: List<HealthMetric>): BatchValidationResult {
        val results = metrics.map { metric ->
            metric.id to validateHealthMetric(metric)
        }.toMap()
        
        val validMetrics = results.filter { it.value.isValid }.keys
        val invalidMetrics = results.filter { !it.value.isValid }.keys
        val warnings = results.values.flatMap { it.warnings }
        
        return BatchValidationResult(
            totalMetrics = metrics.size,
            validMetrics = validMetrics.size,
            invalidMetrics = invalidMetrics.size,
            validationResults = results,
            overallWarnings = warnings.distinct()
        )
    }
}

/**
 * Result of health metric validation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val confidence: Float
)

/**
 * Result of batch validation
 */
data class BatchValidationResult(
    val totalMetrics: Int,
    val validMetrics: Int,
    val invalidMetrics: Int,
    val validationResults: Map<String, ValidationResult>,
    val overallWarnings: List<String>
)