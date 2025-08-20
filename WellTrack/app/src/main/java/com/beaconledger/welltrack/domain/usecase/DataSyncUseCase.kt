package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.domain.repository.DataSyncRepository
import com.beaconledger.welltrack.domain.repository.ConflictResolution
import com.beaconledger.welltrack.data.model.SyncResult
import com.beaconledger.welltrack.data.model.SyncStats
import com.beaconledger.welltrack.data.backup.BackupResult
import com.beaconledger.welltrack.data.backup.ExportResult
import com.beaconledger.welltrack.data.backup.ExportFormat
import com.beaconledger.welltrack.data.cache.CacheStats
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for data synchronization operations
 */
class DataSyncUseCase @Inject constructor(
    private val dataSyncRepository: DataSyncRepository
) {
    
    /**
     * Performs a full synchronization of all user data
     */
    suspend fun performFullSync(): SyncResult {
        return dataSyncRepository.performFullSync()
    }
    
    /**
     * Syncs a specific type of data (e.g., meals, recipes, health metrics)
     */
    suspend fun syncSpecificData(entityType: String): SyncResult {
        return dataSyncRepository.syncEntityType(entityType)
    }
    
    /**
     * Gets current synchronization statistics
     */
    suspend fun getSyncStats(): SyncStats {
        return dataSyncRepository.getSyncStats()
    }
    
    /**
     * Observes sync status changes in real-time
     */
    fun observeSyncStatus(): Flow<SyncStats> {
        return dataSyncRepository.observeSyncStatus()
    }
    
    /**
     * Gets offline cache statistics
     */
    suspend fun getCacheStats(): CacheStats {
        return dataSyncRepository.getCacheStats()
    }
    
    /**
     * Clears all cached data (use with caution)
     */
    suspend fun clearCache() {
        dataSyncRepository.clearCache()
    }
    
    /**
     * Preloads essential data for offline use
     */
    suspend fun preloadEssentialData(userId: String) {
        dataSyncRepository.preloadEssentialData(userId)
    }
    
    /**
     * Creates a backup of user data
     */
    suspend fun createBackup(userId: String, includeEncryption: Boolean = true): BackupResult {
        return dataSyncRepository.createBackup(userId, includeEncryption)
    }
    
    /**
     * Restores data from a backup file
     */
    suspend fun restoreBackup(backupUri: Uri, userId: String): BackupResult {
        return dataSyncRepository.restoreBackup(backupUri, userId)
    }
    
    /**
     * Exports user data in the specified format
     */
    suspend fun exportData(userId: String, format: ExportFormat): ExportResult {
        return dataSyncRepository.exportData(userId, format)
    }
    
    /**
     * Gets all current sync conflicts
     */
    suspend fun getConflicts(): List<com.beaconledger.welltrack.data.model.SyncConflict> {
        return dataSyncRepository.getConflicts()
    }
    
    /**
     * Resolves a sync conflict with the specified resolution strategy
     */
    suspend fun resolveConflict(conflictId: String, resolution: ConflictResolution): SyncResult {
        return dataSyncRepository.resolveConflict(conflictId, resolution)
    }
    
    /**
     * Validates if sync is needed based on data changes
     */
    suspend fun isSyncNeeded(): Boolean {
        val stats = getSyncStats()
        return stats.pendingUpload > 0 || stats.pendingDownload > 0 || stats.failed > 0
    }
    
    /**
     * Gets a summary of sync status for UI display
     */
    suspend fun getSyncSummary(): SyncSummary {
        val stats = getSyncStats()
        val cacheStats = getCacheStats()
        
        return SyncSummary(
            isOnline = cacheStats.isConnected,
            hasPendingChanges = stats.pendingUpload > 0,
            hasConflicts = stats.conflicts > 0,
            lastSyncTime = cacheStats.lastSyncAttempt,
            totalPendingItems = stats.pendingUpload + stats.pendingDownload,
            syncStatus = when {
                stats.conflicts > 0 -> SyncStatusType.CONFLICTS
                stats.failed > 0 -> SyncStatusType.FAILED
                stats.pendingUpload > 0 || stats.pendingDownload > 0 -> SyncStatusType.PENDING
                else -> SyncStatusType.SYNCED
            }
        )
    }
}

/**
 * Summary of sync status for UI display
 */
data class SyncSummary(
    val isOnline: Boolean,
    val hasPendingChanges: Boolean,
    val hasConflicts: Boolean,
    val lastSyncTime: java.time.LocalDateTime?,
    val totalPendingItems: Int,
    val syncStatus: SyncStatusType
)

/**
 * Sync status types for UI
 */
enum class SyncStatusType {
    SYNCED,
    PENDING,
    CONFLICTS,
    FAILED
}