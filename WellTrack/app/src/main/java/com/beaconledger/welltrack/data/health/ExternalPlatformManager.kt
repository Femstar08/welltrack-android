package com.beaconledger.welltrack.data.health

import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalPlatformManager @Inject constructor(
    private val healthConnectManager: HealthConnectManager,
    private val garminConnectManager: GarminConnectManager,
    private val samsungHealthManager: SamsungHealthManager,
    private val healthDataPrioritizer: HealthDataPrioritizer,
    private val healthMetricDao: HealthMetricDao
) {
    
    /**
     * Sync health data from all available external platforms
     */
    suspend fun syncAllPlatforms(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): Result<List<HealthMetric>> {
        return try {
            val allMetrics = mutableListOf<HealthMetric>()
            
            coroutineScope {
                val syncTasks = listOf(
                    // Sync from Health Connect
                    async {
                        if (healthConnectManager.isAvailable()) {
                            try {
                                healthConnectManager.syncHealthData(userId, startTime, endTime).first()
                            } catch (e: Exception) {
                                emptyList<HealthMetric>()
                            }
                        } else {
                            emptyList<HealthMetric>()
                        }
                    },
                    
                    // Sync from Garmin Connect
                    async {
                        if (garminConnectManager.isAuthenticated()) {
                            try {
                                garminConnectManager.syncHealthData(userId, startTime, endTime).first()
                            } catch (e: Exception) {
                                emptyList<HealthMetric>()
                            }
                        } else {
                            emptyList<HealthMetric>()
                        }
                    },
                    
                    // Sync from Samsung Health
                    async {
                        if (samsungHealthManager.isAvailable()) {
                            try {
                                samsungHealthManager.syncHealthData(userId, startTime, endTime).first()
                            } catch (e: Exception) {
                                emptyList<HealthMetric>()
                            }
                        } else {
                            emptyList<HealthMetric>()
                        }
                    }
                )
                
                // Wait for all sync operations to complete
                val results = syncTasks.awaitAll()
                results.forEach { metrics ->
                    allMetrics.addAll(metrics)
                }
            }
            
            // Get existing metrics from database for deduplication
            val existingMetrics = getExistingMetrics(userId, startTime, endTime)
            
            // Prioritize and deduplicate all metrics
            val prioritizedMetrics = healthDataPrioritizer.filterOutdatedMetrics(
                existingMetrics, allMetrics
            )
            
            // Save new/updated metrics to database
            val newMetrics = prioritizedMetrics.filter { metric ->
                existingMetrics.none { existing -> 
                    existing.id == metric.id || 
                    (existing.type == metric.type && 
                     existing.timestamp == metric.timestamp && 
                     existing.source == metric.source)
                }
            }
            
            newMetrics.forEach { metric ->
                healthMetricDao.insertHealthMetric(metric)
            }
            
            Result.success(prioritizedMetrics)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get available platforms and their connection status
     */
    suspend fun getPlatformStatus(): Map<String, PlatformStatus> {
        return mapOf(
            "Health Connect" to PlatformStatus(
                isAvailable = healthConnectManager.isAvailable(),
                isConnected = healthConnectManager.hasAllPermissions(),
                supportedMetrics = getHealthConnectSupportedMetrics()
            ),
            "Garmin Connect" to PlatformStatus(
                isAvailable = true, // Always available if configured
                isConnected = garminConnectManager.isAuthenticated(),
                supportedMetrics = getGarminSupportedMetrics()
            ),
            "Samsung Health" to PlatformStatus(
                isAvailable = samsungHealthManager.isAvailable(),
                isConnected = samsungHealthManager.hasAllPermissions(),
                supportedMetrics = getSamsungHealthSupportedMetrics()
            )
        )
    }
    
    /**
     * Connect to a specific platform
     */
    suspend fun connectToPlatform(platform: String): Result<Unit> {
        return when (platform.lowercase()) {
            "health connect" -> {
                if (healthConnectManager.hasAllPermissions()) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Health Connect permissions not granted"))
                }
            }
            "garmin connect" -> {
                if (garminConnectManager.isAuthenticated()) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("Garmin Connect not authenticated"))
                }
            }
            "samsung health" -> {
                samsungHealthManager.initialize()
                Result.success(Unit)
            }
            else -> Result.failure(Exception("Unknown platform: $platform"))
        }
    }
    
    /**
     * Get health metrics with data source information
     */
    suspend fun getHealthMetricsWithSources(
        userId: String,
        type: HealthMetricType? = null,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<HealthMetricWithSource>> = flow {
        val metrics = if (type != null) {
            healthMetricDao.getHealthMetricsByTypeAndDateRange(
                userId, type, startDate.toString(), endDate.toString()
            ).first()
        } else {
            healthMetricDao.getHealthMetricsByDateRange(
                userId, startDate.toString(), endDate.toString()
            ).first()
        }
        
        val metricsWithSources = metrics.map { metric ->
            HealthMetricWithSource(
                metric = metric,
                qualityScore = healthDataPrioritizer.getDataQualityScore(metric),
                isPreferredSource = isPreferredSourceForMetric(metric)
            )
        }
        
        emit(metricsWithSources)
    }
    
    /**
     * Identify missing health metrics that could be manually entered
     */
    suspend fun identifyMissingMetrics(
        userId: String,
        requiredTypes: Set<HealthMetricType>,
        timeRange: Pair<LocalDateTime, LocalDateTime>
    ): List<HealthMetricType> {
        val existingMetrics = healthMetricDao.getHealthMetricsByDateRange(
            userId, timeRange.first.toString(), timeRange.second.toString()
        ).first()
        
        return healthDataPrioritizer.identifyDataGaps(
            existingMetrics, requiredTypes, timeRange
        )
    }
    
    /**
     * Get manual entry suggestions for critical health metrics
     */
    suspend fun getManualEntrySuggestions(
        userId: String,
        criticalTypes: Set<HealthMetricType>
    ): List<ManualEntrySuggestion> {
        val recentMetrics = healthMetricDao.getHealthMetricsByDateRange(
            userId,
            LocalDateTime.now().minusDays(7).toString(),
            LocalDateTime.now().toString()
        ).first()
        
        val missingTypes = healthDataPrioritizer.suggestManualEntry(
            userId, recentMetrics, criticalTypes
        )
        
        return missingTypes.map { type ->
            ManualEntrySuggestion(
                metricType = type,
                reason = getManualEntryReason(type, recentMetrics),
                priority = getManualEntryPriority(type),
                lastRecorded = getLastRecordedDate(type, recentMetrics)
            )
        }
    }
    
    private suspend fun getExistingMetrics(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): List<HealthMetric> {
        val startDate = startTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
        val endDate = endTime.atZone(ZoneId.systemDefault()).toLocalDateTime()
        
        return healthMetricDao.getHealthMetricsByDateRange(
            userId, startDate.toString(), endDate.toString()
        ).first()
    }
    
    private fun isPreferredSourceForMetric(metric: HealthMetric): Boolean {
        val preferredSources = when (metric.type) {
            HealthMetricType.HRV -> listOf("Garmin Connect", "Samsung Health")
            HealthMetricType.TRAINING_RECOVERY -> listOf("Garmin Connect")
            HealthMetricType.ECG -> listOf("Samsung Health")
            HealthMetricType.BODY_FAT_PERCENTAGE -> listOf("Samsung Health")
            HealthMetricType.MUSCLE_MASS -> listOf("Samsung Health")
            else -> emptyList()
        }
        
        val sourceName = when (metric.source) {
            com.beaconledger.welltrack.data.model.DataSource.GARMIN -> "Garmin Connect"
            com.beaconledger.welltrack.data.model.DataSource.SAMSUNG_HEALTH -> "Samsung Health"
            com.beaconledger.welltrack.data.model.DataSource.HEALTH_CONNECT -> "Health Connect"
            else -> "Other"
        }
        
        return sourceName in preferredSources
    }
    
    private fun getHealthConnectSupportedMetrics(): List<HealthMetricType> {
        return listOf(
            HealthMetricType.STEPS,
            HealthMetricType.HEART_RATE,
            HealthMetricType.WEIGHT,
            HealthMetricType.CALORIES_BURNED,
            HealthMetricType.BLOOD_PRESSURE,
            HealthMetricType.BLOOD_GLUCOSE,
            HealthMetricType.BODY_FAT_PERCENTAGE,
            HealthMetricType.SLEEP_DURATION,
            HealthMetricType.EXERCISE_DURATION,
            HealthMetricType.HYDRATION,
            HealthMetricType.VO2_MAX,
            HealthMetricType.MUSCLE_MASS
        )
    }
    
    private fun getGarminSupportedMetrics(): List<HealthMetricType> {
        return listOf(
            HealthMetricType.HRV,
            HealthMetricType.TRAINING_RECOVERY,
            HealthMetricType.STRESS_SCORE,
            HealthMetricType.BIOLOGICAL_AGE,
            HealthMetricType.SLEEP_DURATION,
            HealthMetricType.STEPS,
            HealthMetricType.HEART_RATE,
            HealthMetricType.CALORIES_BURNED,
            HealthMetricType.VO2_MAX
        )
    }
    
    private fun getSamsungHealthSupportedMetrics(): List<HealthMetricType> {
        return listOf(
            HealthMetricType.ECG,
            HealthMetricType.BODY_FAT_PERCENTAGE,
            HealthMetricType.MUSCLE_MASS,
            HealthMetricType.STEPS,
            HealthMetricType.HEART_RATE,
            HealthMetricType.SLEEP_DURATION,
            HealthMetricType.WEIGHT,
            HealthMetricType.BLOOD_PRESSURE,
            HealthMetricType.HYDRATION
        )
    }
    
    private fun getManualEntryReason(
        type: HealthMetricType,
        recentMetrics: List<HealthMetric>
    ): String {
        val lastMetric = recentMetrics
            .filter { it.type == type }
            .maxByOrNull { it.timestamp }
        
        return if (lastMetric == null) {
            "No recent data available for ${type.name.lowercase().replace('_', ' ')}"
        } else {
            "Last recorded ${type.name.lowercase().replace('_', ' ')} was more than 7 days ago"
        }
    }
    
    private fun getManualEntryPriority(type: HealthMetricType): ManualEntryPriority {
        return when (type) {
            HealthMetricType.BLOOD_PRESSURE,
            HealthMetricType.BLOOD_GLUCOSE,
            HealthMetricType.WEIGHT -> ManualEntryPriority.HIGH
            
            HealthMetricType.BODY_FAT_PERCENTAGE,
            HealthMetricType.MUSCLE_MASS,
            HealthMetricType.HRV -> ManualEntryPriority.MEDIUM
            
            else -> ManualEntryPriority.LOW
        }
    }
    
    private fun getLastRecordedDate(
        type: HealthMetricType,
        recentMetrics: List<HealthMetric>
    ): LocalDateTime? {
        return recentMetrics
            .filter { it.type == type }
            .maxByOrNull { it.timestamp }
            ?.let { LocalDateTime.parse(it.timestamp) }
    }
}

data class PlatformStatus(
    val isAvailable: Boolean,
    val isConnected: Boolean,
    val supportedMetrics: List<HealthMetricType>
)

data class HealthMetricWithSource(
    val metric: HealthMetric,
    val qualityScore: Int,
    val isPreferredSource: Boolean
)

data class ManualEntrySuggestion(
    val metricType: HealthMetricType,
    val reason: String,
    val priority: ManualEntryPriority,
    val lastRecorded: LocalDateTime?
)

enum class ManualEntryPriority {
    HIGH, MEDIUM, LOW
}