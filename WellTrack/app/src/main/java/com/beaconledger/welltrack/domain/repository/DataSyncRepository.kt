package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.SyncResult
import com.beaconledger.welltrack.data.model.SyncStats
import com.beaconledger.welltrack.data.backup.BackupResult
import com.beaconledger.welltrack.data.backup.ExportResult
import com.beaconledger.welltrack.data.backup.ExportFormat
import com.beaconledger.welltrack.data.cache.CacheStats
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for data synchronization operations
 */
interface DataSyncRepository {
    
    // Synchronization
    suspend fun performFullSync(): SyncResult
    suspend fun syncEntityType(entityType: String): SyncResult
    suspend fun getSyncStats(): SyncStats
    fun observeSyncStatus(): Flow<SyncStats>
    
    // Offline caching
    suspend fun getCacheStats(): CacheStats
    suspend fun clearCache()
    suspend fun preloadEssentialData(userId: String)
    
    // Backup operations
    suspend fun createBackup(userId: String, includeEncryption: Boolean = true): BackupResult
    suspend fun restoreBackup(backupUri: Uri, userId: String): BackupResult
    
    // Data export
    suspend fun exportData(userId: String, format: ExportFormat): ExportResult
    
    // Conflict resolution
    suspend fun getConflicts(): List<com.beaconledger.welltrack.data.model.SyncConflict>
    suspend fun resolveConflict(
        conflictId: String, 
        resolution: ConflictResolution
    ): SyncResult
}

/**
 * Conflict resolution options
 */
enum class ConflictResolution {
    USE_LOCAL,
    USE_CLOUD,
    MERGE
}