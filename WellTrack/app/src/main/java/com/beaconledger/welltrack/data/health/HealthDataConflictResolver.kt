package com.beaconledger.welltrack.data.health

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

/**
 * Resolves conflicts between health data from multiple sources
 */
@Singleton
class HealthDataConflictResolver @Inject constructor(
    private val healthDataPrioritizer: HealthDataPrioritizer
) {
    
    companion object {
        // Time window for considering metrics as potential conflicts (in minutes)
        private const val CONFLICT_TIME_WINDOW_MINUTES = 15L
        
        // Threshold for value differences to be considered conflicts (percentage)
        private const val VALUE_CONFLICT_THRESHOLD = 0.15 // 15%
        
        // Maximum age for automatic conflict resolution (in hours)
        private const val AUTO_RESOLUTION_MAX_AGE_HOURS = 24L
    }
    
    /**
     * Resolves conflicts in health metrics using various strategies
     */
    suspend fun resolveConflicts(
        userId: String,
        metrics: List<HealthMetric>
    ): List<HealthMetric> {
        // Group metrics by type and time to identify potential conflicts
        val conflictGroups = identifyConflictGroups(metrics)
        
        val resolvedMetrics = mutableListOf<HealthMetric>()
        val unresolvedConflicts = mutableListOf<HealthDataConflict>()
        
        for (group in conflictGroups) {
            if (group.size == 1) {
                // No conflict, add the single metric
                resolvedMetrics.add(group.first())
            } else {
                // Potential conflict, attempt resolution
                val resolutionResult = resolveConflictGroup(userId, group)
                
                when (resolutionResult) {
                    is ConflictResolutionResult.Resolved -> {
                        resolvedMetrics.add(resolutionResult.resolvedMetric)
                    }
                    is ConflictResolutionResult.RequiresManualResolution -> {
                        // For now, use the highest priority metric and log the conflict
                        val bestMetric = healthDataPrioritizer.prioritizeAndDeduplicate(group).first()
                        resolvedMetrics.add(bestMetric)
                        unresolvedConflicts.add(resolutionResult.conflict)
                    }
                }
            }
        }
        
        // Log unresolved conflicts for manual review
        if (unresolvedConflicts.isNotEmpty()) {
            logUnresolvedConflicts(userId, unresolvedConflicts)
        }
        
        return resolvedMetrics
    }
    
    /**
     * Identifies groups of metrics that might be in conflict
     */
    private fun identifyConflictGroups(metrics: List<HealthMetric>): List<List<HealthMetric>> {
        val groups = mutableListOf<MutableList<HealthMetric>>()
        
        // Sort metrics by type and timestamp
        val sortedMetrics = metrics.sortedWith(
            compareBy<HealthMetric> { it.type }.thenBy { it.timestamp }
        )
        
        for (metric in sortedMetrics) {
            val metricTime = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            
            // Find existing group that this metric could belong to
            val existingGroup = groups.find { group ->
                val groupMetric = group.first()
                groupMetric.type == metric.type && 
                isWithinConflictTimeWindow(
                    metricTime, 
                    LocalDateTime.parse(groupMetric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            }
            
            if (existingGroup != null) {
                existingGroup.add(metric)
            } else {
                groups.add(mutableListOf(metric))
            }
        }
        
        return groups
    }
    
    /**
     * Checks if two timestamps are within the conflict detection time window
     */
    private fun isWithinConflictTimeWindow(time1: LocalDateTime, time2: LocalDateTime): Boolean {
        val timeDifference = ChronoUnit.MINUTES.between(time1, time2)
        return abs(timeDifference) <= CONFLICT_TIME_WINDOW_MINUTES
    }
    
    /**
     * Resolves a conflict group using various strategies
     */
    private suspend fun resolveConflictGroup(
        userId: String,
        conflictGroup: List<HealthMetric>
    ): ConflictResolutionResult {
        if (conflictGroup.size < 2) {
            return ConflictResolutionResult.Resolved(conflictGroup.first())
        }
        
        val metricType = conflictGroup.first().type
        
        // Strategy 1: Check if values are similar enough to not be a real conflict
        if (areValuesSimilar(conflictGroup)) {
            // Use the most reliable source
            val bestMetric = selectMostReliableMetric(conflictGroup)
            return ConflictResolutionResult.Resolved(bestMetric)
        }
        
        // Strategy 2: Use source-specific resolution rules
        val sourceBasedResolution = resolveBySourcePriority(conflictGroup)
        if (sourceBasedResolution != null) {
            return ConflictResolutionResult.Resolved(sourceBasedResolution)
        }
        
        // Strategy 3: Use temporal resolution (latest wins for certain types)
        val temporalResolution = resolveByTemporal(conflictGroup)
        if (temporalResolution != null) {
            return ConflictResolutionResult.Resolved(temporalResolution)
        }
        
        // Strategy 4: Use statistical resolution (average, median, etc.)
        val statisticalResolution = resolveByStatistics(conflictGroup)
        if (statisticalResolution != null) {
            return ConflictResolutionResult.Resolved(statisticalResolution)
        }
        
        // If all automatic strategies fail, require manual resolution
        return ConflictResolutionResult.RequiresManualResolution(
            HealthDataConflict(
                id = generateConflictId(),
                userId = userId,
                metricType = metricType,
                conflictingMetrics = conflictGroup,
                detectedAt = LocalDateTime.now(),
                resolutionStrategy = ConflictResolutionStrategy.MANUAL,
                isResolved = false
            )
        )
    }
    
    /**
     * Checks if values in a conflict group are similar enough to not be a real conflict
     */
    private fun areValuesSimilar(metrics: List<HealthMetric>): Boolean {
        if (metrics.size < 2) return true
        
        val values = metrics.map { it.value }
        val average = values.average()
        
        // Check if all values are within the threshold percentage of the average
        return values.all { value ->
            val percentageDifference = abs(value - average) / average
            percentageDifference <= VALUE_CONFLICT_THRESHOLD
        }
    }
    
    /**
     * Selects the most reliable metric from a group
     */
    private fun selectMostReliableMetric(metrics: List<HealthMetric>): HealthMetric {
        return healthDataPrioritizer.prioritizeAndDeduplicate(metrics).first()
    }
    
    /**
     * Resolves conflict by source priority
     */
    private fun resolveBySourcePriority(metrics: List<HealthMetric>): HealthMetric? {
        val metricType = metrics.first().type
        
        // For certain metric types, prefer specific sources
        return when (metricType) {
            HealthMetricType.HRV, HealthMetricType.TRAINING_RECOVERY, HealthMetricType.BIOLOGICAL_AGE -> {
                // Prefer Garmin for these metrics
                metrics.find { it.source == DataSource.GARMIN }
                    ?: metrics.find { it.source == DataSource.MANUAL_ENTRY }
            }
            
            HealthMetricType.ECG, HealthMetricType.BODY_FAT_PERCENTAGE -> {
                // Prefer Samsung Health for these metrics
                metrics.find { it.source == DataSource.SAMSUNG_HEALTH }
                    ?: metrics.find { it.source == DataSource.MANUAL_ENTRY }
            }
            
            in listOf(
                HealthMetricType.TESTOSTERONE, HealthMetricType.ESTRADIOL, HealthMetricType.CORTISOL,
                HealthMetricType.VITAMIN_D3, HealthMetricType.VITAMIN_B12, HealthMetricType.HBA1C
            ) -> {
                // Always prefer blood test data for biomarkers
                metrics.find { it.source == DataSource.BLOOD_TEST }
                    ?: metrics.find { it.source == DataSource.MANUAL_ENTRY }
            }
            
            else -> {
                // Use general prioritization
                healthDataPrioritizer.prioritizeAndDeduplicate(metrics).firstOrNull()
            }
        }
    }
    
    /**
     * Resolves conflict by temporal strategy (latest wins for certain types)
     */
    private fun resolveByTemporal(metrics: List<HealthMetric>): HealthMetric? {
        val metricType = metrics.first().type
        
        // For certain metrics, the latest reading is most accurate
        return when (metricType) {
            HealthMetricType.WEIGHT, HealthMetricType.BLOOD_PRESSURE, HealthMetricType.BLOOD_GLUCOSE -> {
                // Latest measurement is most relevant
                metrics.maxByOrNull { 
                    LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                }
            }
            
            HealthMetricType.STEPS, HealthMetricType.CALORIES_BURNED, HealthMetricType.HYDRATION -> {
                // For cumulative metrics, sum might be more appropriate, but latest is safer
                metrics.maxByOrNull { 
                    LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                }
            }
            
            else -> null // Don't use temporal resolution for other types
        }
    }
    
    /**
     * Resolves conflict using statistical methods
     */
    private fun resolveByStatistics(metrics: List<HealthMetric>): HealthMetric? {
        val metricType = metrics.first().type
        
        return when (metricType) {
            HealthMetricType.HEART_RATE, HealthMetricType.BLOOD_PRESSURE -> {
                // Use median for vital signs to avoid outliers
                val sortedValues = metrics.map { it.value }.sorted()
                val medianValue = if (sortedValues.size % 2 == 0) {
                    (sortedValues[sortedValues.size / 2 - 1] + sortedValues[sortedValues.size / 2]) / 2.0
                } else {
                    sortedValues[sortedValues.size / 2]
                }
                
                // Find the metric closest to the median
                metrics.minByOrNull { abs(it.value - medianValue) }
            }
            
            HealthMetricType.SLEEP_DURATION, HealthMetricType.EXERCISE_DURATION -> {
                // Use average for duration metrics
                val averageValue = metrics.map { it.value }.average()
                
                // Find the metric closest to the average
                metrics.minByOrNull { abs(it.value - averageValue) }
            }
            
            else -> null // Don't use statistical resolution for other types
        }
    }
    
    /**
     * Generates a unique conflict ID
     */
    private fun generateConflictId(): String {
        return "conflict_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * Logs unresolved conflicts for manual review
     */
    private suspend fun logUnresolvedConflicts(
        userId: String,
        conflicts: List<HealthDataConflict>
    ) {
        // In a real implementation, this would save to a conflicts table
        // or send to a logging service for manual review
        conflicts.forEach { conflict ->
            println("Unresolved health data conflict for user $userId: ${conflict.metricType} with ${conflict.conflictingMetrics.size} conflicting values")
        }
    }
    
    /**
     * Manually resolves a conflict with user input
     */
    suspend fun manuallyResolveConflict(
        conflictId: String,
        selectedMetricId: String,
        resolutionStrategy: ConflictResolutionStrategy
    ): ConflictResolutionResult {
        // In a real implementation, this would:
        // 1. Load the conflict from storage
        // 2. Apply the user's choice
        // 3. Update the conflict status
        // 4. Return the resolved metric
        
        // For now, return a placeholder
        return ConflictResolutionResult.RequiresManualResolution(
            HealthDataConflict(
                id = conflictId,
                userId = "",
                metricType = HealthMetricType.HEART_RATE,
                conflictingMetrics = emptyList(),
                detectedAt = LocalDateTime.now(),
                resolutionStrategy = resolutionStrategy,
                isResolved = true
            )
        )
    }
    
    /**
     * Gets all unresolved conflicts for a user
     */
    fun getUnresolvedConflicts(userId: String): Flow<List<HealthDataConflict>> = flow {
        // In a real implementation, this would query a conflicts table
        emit(emptyList())
    }
    
    /**
     * Applies a resolution strategy to resolve conflicts automatically
     */
    suspend fun applyResolutionStrategy(
        userId: String,
        strategy: ConflictResolutionStrategy
    ): ConflictBatchResolutionResult {
        val unresolvedConflicts = getUnresolvedConflicts(userId)
        var resolvedCount = 0
        var failedCount = 0
        val errors = mutableListOf<String>()
        
        unresolvedConflicts.collect { conflicts ->
            for (conflict in conflicts) {
                try {
                    val resolution = when (strategy) {
                        ConflictResolutionStrategy.LOCAL_WINS -> {
                            // Prefer local/manual entry data
                            val localMetric = conflict.conflictingMetrics.find { 
                                it.source == DataSource.MANUAL_ENTRY 
                            } ?: conflict.conflictingMetrics.first()
                            ConflictResolutionResult.Resolved(localMetric)
                        }
                        
                        ConflictResolutionStrategy.CLOUD_WINS -> {
                            // Prefer cloud/platform data
                            val cloudMetric = conflict.conflictingMetrics.find { 
                                it.source != DataSource.MANUAL_ENTRY 
                            } ?: conflict.conflictingMetrics.first()
                            ConflictResolutionResult.Resolved(cloudMetric)
                        }
                        
                        ConflictResolutionStrategy.LATEST_WINS -> {
                            // Use the latest timestamp
                            val latestMetric = conflict.conflictingMetrics.maxByOrNull { 
                                LocalDateTime.parse(it.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME) 
                            } ?: conflict.conflictingMetrics.first()
                            ConflictResolutionResult.Resolved(latestMetric)
                        }
                        
                        ConflictResolutionStrategy.MANUAL -> {
                            // Skip manual conflicts in batch resolution
                            continue
                        }
                    }
                    
                    if (resolution is ConflictResolutionResult.Resolved) {
                        resolvedCount++
                    }
                } catch (e: Exception) {
                    failedCount++
                    errors.add("Failed to resolve conflict ${conflict.id}: ${e.message}")
                }
            }
        }
        
        return ConflictBatchResolutionResult(
            totalConflicts = resolvedCount + failedCount,
            resolvedConflicts = resolvedCount,
            failedConflicts = failedCount,
            errors = errors
        )
    }
}

/**
 * Result of conflict resolution
 */
sealed class ConflictResolutionResult {
    data class Resolved(val resolvedMetric: HealthMetric) : ConflictResolutionResult()
    data class RequiresManualResolution(val conflict: HealthDataConflict) : ConflictResolutionResult()
}

/**
 * Represents a health data conflict
 */
data class HealthDataConflict(
    val id: String,
    val userId: String,
    val metricType: HealthMetricType,
    val conflictingMetrics: List<HealthMetric>,
    val detectedAt: LocalDateTime,
    val resolutionStrategy: ConflictResolutionStrategy,
    val isResolved: Boolean,
    val resolvedMetricId: String? = null,
    val resolvedAt: LocalDateTime? = null
)

/**
 * Result of batch conflict resolution
 */
data class ConflictBatchResolutionResult(
    val totalConflicts: Int,
    val resolvedConflicts: Int,
    val failedConflicts: Int,
    val errors: List<String>
)