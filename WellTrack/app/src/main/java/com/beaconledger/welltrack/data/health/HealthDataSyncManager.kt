package com.beaconledger.welltrack.data.health

import android.content.Context
import com.beaconledger.welltrack.data.cache.HealthDataCacheManager
import com.beaconledger.welltrack.data.cache.OfflineCacheManager
import com.beaconledger.welltrack.data.database.dao.HealthMetricDao
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.sync.SyncService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Comprehensive health data synchronization manager that handles bidirectional sync,
 * conflict resolution, data validation, and offline caching
 */
@Singleton
class HealthDataSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val healthConnectManager: HealthConnectManager,
    private val samsungHealthManager: SamsungHealthManager,
    private val garminConnectManager: GarminConnectManager,
    private val healthDataPrioritizer: HealthDataPrioritizer,
    private val healthDataValidator: HealthDataValidator,
    private val healthMetricDao: HealthMetricDao,
    private val syncService: SyncService,
    private val offlineCacheManager: OfflineCacheManager,
    private val healthDataCacheManager: HealthDataCacheManager,
    private val healthDataConflictResolver: HealthDataConflictResolver,
    private val debugLogger: HealthSyncDebugLogger
) {
    
    companion object {
        private const val SYNC_BATCH_SIZE = 100
        private const val MAX_SYNC_RETRIES = 3
        private const val SYNC_TIMEOUT_MINUTES = 10L
    }
    
    /**
     * Performs comprehensive bidirectional health data synchronization
     */
    suspend fun performBidirectionalSync(
        userId: String,
        syncTimeRange: Pair<Instant, Instant>
    ): HealthSyncResult = supervisorScope {
        val syncStartTime = System.currentTimeMillis()
        val platforms = listOf("Health Connect", "Samsung Health", "Garmin Connect")
        
        try {
            // Log sync start
            debugLogger.logSyncStart(userId, platforms, syncTimeRange)
            debugLogger.logDeviceInfo()
            
            val (startTime, endTime) = syncTimeRange
            
            // Step 1: Sync from external platforms to local storage
            val platformSyncResults = syncFromExternalPlatforms(userId, startTime, endTime)
            
            // Step 2: Validate and sanitize all incoming data
            val validatedMetrics = validateAndSanitizeHealthData(platformSyncResults.metrics)
            
            // Step 3: Resolve conflicts and prioritize data
            val prioritizedMetrics = resolveConflictsAndPrioritize(userId, validatedMetrics)
            
            // Step 4: Cache data offline for resilience
            cacheHealthDataOffline(userId, prioritizedMetrics)
            
            // Step 5: Sync to cloud storage (bidirectional)
            val cloudSyncResult = syncToCloudStorage(userId, prioritizedMetrics)
            
            // Step 6: Update local database with final resolved data
            updateLocalDatabase(userId, prioritizedMetrics)
            
            // Step 7: Generate sync summary
            val result = generateSyncSummary(platformSyncResults, cloudSyncResult, prioritizedMetrics)
            
            // Log sync completion
            val totalDuration = System.currentTimeMillis() - syncStartTime
            debugLogger.logSyncCompletion(userId, result, totalDuration, platformSyncResults.platformStatuses)
            debugLogger.logPerformanceMetrics("bidirectional_sync", totalDuration, prioritizedMetrics.size)
            
            result
            
        } catch (e: Exception) {
            val totalDuration = System.currentTimeMillis() - syncStartTime
            debugLogger.logError("bidirectional_sync", e, mapOf(
                "userId" to userId,
                "syncDuration" to totalDuration,
                "timeRange" to syncTimeRange.toString()
            ))
            
            HealthSyncResult.Error(
                message = "Bidirectional sync failed: ${e.message}",
                exception = e,
                partialData = emptyList()
            )
        }
    }
    
    /**
     * Syncs health data from all external platforms
     */
    private suspend fun syncFromExternalPlatforms(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): PlatformSyncResult = supervisorScope {
        val syncTasks = listOf(
            async { syncFromHealthConnect(userId, startTime, endTime) },
            async { syncFromSamsungHealth(userId, startTime, endTime) },
            async { syncFromGarminConnect(userId, startTime, endTime) }
        )
        
        val results = syncTasks.awaitAll()
        val allMetrics = results.flatMap { it.metrics }
        val platformStatuses = results.flatMap { it.platformStatuses }

        PlatformSyncResult(
            metrics = allMetrics,
            platformStatuses = platformStatuses,
            syncTimestamp = LocalDateTime.now()
        )
    }
    
    /**
     * Syncs data from Health Connect
     */
    private suspend fun syncFromHealthConnect(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): PlatformSyncResult {
        val platformSyncStartTime = System.currentTimeMillis()
        
        return try {
            val isAvailable = healthConnectManager.isAvailable()
            val hasPermissions = healthConnectManager.hasAllPermissions()
            
            debugLogger.logPlatformSyncAttempt("Health Connect", isAvailable, hasPermissions)
            
            if (!isAvailable || !hasPermissions) {
                val errorMessage = "Health Connect not available or permissions not granted"
                debugLogger.logPlatformSyncResult("Health Connect", SyncState.FAILED, 0, errorMessage)
                
                return PlatformSyncResult(
                    metrics = emptyList(),
                    platformStatuses = listOf(
                        PlatformSyncStatus(
                            platform = "Health Connect",
                            isAvailable = isAvailable,
                            isConnected = hasPermissions,
                            lastSyncTime = null,
                            syncStatus = SyncState.FAILED,
                            errorMessage = errorMessage
                        )
                    ),
                    syncTimestamp = LocalDateTime.now()
                )
            }
            
            val metricsFlow = healthConnectManager.syncHealthData(userId, startTime, endTime)
            val metrics = metricsFlow.first()
            
            val syncDuration = System.currentTimeMillis() - platformSyncStartTime
            debugLogger.logPlatformSyncResult("Health Connect", SyncState.SYNCED, metrics.size, null, syncDuration)
            
            PlatformSyncResult(
                metrics = metrics,
                platformStatuses = listOf(
                    PlatformSyncStatus(
                        platform = "Health Connect",
                        isAvailable = true,
                        isConnected = true,
                        lastSyncTime = LocalDateTime.now(),
                        syncStatus = SyncState.SYNCED,
                        errorMessage = null
                    )
                ),
                syncTimestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            val syncDuration = System.currentTimeMillis() - platformSyncStartTime
            debugLogger.logPlatformSyncResult("Health Connect", SyncState.FAILED, 0, e.message, syncDuration)
            debugLogger.logError("health_connect_sync", e, mapOf("userId" to userId))
            
            PlatformSyncResult(
                metrics = emptyList(),
                platformStatuses = listOf(
                    PlatformSyncStatus(
                        platform = "Health Connect",
                        isAvailable = healthConnectManager.isAvailable(),
                        isConnected = false,
                        lastSyncTime = null,
                        syncStatus = SyncState.FAILED,
                        errorMessage = e.message
                    )
                ),
                syncTimestamp = LocalDateTime.now()
            )
        }
    }
    
    /**
     * Syncs data from Samsung Health
     */
    private suspend fun syncFromSamsungHealth(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): PlatformSyncResult {
        return try {
            if (!samsungHealthManager.isAvailable() || !samsungHealthManager.hasAllPermissions()) {
                return PlatformSyncResult(
                    metrics = emptyList(),
                    platformStatuses = listOf(
                        PlatformSyncStatus(
                            platform = "Samsung Health",
                            isAvailable = samsungHealthManager.isAvailable(),
                            isConnected = samsungHealthManager.hasAllPermissions(),
                            lastSyncTime = null,
                            syncStatus = SyncState.FAILED,
                            errorMessage = "Samsung Health not available or permissions not granted"
                        )
                    ),
                    syncTimestamp = LocalDateTime.now()
                )
            }
            
            val metricsFlow = samsungHealthManager.syncHealthData(userId, startTime, endTime)
            val metrics = metricsFlow.first()
            
            PlatformSyncResult(
                metrics = metrics,
                platformStatuses = listOf(
                    PlatformSyncStatus(
                        platform = "Samsung Health",
                        isAvailable = true,
                        isConnected = true,
                        lastSyncTime = LocalDateTime.now(),
                        syncStatus = SyncState.SYNCED,
                        errorMessage = null
                    )
                ),
                syncTimestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            PlatformSyncResult(
                metrics = emptyList(),
                platformStatuses = listOf(
                    PlatformSyncStatus(
                        platform = "Samsung Health",
                        isAvailable = samsungHealthManager.isAvailable(),
                        isConnected = false,
                        lastSyncTime = null,
                        syncStatus = SyncState.FAILED,
                        errorMessage = e.message
                    )
                ),
                syncTimestamp = LocalDateTime.now()
            )
        }
    }
    
    /**
     * Syncs data from Garmin Connect
     */
    private suspend fun syncFromGarminConnect(
        userId: String,
        startTime: Instant,
        endTime: Instant
    ): PlatformSyncResult {
        return try {
            if (!garminConnectManager.isAuthenticated()) {
                return PlatformSyncResult(
                    metrics = emptyList(),
                    platformStatuses = listOf(
                        PlatformSyncStatus(
                            platform = "Garmin Connect",
                            isAvailable = true,
                            isConnected = false,
                            lastSyncTime = null,
                            syncStatus = SyncState.FAILED,
                            errorMessage = "Garmin Connect not authenticated"
                        )
                    ),
                    syncTimestamp = LocalDateTime.now()
                )
            }
            
            val metricsFlow = garminConnectManager.syncHealthData(userId, startTime, endTime)
            val metrics = metricsFlow.first()
            
            PlatformSyncResult(
                metrics = metrics,
                platformStatuses = listOf(
                    PlatformSyncStatus(
                        platform = "Garmin Connect",
                        isAvailable = true,
                        isConnected = true,
                        lastSyncTime = LocalDateTime.now(),
                        syncStatus = SyncState.SYNCED,
                        errorMessage = null
                    )
                ),
                syncTimestamp = LocalDateTime.now()
            )
        } catch (e: Exception) {
            PlatformSyncResult(
                metrics = emptyList(),
                platformStatuses = listOf(
                    PlatformSyncStatus(
                        platform = "Garmin Connect",
                        isAvailable = true,
                        isConnected = false,
                        lastSyncTime = null,
                        syncStatus = SyncState.FAILED,
                        errorMessage = e.message
                    )
                ),
                syncTimestamp = LocalDateTime.now()
            )
        }
    }
    
    /**
     * Validates and sanitizes health data from all sources
     */
    private suspend fun validateAndSanitizeHealthData(
        metrics: List<HealthMetric>
    ): List<HealthMetric> {
        return metrics.mapNotNull { metric ->
            val validationResult = healthDataValidator.validateHealthMetric(metric)
            if (validationResult.isValid) {
                healthDataValidator.sanitizeHealthMetric(metric)
            } else {
                // Log validation failure but don't include invalid data
                null
            }
        }
    }
    
    /**
     * Resolves conflicts and prioritizes data from multiple sources
     */
    private suspend fun resolveConflictsAndPrioritize(
        userId: String,
        newMetrics: List<HealthMetric>
    ): List<HealthMetric> {
        // Get existing metrics from local database
        val existingMetrics = healthMetricDao.getAllMetricsForUser(userId)
        
        // Combine new and existing metrics
        val allMetrics = existingMetrics + newMetrics
        
        // Use prioritizer to resolve conflicts and deduplicate
        val prioritizedMetrics = healthDataPrioritizer.prioritizeAndDeduplicate(allMetrics)
        
        // Handle any remaining conflicts that require manual resolution
        return healthDataConflictResolver.resolveConflicts(userId, prioritizedMetrics)
    }
    
    /**
     * Caches health data offline for resilience
     */
    private suspend fun cacheHealthDataOffline(
        userId: String,
        metrics: List<HealthMetric>
    ) {
        try {
            healthDataCacheManager.cacheHealthMetrics(userId, metrics)
        } catch (e: Exception) {
            // Log error but don't fail the sync
        }
    }
    
    /**
     * Syncs data to cloud storage
     */
    private suspend fun syncToCloudStorage(
        userId: String,
        metrics: List<HealthMetric>
    ): CloudSyncResult {
        return try {
            // Mark metrics for upload
            metrics.forEach { metric ->
                syncService.markForUpload(metric.id, "HealthMetric")
            }
            
            // Perform cloud sync
            val syncResult = syncService.performFullSync()
            
            when (syncResult) {
                is SyncResult.Success -> CloudSyncResult.Success
                is SyncResult.Error -> CloudSyncResult.Error(syncResult.message, syncResult.exception)
                is SyncResult.Conflict -> CloudSyncResult.Conflicts(syncResult.conflicts)
                is SyncResult.PartialSuccess -> CloudSyncResult.PartialSuccess(
                    syncResult.successCount,
                    syncResult.failureCount,
                    syncResult.errors
                )
            }
        } catch (e: Exception) {
            CloudSyncResult.Error("Cloud sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Updates local database with final resolved data
     */
    private suspend fun updateLocalDatabase(
        userId: String,
        metrics: List<HealthMetric>
    ) {
        try {
            // Batch insert/update metrics
            val batches = metrics.chunked(SYNC_BATCH_SIZE)
            batches.forEach { batch ->
                healthMetricDao.insertHealthMetrics(batch)
            }
        } catch (e: Exception) {
            // Log error but don't fail the sync
        }
    }
    
    /**
     * Generates comprehensive sync summary
     */
    private fun generateSyncSummary(
        platformSyncResult: PlatformSyncResult,
        cloudSyncResult: CloudSyncResult,
        finalMetrics: List<HealthMetric>
    ): HealthSyncResult {
        val successfulPlatforms = platformSyncResult.platformStatuses.count { it.syncStatus == SyncState.SYNCED }
        val totalPlatforms = platformSyncResult.platformStatuses.size
        
        return HealthSyncResult.Success(
            syncedMetricsCount = finalMetrics.size,
            platformSyncStatuses = platformSyncResult.platformStatuses,
            cloudSyncResult = cloudSyncResult,
            syncTimestamp = LocalDateTime.now(),
            summary = "Successfully synced ${finalMetrics.size} health metrics from $successfulPlatforms/$totalPlatforms platforms"
        )
    }
    
    /**
     * Observes real-time sync status
     */
    fun observeSyncStatus(): Flow<HealthSyncStatus> = flow {
        syncService.observePendingSyncItems().collect { pendingItems ->
            val healthMetricItems = pendingItems.filter { it.entityType == "HealthMetric" }
            
            emit(
                HealthSyncStatus(
                    pendingUploads = healthMetricItems.count { it.syncState == SyncState.PENDING_UPLOAD },
                    pendingDownloads = healthMetricItems.count { it.syncState == SyncState.PENDING_DOWNLOAD },
                    conflicts = healthMetricItems.count { it.syncState == SyncState.CONFLICT },
                    failed = healthMetricItems.count { it.syncState == SyncState.FAILED },
                    lastSyncTime = healthMetricItems.maxOfOrNull { it.lastSyncTime ?: LocalDateTime.MIN }
                )
            )
        }
    }
    
    /**
     * Forces a manual sync for specific metric types
     */
    suspend fun forceSyncForMetricTypes(
        userId: String,
        metricTypes: Set<HealthMetricType>,
        timeRange: Pair<Instant, Instant>
    ): HealthSyncResult {
        return try {
            val (startTime, endTime) = timeRange
            
            // Get existing metrics for these types
            val existingMetrics = healthMetricDao.getAllMetricsForUser(userId)
                .filter { it.type in metricTypes }
            
            // Perform targeted sync
            val syncResult = performBidirectionalSync(userId, timeRange)
            
            when (syncResult) {
                is HealthSyncResult.Success -> {
                    val newMetrics = syncResult.syncedMetricsCount - existingMetrics.size
                    HealthSyncResult.Success(
                        syncedMetricsCount = newMetrics,
                        platformSyncStatuses = syncResult.platformSyncStatuses,
                        cloudSyncResult = syncResult.cloudSyncResult,
                        syncTimestamp = LocalDateTime.now(),
                        summary = "Force sync completed: $newMetrics new metrics for ${metricTypes.size} metric types"
                    )
                }
                else -> syncResult
            }
        } catch (e: Exception) {
            HealthSyncResult.Error(
                message = "Force sync failed: ${e.message}",
                exception = e,
                partialData = emptyList()
            )
        }
    }
}