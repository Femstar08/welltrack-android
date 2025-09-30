package com.beaconledger.welltrack.data.model

import java.time.LocalDateTime

/**
 * Result of health data synchronization
 */
sealed class HealthSyncResult {
    data class Success(
        val syncedMetricsCount: Int,
        val platformSyncStatuses: List<PlatformSyncStatus>,
        val cloudSyncResult: CloudSyncResult,
        val syncTimestamp: LocalDateTime,
        val summary: String
    ) : HealthSyncResult()
    
    data class Error(
        val message: String,
        val exception: Throwable?,
        val partialData: List<HealthMetric>
    ) : HealthSyncResult()
    
    data class PartialSuccess(
        val syncedMetricsCount: Int,
        val failedMetricsCount: Int,
        val platformSyncStatuses: List<PlatformSyncStatus>,
        val cloudSyncResult: CloudSyncResult,
        val errors: List<String>,
        val syncTimestamp: LocalDateTime
    ) : HealthSyncResult()
}

/**
 * Status of synchronization with external platforms
 */
data class PlatformSyncStatus(
    val platform: String,
    val isAvailable: Boolean,
    val isConnected: Boolean,
    val lastSyncTime: LocalDateTime?,
    val syncStatus: SyncState,
    val errorMessage: String?,
    val syncedMetricsCount: Int = 0,
    val supportedMetricTypes: List<HealthMetricType> = emptyList()
)

/**
 * Result of platform synchronization
 */
data class PlatformSyncResult(
    val metrics: List<HealthMetric>,
    val platformStatuses: List<PlatformSyncStatus>,
    val syncTimestamp: LocalDateTime
)

/**
 * Result of cloud synchronization
 */
sealed class CloudSyncResult {
    object Success : CloudSyncResult()
    data class Error(val message: String, val exception: Throwable?) : CloudSyncResult()
    data class Conflicts(val conflicts: List<SyncConflict>) : CloudSyncResult()
    data class PartialSuccess(
        val successCount: Int,
        val failureCount: Int,
        val errors: List<String>
    ) : CloudSyncResult()
}

/**
 * Current health data sync status
 */
data class HealthSyncStatus(
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflicts: Int,
    val failed: Int,
    val lastSyncTime: LocalDateTime?
)

/**
 * Configuration for health data synchronization
 */
data class HealthSyncConfig(
    val enabledPlatforms: Set<String> = setOf("Health Connect", "Samsung Health", "Garmin Connect"),
    val syncInterval: Long = 3600000L, // 1 hour in milliseconds
    val batchSize: Int = 100,
    val maxRetries: Int = 3,
    val conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.MANUAL,
    val enableOfflineCache: Boolean = true,
    val enableDataValidation: Boolean = true,
    val enableBidirectionalSync: Boolean = true
)

/**
 * Health data sync statistics
 */
data class SyncStats(
    val pendingUpload: Int,
    val pendingDownload: Int,
    val conflicts: Int,
    val failed: Int,
    val synced: Int
)

/**
 * Represents a queue item for offline health data sync
 */
data class HealthSyncQueueItem(
    val id: String,
    val userId: String,
    val operation: SyncOperation,
    val healthMetric: HealthMetric,
    val targetPlatform: String,
    val createdAt: LocalDateTime,
    val retryCount: Int = 0,
    val maxRetries: Int = 3,
    val priority: SyncPriority = SyncPriority.NORMAL
)

/**
 * Types of sync operations
 */
enum class SyncOperation {
    UPLOAD,
    DOWNLOAD,
    UPDATE,
    DELETE
}

/**
 * Priority levels for sync operations
 */
enum class SyncPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}

/**
 * Health data validation error
 */
data class HealthDataValidationError(
    val metricId: String,
    val errorType: ValidationErrorType,
    val message: String,
    val field: String?,
    val suggestedFix: String?
)

/**
 * Types of validation errors
 */
enum class ValidationErrorType {
    MISSING_REQUIRED_FIELD,
    INVALID_VALUE_RANGE,
    INVALID_TIMESTAMP,
    INVALID_UNIT,
    INVALID_DATA_TYPE,
    DUPLICATE_ENTRY,
    INCONSISTENT_DATA
}

/**
 * Health data sync event for monitoring
 */
data class HealthSyncEvent(
    val id: String,
    val userId: String,
    val eventType: SyncEventType,
    val platform: String?,
    val metricType: HealthMetricType?,
    val timestamp: LocalDateTime,
    val details: String?,
    val isSuccess: Boolean
)

/**
 * Types of sync events
 */
enum class SyncEventType {
    SYNC_STARTED,
    SYNC_COMPLETED,
    SYNC_FAILED,
    CONFLICT_DETECTED,
    CONFLICT_RESOLVED,
    DATA_VALIDATED,
    DATA_SANITIZED,
    PLATFORM_CONNECTED,
    PLATFORM_DISCONNECTED,
    PERMISSION_GRANTED,
    PERMISSION_DENIED
}

/**
 * Health data sync metrics for monitoring and analytics
 */
data class HealthSyncMetrics(
    val totalSyncOperations: Long,
    val successfulSyncs: Long,
    val failedSyncs: Long,
    val conflictsDetected: Long,
    val conflictsResolved: Long,
    val averageSyncDuration: Long, // milliseconds
    val dataValidationErrors: Long,
    val platformAvailability: Map<String, Double>, // percentage uptime
    val syncFrequency: Map<HealthMetricType, Long>, // syncs per day
    val lastUpdated: LocalDateTime
)

/**
 * Health data cache entry for offline support
 */
data class HealthDataCacheEntry(
    val id: String,
    val userId: String,
    val healthMetric: HealthMetric,
    val cachedAt: LocalDateTime,
    val expiresAt: LocalDateTime,
    val syncStatus: SyncState,
    val checksum: String // for data integrity verification
)

/**
 * Offline sync queue status
 */
data class OfflineSyncQueueStatus(
    val totalItems: Int,
    val pendingItems: Int,
    val failedItems: Int,
    val oldestItemAge: Long?, // milliseconds
    val queueSizeBytes: Long,
    val lastProcessedAt: LocalDateTime?
)