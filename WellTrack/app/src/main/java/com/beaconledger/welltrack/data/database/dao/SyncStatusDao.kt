package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.SyncStatus
import com.beaconledger.welltrack.data.model.SyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncStatusDao {
    
    @Query("SELECT * FROM sync_status WHERE entityId = :entityId")
    suspend fun getSyncStatus(entityId: String): SyncStatus?
    
    @Query("SELECT * FROM sync_status WHERE syncState = :state")
    suspend fun getEntitiesByState(state: SyncState): List<SyncStatus>
    
    @Query("SELECT * FROM sync_status WHERE syncState IN (:states)")
    suspend fun getEntitiesByStates(states: List<SyncState>): List<SyncStatus>
    
    @Query("SELECT * FROM sync_status WHERE entityType = :entityType")
    suspend fun getEntitiesByType(entityType: String): List<SyncStatus>
    
    @Query("SELECT * FROM sync_status WHERE syncState = :state AND entityType = :entityType")
    suspend fun getEntitiesByStateAndType(state: SyncState, entityType: String): List<SyncStatus>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncStatus(syncStatus: SyncStatus)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncStatuses(syncStatuses: List<SyncStatus>)
    
    @Update
    suspend fun updateSyncStatus(syncStatus: SyncStatus)
    
    @Delete
    suspend fun deleteSyncStatus(syncStatus: SyncStatus)
    
    @Query("DELETE FROM sync_status WHERE entityId = :entityId")
    suspend fun deleteSyncStatusById(entityId: String)
    
    @Query("UPDATE sync_status SET syncState = :newState WHERE entityId = :entityId")
    suspend fun updateSyncState(entityId: String, newState: SyncState)
    
    @Query("UPDATE sync_status SET retryCount = retryCount + 1, errorMessage = :errorMessage WHERE entityId = :entityId")
    suspend fun incrementRetryCount(entityId: String, errorMessage: String?)
    
    @Query("SELECT COUNT(*) FROM sync_status WHERE syncState = :state")
    suspend fun getCountByState(state: SyncState): Int
    
    @Query("SELECT * FROM sync_status WHERE syncState != :syncedState")
    fun observePendingSyncItems(syncedState: SyncState = SyncState.SYNCED): Flow<List<SyncStatus>>
    
    @Query("DELETE FROM sync_status WHERE entityType = :entityType")
    suspend fun clearSyncStatusForEntityType(entityType: String)
    
    @Query("DELETE FROM sync_status")
    suspend fun clearAllSyncStatus()
}