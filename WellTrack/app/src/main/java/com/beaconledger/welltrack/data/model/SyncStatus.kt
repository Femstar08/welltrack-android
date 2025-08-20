package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Represents the synchronization status of data entities
 */
enum class SyncState {
    SYNCED,          // Data is synchronized with cloud
    PENDING_UPLOAD,  // Local changes need to be uploaded
    PENDING_DOWNLOAD,// Cloud changes need to be downloaded
    CONFLICT,        // Conflict detected between local and cloud
    FAILED           // Sync operation failed
}

/**
 * Tracks synchronization metadata for each entity
 */
@Entity(tableName = "sync_status")
data class SyncStatus(
    @PrimaryKey
    val entityId: String,
    val entityType: String,
    val syncState: SyncState,
    val lastSyncTime: LocalDateTime?,
    val lastModifiedTime: LocalDateTime,
    val deviceId: String,
    val version: Long = 1,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * Represents a conflict between local and cloud data
 */
data class SyncConflict(
    val entityId: String,
    val entityType: String,
    val localVersion: Long,
    val cloudVersion: Long,
    val localData: String, // JSON representation
    val cloudData: String, // JSON representation
    val conflictTime: LocalDateTime
)

/**
 * Represents the result of a sync operation
 */
sealed class SyncResult {
    object Success : SyncResult()
    data class Conflict(val conflicts: List<SyncConflict>) : SyncResult()
    data class Error(val message: String, val exception: Throwable? = null) : SyncResult()
    data class PartialSuccess(val successCount: Int, val failureCount: Int, val errors: List<String>) : SyncResult()
}

/**
 * Configuration for sync operations
 */
data class SyncConfig(
    val batchSize: Int = 50,
    val maxRetries: Int = 3,
    val retryDelayMs: Long = 1000,
    val conflictResolutionStrategy: ConflictResolutionStrategy = ConflictResolutionStrategy.MANUAL
)

/**
 * Strategy for resolving sync conflicts
 */
enum class ConflictResolutionStrategy {
    LOCAL_WINS,     // Always use local data
    CLOUD_WINS,     // Always use cloud data
    LATEST_WINS,    // Use data with latest timestamp
    MANUAL          // Require user intervention
}