package com.beaconledger.welltrack.data.cache

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.security.MessageDigest
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages offline caching of health data for resilient synchronization
 */
@Singleton
class HealthDataCacheManager @Inject constructor(
    private val offlineCacheManager: OfflineCacheManager
) {
    
    companion object {
        private const val HEALTH_DATA_CACHE_PREFIX = "health_data_"
        private const val SYNC_QUEUE_CACHE_PREFIX = "sync_queue_"
        private const val DEFAULT_CACHE_EXPIRY_HOURS = 24L
        private const val MAX_CACHE_SIZE_MB = 50L
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Caches health metrics for offline access
     */
    suspend fun cacheHealthMetrics(
        userId: String,
        metrics: List<HealthMetric>,
        expiryHours: Long = DEFAULT_CACHE_EXPIRY_HOURS
    ): Result<Unit> {
        return try {
            val cacheEntries = metrics.map { metric ->
                createCacheEntry(userId, metric, expiryHours)
            }
            
            // Store each cache entry
            cacheEntries.forEach { entry ->
                val cacheKey = generateCacheKey(userId, entry.healthMetric.type, entry.healthMetric.id)
                val serializedEntry = json.encodeToString(entry)
                offlineCacheManager.put(cacheKey, serializedEntry)
            }
            
            // Update cache metadata
            updateCacheMetadata(userId, cacheEntries.size)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Retrieves cached health metrics
     */
    suspend fun getCachedHealthMetrics(
        userId: String,
        metricType: HealthMetricType? = null,
        includeExpired: Boolean = false
    ): List<HealthMetric> {
        return try {
            val cacheKeys = if (metricType != null) {
                listOf(generateCacheKeyPattern(userId, metricType))
            } else {
                getAllCacheKeysForUser(userId)
            }
            
            val cachedMetrics = mutableListOf<HealthMetric>()
            
            for (keyPattern in cacheKeys) {
                val matchingKeys = offlineCacheManager.getKeysMatching(keyPattern)
                
                for (key in matchingKeys) {
                    val cachedData = offlineCacheManager.get(key)
                    if (cachedData != null) {
                        try {
                            val cacheEntry = json.decodeFromString<HealthDataCacheEntry>(cachedData)
                            
                            // Check if entry is expired
                            if (includeExpired || !isCacheEntryExpired(cacheEntry)) {
                                // Verify data integrity
                                if (verifyCacheEntryIntegrity(cacheEntry)) {
                                    cachedMetrics.add(cacheEntry.healthMetric)
                                } else {
                                    // Remove corrupted entry
                                    offlineCacheManager.remove(key)
                                }
                            } else {
                                // Remove expired entry
                                offlineCacheManager.remove(key)
                            }
                        } catch (e: Exception) {
                            // Remove invalid entry
                            offlineCacheManager.remove(key)
                        }
                    }
                }
            }
            
            cachedMetrics
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Adds health metrics to sync queue for offline processing
     */
    suspend fun queueForSync(
        userId: String,
        metrics: List<HealthMetric>,
        operation: SyncOperation,
        targetPlatform: String,
        priority: SyncPriority = SyncPriority.NORMAL
    ): Result<Unit> {
        return try {
            val queueItems = metrics.map { metric ->
                HealthSyncQueueItem(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    operation = operation,
                    healthMetric = metric,
                    targetPlatform = targetPlatform,
                    createdAt = LocalDateTime.now(),
                    priority = priority
                )
            }
            
            // Store queue items
            queueItems.forEach { item ->
                val queueKey = generateSyncQueueKey(userId, item.id)
                val serializedItem = json.encodeToString(item)
                offlineCacheManager.put(queueKey, serializedItem)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets pending sync queue items
     */
    suspend fun getPendingSyncItems(
        userId: String,
        platform: String? = null,
        priority: SyncPriority? = null
    ): List<HealthSyncQueueItem> {
        return try {
            val queueKeyPattern = generateSyncQueueKeyPattern(userId)
            val matchingKeys = offlineCacheManager.getKeysMatching(queueKeyPattern)
            
            val queueItems = mutableListOf<HealthSyncQueueItem>()
            
            for (key in matchingKeys) {
                val cachedData = offlineCacheManager.get(key)
                if (cachedData != null) {
                    try {
                        val queueItem = json.decodeFromString<HealthSyncQueueItem>(cachedData)
                        
                        // Apply filters
                        val matchesPlatform = platform == null || queueItem.targetPlatform == platform
                        val matchesPriority = priority == null || queueItem.priority == priority
                        val notExceededRetries = queueItem.retryCount < queueItem.maxRetries
                        
                        if (matchesPlatform && matchesPriority && notExceededRetries) {
                            queueItems.add(queueItem)
                        }
                    } catch (e: Exception) {
                        // Remove invalid queue item
                        offlineCacheManager.remove(key)
                    }
                }
            }
            
            // Sort by priority and creation time
            queueItems.sortedWith(
                compareByDescending<HealthSyncQueueItem> { it.priority.ordinal }
                    .thenBy { it.createdAt }
            )
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Removes sync queue item after successful processing
     */
    suspend fun removeSyncQueueItem(userId: String, itemId: String): Result<Unit> {
        return try {
            val queueKey = generateSyncQueueKey(userId, itemId)
            offlineCacheManager.remove(queueKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Updates retry count for a sync queue item
     */
    suspend fun updateSyncQueueItemRetryCount(
        userId: String,
        itemId: String,
        errorMessage: String
    ): Result<Unit> {
        return try {
            val queueKey = generateSyncQueueKey(userId, itemId)
            val cachedData = offlineCacheManager.get(queueKey)
            
            if (cachedData != null) {
                val queueItem = json.decodeFromString<HealthSyncQueueItem>(cachedData)
                val updatedItem = queueItem.copy(retryCount = queueItem.retryCount + 1)
                
                if (updatedItem.retryCount < updatedItem.maxRetries) {
                    val serializedItem = json.encodeToString(updatedItem)
                    offlineCacheManager.put(queueKey, serializedItem)
                } else {
                    // Remove item if max retries exceeded
                    offlineCacheManager.remove(queueKey)
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Gets cache statistics
     */
    suspend fun getCacheStatistics(userId: String): HealthCacheStatistics {
        return try {
            val healthDataKeys = getAllCacheKeysForUser(userId)
            val syncQueueKeys = offlineCacheManager.getKeysMatching(generateSyncQueueKeyPattern(userId))
            
            var totalCachedMetrics = 0
            var expiredMetrics = 0
            var totalCacheSize = 0L
            var oldestCacheEntry: LocalDateTime? = null
            
            for (key in healthDataKeys) {
                val cachedData = offlineCacheManager.get(key)
                if (cachedData != null) {
                    totalCacheSize += cachedData.length
                    
                    try {
                        val cacheEntry = json.decodeFromString<HealthDataCacheEntry>(cachedData)
                        totalCachedMetrics++
                        
                        if (isCacheEntryExpired(cacheEntry)) {
                            expiredMetrics++
                        }
                        
                        if (oldestCacheEntry == null || cacheEntry.cachedAt.isBefore(oldestCacheEntry)) {
                            oldestCacheEntry = cacheEntry.cachedAt
                        }
                    } catch (e: Exception) {
                        // Invalid cache entry
                    }
                }
            }
            
            val pendingSyncItems = syncQueueKeys.size
            
            HealthCacheStatistics(
                totalCachedMetrics = totalCachedMetrics,
                expiredMetrics = expiredMetrics,
                pendingSyncItems = pendingSyncItems,
                totalCacheSizeBytes = totalCacheSize,
                oldestCacheEntry = oldestCacheEntry,
                lastUpdated = LocalDateTime.now()
            )
        } catch (e: Exception) {
            HealthCacheStatistics(
                totalCachedMetrics = 0,
                expiredMetrics = 0,
                pendingSyncItems = 0,
                totalCacheSizeBytes = 0L,
                oldestCacheEntry = null,
                lastUpdated = LocalDateTime.now()
            )
        }
    }
    
    /**
     * Cleans up expired cache entries
     */
    suspend fun cleanupExpiredEntries(userId: String): Result<Int> {
        return try {
            val healthDataKeys = getAllCacheKeysForUser(userId)
            var removedCount = 0
            
            for (key in healthDataKeys) {
                val cachedData = offlineCacheManager.get(key)
                if (cachedData != null) {
                    try {
                        val cacheEntry = json.decodeFromString<HealthDataCacheEntry>(cachedData)
                        if (isCacheEntryExpired(cacheEntry)) {
                            offlineCacheManager.remove(key)
                            removedCount++
                        }
                    } catch (e: Exception) {
                        // Remove invalid entries
                        offlineCacheManager.remove(key)
                        removedCount++
                    }
                }
            }
            
            Result.success(removedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Clears all cached data for a user
     */
    suspend fun clearUserCache(userId: String): Result<Unit> {
        return try {
            val healthDataKeys = getAllCacheKeysForUser(userId)
            val syncQueueKeys = offlineCacheManager.getKeysMatching(generateSyncQueueKeyPattern(userId))
            
            (healthDataKeys + syncQueueKeys).forEach { key ->
                offlineCacheManager.remove(key)
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Observes cache status changes
     */
    fun observeCacheStatus(userId: String): Flow<HealthCacheStatus> = flow {
        while (true) {
            val statistics = getCacheStatistics(userId)
            val queueStatus = getOfflineSyncQueueStatus(userId)
            
            emit(
                HealthCacheStatus(
                    isHealthy = statistics.expiredMetrics < statistics.totalCachedMetrics * 0.1, // Less than 10% expired
                    totalCachedItems = statistics.totalCachedMetrics,
                    pendingSyncItems = statistics.pendingSyncItems,
                    cacheUsageBytes = statistics.totalCacheSizeBytes,
                    maxCacheUsageBytes = MAX_CACHE_SIZE_MB * 1024 * 1024,
                    lastCleanup = statistics.lastUpdated,
                    queueStatus = queueStatus
                )
            )
            
            kotlinx.coroutines.delay(30000) // Update every 30 seconds
        }
    }
    
    // Private helper methods
    
    private fun createCacheEntry(
        userId: String,
        metric: HealthMetric,
        expiryHours: Long
    ): HealthDataCacheEntry {
        val now = LocalDateTime.now()
        return HealthDataCacheEntry(
            id = UUID.randomUUID().toString(),
            userId = userId,
            healthMetric = metric,
            cachedAt = now,
            expiresAt = now.plusHours(expiryHours),
            syncStatus = SyncState.PENDING_UPLOAD,
            checksum = calculateChecksum(metric)
        )
    }
    
    private fun calculateChecksum(metric: HealthMetric): String {
        val data = "${metric.id}${metric.userId}${metric.type}${metric.value}${metric.unit}${metric.timestamp}${metric.source}"
        return MessageDigest.getInstance("SHA-256")
            .digest(data.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    
    private fun verifyCacheEntryIntegrity(entry: HealthDataCacheEntry): Boolean {
        val calculatedChecksum = calculateChecksum(entry.healthMetric)
        return calculatedChecksum == entry.checksum
    }
    
    private fun isCacheEntryExpired(entry: HealthDataCacheEntry): Boolean {
        return LocalDateTime.now().isAfter(entry.expiresAt)
    }
    
    private fun generateCacheKey(userId: String, metricType: HealthMetricType, metricId: String): String {
        return "${HEALTH_DATA_CACHE_PREFIX}${userId}_${metricType}_${metricId}"
    }
    
    private fun generateCacheKeyPattern(userId: String, metricType: HealthMetricType): String {
        return "${HEALTH_DATA_CACHE_PREFIX}${userId}_${metricType}_*"
    }
    
    private fun generateSyncQueueKey(userId: String, itemId: String): String {
        return "${SYNC_QUEUE_CACHE_PREFIX}${userId}_${itemId}"
    }
    
    private fun generateSyncQueueKeyPattern(userId: String): String {
        return "${SYNC_QUEUE_CACHE_PREFIX}${userId}_*"
    }
    
    private suspend fun getAllCacheKeysForUser(userId: String): List<String> {
        return offlineCacheManager.getKeysMatching("${HEALTH_DATA_CACHE_PREFIX}${userId}_*")
    }
    
    private suspend fun updateCacheMetadata(userId: String, newEntriesCount: Int) {
        // Update cache metadata for monitoring
        val metadataKey = "cache_metadata_$userId"
        val metadata = mapOf(
            "lastUpdated" to LocalDateTime.now().toString(),
            "newEntriesCount" to newEntriesCount.toString()
        )
        offlineCacheManager.put(metadataKey, json.encodeToString(metadata))
    }
    
    private suspend fun getOfflineSyncQueueStatus(userId: String): OfflineSyncQueueStatus {
        val queueItems = getPendingSyncItems(userId)
        val failedItems = queueItems.filter { it.retryCount >= it.maxRetries }
        val oldestItem = queueItems.minByOrNull { it.createdAt }
        
        val oldestItemAge = oldestItem?.let { item ->
            java.time.Duration.between(item.createdAt, LocalDateTime.now()).toMillis()
        }
        
        val queueSizeBytes = queueItems.sumOf { item ->
            json.encodeToString(item).length.toLong()
        }
        
        return OfflineSyncQueueStatus(
            totalItems = queueItems.size,
            pendingItems = queueItems.size - failedItems.size,
            failedItems = failedItems.size,
            oldestItemAge = oldestItemAge,
            queueSizeBytes = queueSizeBytes,
            lastProcessedAt = LocalDateTime.now()
        )
    }
}

/**
 * Health cache statistics
 */
data class HealthCacheStatistics(
    val totalCachedMetrics: Int,
    val expiredMetrics: Int,
    val pendingSyncItems: Int,
    val totalCacheSizeBytes: Long,
    val oldestCacheEntry: LocalDateTime?,
    val lastUpdated: LocalDateTime
)

/**
 * Health cache status
 */
data class HealthCacheStatus(
    val isHealthy: Boolean,
    val totalCachedItems: Int,
    val pendingSyncItems: Int,
    val cacheUsageBytes: Long,
    val maxCacheUsageBytes: Long,
    val lastCleanup: LocalDateTime,
    val queueStatus: OfflineSyncQueueStatus
)