package com.beaconledger.welltrack.data.health

import com.beaconledger.welltrack.data.model.DataSource
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthDataPrioritizer @Inject constructor() {
    
    companion object {
        // Data source priority mapping (higher number = higher priority)
        private val DATA_SOURCE_PRIORITY = mapOf(
            DataSource.MANUAL_ENTRY to 100,      // Highest priority - user entered
            DataSource.BLOOD_TEST to 90,         // Medical grade data
            DataSource.GARMIN to 80,             // Advanced fitness tracking
            DataSource.SAMSUNG_HEALTH to 70,     // Good health platform
            DataSource.HEALTH_CONNECT to 60,     // Android health platform
            DataSource.CUSTOM to 50              // Custom integrations
        )
        
        // Metric-specific source preferences
        private val METRIC_SOURCE_PREFERENCES = mapOf(
            HealthMetricType.HRV to listOf(DataSource.GARMIN, DataSource.SAMSUNG_HEALTH),
            HealthMetricType.TRAINING_RECOVERY to listOf(DataSource.GARMIN),
            HealthMetricType.STRESS_SCORE to listOf(DataSource.GARMIN, DataSource.SAMSUNG_HEALTH),
            HealthMetricType.BIOLOGICAL_AGE to listOf(DataSource.GARMIN),
            HealthMetricType.ECG to listOf(DataSource.SAMSUNG_HEALTH),
            HealthMetricType.BODY_FAT_PERCENTAGE to listOf(DataSource.SAMSUNG_HEALTH, DataSource.HEALTH_CONNECT),
            HealthMetricType.MUSCLE_MASS to listOf(DataSource.SAMSUNG_HEALTH, DataSource.HEALTH_CONNECT),
            HealthMetricType.BLOOD_PRESSURE to listOf(DataSource.MANUAL_ENTRY, DataSource.HEALTH_CONNECT),
            HealthMetricType.BLOOD_GLUCOSE to listOf(DataSource.MANUAL_ENTRY, DataSource.HEALTH_CONNECT),
            HealthMetricType.WEIGHT to listOf(DataSource.MANUAL_ENTRY, DataSource.HEALTH_CONNECT, DataSource.SAMSUNG_HEALTH),
            HealthMetricType.STEPS to listOf(DataSource.GARMIN, DataSource.SAMSUNG_HEALTH, DataSource.HEALTH_CONNECT),
            HealthMetricType.HEART_RATE to listOf(DataSource.GARMIN, DataSource.SAMSUNG_HEALTH, DataSource.HEALTH_CONNECT),
            HealthMetricType.SLEEP_DURATION to listOf(DataSource.GARMIN, DataSource.SAMSUNG_HEALTH, DataSource.HEALTH_CONNECT)
        )
        
        // Time window for considering metrics as duplicates (in minutes)
        private const val DUPLICATE_TIME_WINDOW_MINUTES = 30L
    }
    
    /**
     * Prioritize and deduplicate health metrics from multiple sources
     */
    fun prioritizeAndDeduplicate(metrics: List<HealthMetric>): List<HealthMetric> {
        if (metrics.isEmpty()) return emptyList()
        
        // Group metrics by type and time window
        val groupedMetrics = groupMetricsByTypeAndTime(metrics)
        
        // For each group, select the best metric based on priority
        return groupedMetrics.map { group ->
            selectBestMetricFromGroup(group)
        }
    }
    
    /**
     * Group metrics by type and time window to identify potential duplicates
     */
    private fun groupMetricsByTypeAndTime(metrics: List<HealthMetric>): List<List<HealthMetric>> {
        val groups = mutableListOf<MutableList<HealthMetric>>()
        
        metrics.sortedBy { it.timestamp }.forEach { metric ->
            val metricTime = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            
            // Find existing group that this metric could belong to
            val existingGroup = groups.find { group ->
                val groupMetric = group.first()
                groupMetric.type == metric.type && 
                isWithinTimeWindow(metricTime, LocalDateTime.parse(groupMetric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
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
     * Check if two timestamps are within the duplicate detection time window
     */
    private fun isWithinTimeWindow(time1: LocalDateTime, time2: LocalDateTime): Boolean {
        val timeDifference = ChronoUnit.MINUTES.between(time1, time2)
        return kotlin.math.abs(timeDifference) <= DUPLICATE_TIME_WINDOW_MINUTES
    }
    
    /**
     * Select the best metric from a group of potential duplicates
     */
    private fun selectBestMetricFromGroup(group: List<HealthMetric>): HealthMetric {
        if (group.size == 1) return group.first()
        
        val metricType = group.first().type
        
        // Get preferred sources for this metric type
        val preferredSources = METRIC_SOURCE_PREFERENCES[metricType] ?: emptyList()
        
        // First, try to find a metric from preferred sources
        for (preferredSource in preferredSources) {
            val preferredMetric = group.find { it.source == preferredSource }
            if (preferredMetric != null) {
                return preferredMetric
            }
        }
        
        // If no preferred source found, use general priority
        return group.maxByOrNull { getSourcePriority(it.source) } ?: group.first()
    }
    
    /**
     * Get priority score for a data source
     */
    private fun getSourcePriority(source: DataSource): Int {
        return DATA_SOURCE_PRIORITY[source] ?: 0
    }
    
    /**
     * Check if a metric should be considered more reliable than another
     */
    fun isMoreReliable(metric1: HealthMetric, metric2: HealthMetric): Boolean {
        // Same metric type comparison
        if (metric1.type == metric2.type) {
            val priority1 = getSourcePriority(metric1.source)
            val priority2 = getSourcePriority(metric2.source)
            
            // Check metric-specific preferences first
            val preferredSources = METRIC_SOURCE_PREFERENCES[metric1.type]
            if (preferredSources != null) {
                val index1 = preferredSources.indexOf(metric1.source)
                val index2 = preferredSources.indexOf(metric2.source)
                
                when {
                    index1 != -1 && index2 != -1 -> return index1 < index2 // Lower index = higher preference
                    index1 != -1 && index2 == -1 -> return true
                    index1 == -1 && index2 != -1 -> return false
                }
            }
            
            return priority1 > priority2
        }
        
        return false
    }
    
    /**
     * Filter out outdated metrics when newer, more reliable data is available
     */
    fun filterOutdatedMetrics(
        existingMetrics: List<HealthMetric>,
        newMetrics: List<HealthMetric>
    ): List<HealthMetric> {
        val allMetrics = existingMetrics + newMetrics
        return prioritizeAndDeduplicate(allMetrics)
    }
    
    /**
     * Get data quality score for a metric (0-100)
     */
    fun getDataQualityScore(metric: HealthMetric): Int {
        var score = getSourcePriority(metric.source)
        
        // Boost score for preferred sources for this metric type
        val preferredSources = METRIC_SOURCE_PREFERENCES[metric.type]
        if (preferredSources?.contains(metric.source) == true) {
            val preferenceIndex = preferredSources.indexOf(metric.source)
            score += (preferredSources.size - preferenceIndex) * 5
        }
        
        // Boost score for manual entry (user verified)
        if (metric.source == DataSource.MANUAL_ENTRY) {
            score += 20
        }
        
        // Boost score for medical grade data
        if (metric.source == DataSource.BLOOD_TEST) {
            score += 15
        }
        
        return minOf(100, score)
    }
    
    /**
     * Identify gaps in health data that could be filled by manual entry
     */
    fun identifyDataGaps(
        metrics: List<HealthMetric>,
        requiredTypes: Set<HealthMetricType>,
        timeRange: Pair<LocalDateTime, LocalDateTime>
    ): List<HealthMetricType> {
        val availableTypes = metrics.map { it.type }.toSet()
        return requiredTypes.minus(availableTypes).toList()
    }
    
    /**
     * Suggest manual entry for missing critical health metrics
     */
    fun suggestManualEntry(
        userId: String,
        existingMetrics: List<HealthMetric>,
        criticalTypes: Set<HealthMetricType>
    ): List<HealthMetricType> {
        val recentMetrics = existingMetrics.filter { metric ->
            val metricTime = LocalDateTime.parse(metric.timestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val daysSinceMetric = ChronoUnit.DAYS.between(metricTime, LocalDateTime.now())
            daysSinceMetric <= 7 // Consider metrics from last 7 days as recent
        }
        
        val recentTypes = recentMetrics.map { it.type }.toSet()
        return criticalTypes.minus(recentTypes).toList()
    }
}