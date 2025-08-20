package com.beaconledger.welltrack.data.cache

import android.content.Context
import com.beaconledger.welltrack.data.database.WellTrackDatabase
import com.beaconledger.welltrack.data.sync.SyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages offline data caching and synchronization when connectivity is restored
 */
@Singleton
class OfflineCacheManager @Inject constructor(
    private val database: WellTrackDatabase,
    private val syncService: SyncService,
    private val connectivityMonitor: ConnectivityMonitor,
    private val context: Context
) {
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    private val _cacheStatus = MutableStateFlow(CacheStatus.IDLE)
    val cacheStatus: StateFlow<CacheStatus> = _cacheStatus
    
    private val _pendingOperations = MutableStateFlow(0)
    val pendingOperations: StateFlow<Int> = _pendingOperations
    
    init {
        // Monitor connectivity changes
        scope.launch {
            connectivityMonitor.isConnected.collect { isConnected ->
                if (isConnected) {
                    performPendingSync()
                }
            }
        }
        
        // Monitor pending sync items
        scope.launch {
            syncService.observePendingSyncItems().collect { pendingItems ->
                _pendingOperations.value = pendingItems.size
            }
        }
    }
    
    /**
     * Caches data locally when offline
     */
    suspend fun cacheData(entityId: String, entityType: String, operation: CacheOperation) {
        _cacheStatus.value = CacheStatus.CACHING
        
        try {
            when (operation) {
                CacheOperation.CREATE, CacheOperation.UPDATE -> {
                    syncService.markForUpload(entityId, entityType)
                }
                CacheOperation.DELETE -> {
                    // Mark for deletion sync
                    syncService.markForUpload(entityId, entityType)
                }
            }
            
            _cacheStatus.value = CacheStatus.CACHED
        } catch (e: Exception) {
            _cacheStatus.value = CacheStatus.ERROR
            throw e
        }
    }
    
    /**
     * Performs pending synchronization when connectivity is restored
     */
    private suspend fun performPendingSync() {
        if (_cacheStatus.value == CacheStatus.SYNCING) return
        
        _cacheStatus.value = CacheStatus.SYNCING
        
        try {
            val result = syncService.performFullSync()
            
            when (result) {
                is com.beaconledger.welltrack.data.model.SyncResult.Success -> {
                    _cacheStatus.value = CacheStatus.SYNCED
                }
                is com.beaconledger.welltrack.data.model.SyncResult.Conflict -> {
                    _cacheStatus.value = CacheStatus.CONFLICT
                }
                is com.beaconledger.welltrack.data.model.SyncResult.Error -> {
                    _cacheStatus.value = CacheStatus.ERROR
                }
                is com.beaconledger.welltrack.data.model.SyncResult.PartialSuccess -> {
                    _cacheStatus.value = if (result.failureCount > 0) CacheStatus.PARTIAL_SYNC else CacheStatus.SYNCED
                }
            }
        } catch (e: Exception) {
            _cacheStatus.value = CacheStatus.ERROR
        }
    }
    
    /**
     * Forces a manual sync attempt
     */
    suspend fun forcSync(): com.beaconledger.welltrack.data.model.SyncResult {
        return if (connectivityMonitor.isConnected.value) {
            syncService.performFullSync()
        } else {
            com.beaconledger.welltrack.data.model.SyncResult.Error("No internet connection", null)
        }
    }
    
    /**
     * Gets cache statistics
     */
    suspend fun getCacheStats(): CacheStats {
        val syncStats = syncService.getSyncStats()
        return CacheStats(
            totalCachedItems = syncStats.pendingUpload + syncStats.pendingDownload,
            pendingUploads = syncStats.pendingUpload,
            pendingDownloads = syncStats.pendingDownload,
            conflicts = syncStats.conflicts,
            lastSyncAttempt = LocalDateTime.now(), // This should be stored and retrieved
            isConnected = connectivityMonitor.isConnected.value
        )
    }
    
    /**
     * Clears all cached data (use with caution)
     */
    suspend fun clearCache() {
        database.clearAllTables()
        _cacheStatus.value = CacheStatus.IDLE
        _pendingOperations.value = 0
    }
    
    /**
     * Preloads essential data for offline use
     */
    suspend fun preloadEssentialData(userId: String) {
        _cacheStatus.value = CacheStatus.PRELOADING
        
        try {
            // Preload user's recent meals, recipes, and health data
            // This would involve fetching from cloud and storing locally
            
            _cacheStatus.value = CacheStatus.PRELOADED
        } catch (e: Exception) {
            _cacheStatus.value = CacheStatus.ERROR
            throw e
        }
    }
}

/**
 * Cache operation types
 */
enum class CacheOperation {
    CREATE, UPDATE, DELETE
}

/**
 * Cache status
 */
enum class CacheStatus {
    IDLE,
    CACHING,
    CACHED,
    SYNCING,
    SYNCED,
    PARTIAL_SYNC,
    CONFLICT,
    ERROR,
    PRELOADING,
    PRELOADED
}

/**
 * Cache statistics
 */
data class CacheStats(
    val totalCachedItems: Int,
    val pendingUploads: Int,
    val pendingDownloads: Int,
    val conflicts: Int,
    val lastSyncAttempt: LocalDateTime?,
    val isConnected: Boolean
)