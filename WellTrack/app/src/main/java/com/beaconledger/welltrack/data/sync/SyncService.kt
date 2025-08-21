package com.beaconledger.welltrack.data.sync

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.database.dao.SyncStatusDao
import com.beaconledger.welltrack.data.security.EncryptionManager
import com.beaconledger.welltrack.data.remote.SupabaseClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Core synchronization service that handles data sync between local and cloud storage
 */
@Singleton
class SyncService @Inject constructor(
    private val syncStatusDao: SyncStatusDao,
    private val encryptionManager: EncryptionManager,
    private val supabaseClient: SupabaseClient,
    private val entitySyncHandlers: Map<String, @JvmSuppressWildcards EntitySyncHandler<*>>,
    private val deviceIdProvider: DeviceIdProvider
) {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * Performs a full synchronization of all data
     */
    suspend fun performFullSync(): SyncResult {
        return try {
            val pendingItems = syncStatusDao.getEntitiesByStates(
                listOf(SyncState.PENDING_UPLOAD, SyncState.PENDING_DOWNLOAD, SyncState.FAILED)
            )
            
            val results = mutableListOf<SyncResult>()
            
            // Group by entity type for batch processing
            val groupedItems = pendingItems.groupBy { it.entityType }
            
            for ((entityType, items) in groupedItems) {
                val handler = entitySyncHandlers[entityType]
                if (handler != null) {
                    val result = syncEntityType(handler, items)
                    results.add(result)
                } else {
                    // Mark as failed if no handler available
                    items.forEach { item ->
                        syncStatusDao.updateSyncStatus(
                            item.copy(
                                syncState = SyncState.FAILED,
                                errorMessage = "No sync handler available for entity type: $entityType"
                            )
                        )
                    }
                }
            }
            
            // Combine results
            combineResults(results)
        } catch (e: Exception) {
            SyncResult.Error("Full sync failed: ${e.message}", e)
        }
    }
    
    /**
     * Syncs a specific entity type
     */
    private suspend fun <T : Any> syncEntityType(
        handler: EntitySyncHandler<T>,
        items: List<SyncStatus>
    ): SyncResult {
        val conflicts = mutableListOf<SyncConflict>()
        var successCount = 0
        var failureCount = 0
        val errors = mutableListOf<String>()
        
        for (item in items) {
            try {
                when (item.syncState) {
                    SyncState.PENDING_UPLOAD -> {
                        val result = uploadEntity(handler, item)
                        when (result) {
                            is SyncResult.Success -> successCount++
                            is SyncResult.Conflict -> conflicts.addAll(result.conflicts)
                            is SyncResult.Error -> {
                                failureCount++
                                errors.add(result.message)
                            }
                            else -> {}
                        }
                    }
                    SyncState.PENDING_DOWNLOAD -> {
                        val result = downloadEntity(handler, item)
                        when (result) {
                            is SyncResult.Success -> successCount++
                            is SyncResult.Error -> {
                                failureCount++
                                errors.add(result.message)
                            }
                            else -> {}
                        }
                    }
                    SyncState.FAILED -> {
                        if (item.retryCount < 3) {
                            // Retry failed items
                            val result = retrySync(handler, item)
                            when (result) {
                                is SyncResult.Success -> successCount++
                                is SyncResult.Error -> {
                                    failureCount++
                                    errors.add(result.message)
                                }
                                else -> {}
                            }
                        }
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                failureCount++
                errors.add("Failed to sync ${item.entityId}: ${e.message}")
                
                // Update retry count
                syncStatusDao.incrementRetryCount(item.entityId, e.message)
            }
        }
        
        return when {
            conflicts.isNotEmpty() -> SyncResult.Conflict(conflicts)
            failureCount == 0 -> SyncResult.Success
            successCount > 0 -> SyncResult.PartialSuccess(successCount, failureCount, errors)
            else -> SyncResult.Error("All sync operations failed", null)
        }
    }
    
    /**
     * Uploads a local entity to the cloud
     */
    private suspend fun <T : Any> uploadEntity(
        handler: EntitySyncHandler<T>,
        syncStatus: SyncStatus
    ): SyncResult {
        return try {
            val localEntity = handler.getLocalEntity(syncStatus.entityId)
            if (localEntity == null) {
                // Entity was deleted locally
                handler.deleteCloudEntity(syncStatus.entityId)
                syncStatusDao.deleteSyncStatusById(syncStatus.entityId)
                return SyncResult.Success
            }
            
            // Check for conflicts
            val cloudEntity = handler.getCloudEntity(syncStatus.entityId)
            if (cloudEntity != null) {
                val cloudVersion = handler.getEntityVersion(cloudEntity)
                if (cloudVersion > syncStatus.version) {
                    // Conflict detected
                    val conflict = SyncConflict(
                        entityId = syncStatus.entityId,
                        entityType = syncStatus.entityType,
                        localVersion = syncStatus.version,
                        cloudVersion = cloudVersion,
                        localData = json.encodeToString(localEntity),
                        cloudData = json.encodeToString(cloudEntity),
                        conflictTime = LocalDateTime.now()
                    )
                    
                    syncStatusDao.updateSyncStatus(
                        syncStatus.copy(syncState = SyncState.CONFLICT)
                    )
                    
                    return SyncResult.Conflict(listOf(conflict))
                }
            }
            
            // Encrypt sensitive data before upload
            val encryptedEntity = handler.encryptSensitiveData(localEntity)
            
            // Upload to cloud
            val uploadedEntity = handler.uploadToCloud(encryptedEntity)
            val newVersion = handler.getEntityVersion(uploadedEntity)
            
            // Update sync status
            syncStatusDao.updateSyncStatus(
                syncStatus.copy(
                    syncState = SyncState.SYNCED,
                    lastSyncTime = LocalDateTime.now(),
                    version = newVersion,
                    retryCount = 0,
                    errorMessage = null
                )
            )
            
            SyncResult.Success
        } catch (e: Exception) {
            syncStatusDao.incrementRetryCount(syncStatus.entityId, e.message)
            SyncResult.Error("Upload failed for ${syncStatus.entityId}: ${e.message}", e)
        }
    }
    
    /**
     * Downloads a cloud entity to local storage
     */
    private suspend fun <T : Any> downloadEntity(
        handler: EntitySyncHandler<T>,
        syncStatus: SyncStatus
    ): SyncResult {
        return try {
            val cloudEntity = handler.getCloudEntity(syncStatus.entityId)
            if (cloudEntity == null) {
                // Entity was deleted in cloud
                handler.deleteLocalEntity(syncStatus.entityId)
                syncStatusDao.deleteSyncStatusById(syncStatus.entityId)
                return SyncResult.Success
            }
            
            // Decrypt sensitive data
            val decryptedEntity = handler.decryptSensitiveData(cloudEntity)
            
            // Save to local storage
            handler.saveLocalEntity(decryptedEntity)
            
            // Update sync status
            val newVersion = handler.getEntityVersion(cloudEntity)
            syncStatusDao.updateSyncStatus(
                syncStatus.copy(
                    syncState = SyncState.SYNCED,
                    lastSyncTime = LocalDateTime.now(),
                    version = newVersion,
                    retryCount = 0,
                    errorMessage = null
                )
            )
            
            SyncResult.Success
        } catch (e: Exception) {
            syncStatusDao.incrementRetryCount(syncStatus.entityId, e.message)
            SyncResult.Error("Download failed for ${syncStatus.entityId}: ${e.message}", e)
        }
    }
    
    /**
     * Retries a failed sync operation
     */
    private suspend fun <T : Any> retrySync(
        handler: EntitySyncHandler<T>,
        syncStatus: SyncStatus
    ): SyncResult {
        // Add exponential backoff delay
        val delayMs = (1000 * Math.pow(2.0, syncStatus.retryCount.toDouble())).toLong()
        delay(delayMs)
        
        return when (syncStatus.syncState) {
            SyncState.FAILED -> {
                // Determine if this was an upload or download failure and retry
                val localEntity = handler.getLocalEntity(syncStatus.entityId)
                if (localEntity != null) {
                    uploadEntity(handler, syncStatus.copy(syncState = SyncState.PENDING_UPLOAD))
                } else {
                    downloadEntity(handler, syncStatus.copy(syncState = SyncState.PENDING_DOWNLOAD))
                }
            }
            else -> SyncResult.Error("Invalid state for retry: ${syncStatus.syncState}", null)
        }
    }
    
    /**
     * Marks an entity for upload sync
     */
    suspend fun markForUpload(entityId: String, entityType: String) {
        val existingStatus = syncStatusDao.getSyncStatus(entityId)
        val syncStatus = existingStatus?.copy(
            syncState = SyncState.PENDING_UPLOAD,
            lastModifiedTime = LocalDateTime.now(),
            version = existingStatus.version + 1
        ) ?: SyncStatus(
            entityId = entityId,
            entityType = entityType,
            syncState = SyncState.PENDING_UPLOAD,
            lastSyncTime = null,
            lastModifiedTime = LocalDateTime.now(),
            deviceId = deviceIdProvider.getDeviceId(),
            version = 1
        )
        
        syncStatusDao.insertSyncStatus(syncStatus)
    }
    
    /**
     * Marks an entity for download sync
     */
    suspend fun markForDownload(entityId: String, entityType: String, cloudVersion: Long) {
        val syncStatus = SyncStatus(
            entityId = entityId,
            entityType = entityType,
            syncState = SyncState.PENDING_DOWNLOAD,
            lastSyncTime = null,
            lastModifiedTime = LocalDateTime.now(),
            deviceId = deviceIdProvider.getDeviceId(),
            version = cloudVersion
        )
        
        syncStatusDao.insertSyncStatus(syncStatus)
    }
    
    /**
     * Observes pending sync items
     */
    fun observePendingSyncItems(): Flow<List<SyncStatus>> {
        return syncStatusDao.observePendingSyncItems()
    }
    
    /**
     * Gets sync statistics
     */
    suspend fun getSyncStats(): SyncStats {
        return SyncStats(
            pendingUpload = syncStatusDao.getCountByState(SyncState.PENDING_UPLOAD),
            pendingDownload = syncStatusDao.getCountByState(SyncState.PENDING_DOWNLOAD),
            conflicts = syncStatusDao.getCountByState(SyncState.CONFLICT),
            failed = syncStatusDao.getCountByState(SyncState.FAILED),
            synced = syncStatusDao.getCountByState(SyncState.SYNCED)
        )
    }
    
    private fun combineResults(results: List<SyncResult>): SyncResult {
        val allConflicts = mutableListOf<SyncConflict>()
        var totalSuccess = 0
        var totalFailure = 0
        val allErrors = mutableListOf<String>()
        
        for (result in results) {
            when (result) {
                is SyncResult.Success -> totalSuccess++
                is SyncResult.Conflict -> allConflicts.addAll(result.conflicts)
                is SyncResult.Error -> {
                    totalFailure++
                    allErrors.add(result.message)
                }
                is SyncResult.PartialSuccess -> {
                    totalSuccess += result.successCount
                    totalFailure += result.failureCount
                    allErrors.addAll(result.errors)
                }
            }
        }
        
        return when {
            allConflicts.isNotEmpty() -> SyncResult.Conflict(allConflicts)
            totalFailure == 0 -> SyncResult.Success
            totalSuccess > 0 -> SyncResult.PartialSuccess(totalSuccess, totalFailure, allErrors)
            else -> SyncResult.Error("All sync operations failed", null)
        }
    }
}

