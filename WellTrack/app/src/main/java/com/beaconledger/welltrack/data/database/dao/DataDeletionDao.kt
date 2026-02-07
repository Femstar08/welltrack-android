package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.compliance.DataDeletionRecord
import com.beaconledger.welltrack.data.compliance.DeletionStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface DataDeletionDao {
    
    @Query("SELECT * FROM data_deletion_records WHERE userId = :userId ORDER BY scheduledDate DESC")
    fun getDeletionRecordsForUser(userId: String): Flow<List<DataDeletionRecord>>
    
    @Query("SELECT * FROM data_deletion_records WHERE id = :recordId")
    suspend fun getDeletionRecordById(recordId: String): DataDeletionRecord?
    
    @Query("SELECT * FROM data_deletion_records WHERE status = :status AND scheduledDate <= :currentDate")
    suspend fun getPendingDeletions(status: DeletionStatus, currentDate: LocalDateTime): List<DataDeletionRecord>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeletionRecord(record: DataDeletionRecord)
    
    @Update
    suspend fun updateDeletionRecord(record: DataDeletionRecord)
    
    @Query("UPDATE data_deletion_records SET status = :status, completedAt = :completedAt WHERE id = :recordId")
    suspend fun updateDeletionStatus(recordId: String, status: DeletionStatus, completedAt: LocalDateTime?)
    
    @Query("DELETE FROM data_deletion_records WHERE id = :recordId")
    suspend fun deleteDeletionRecord(recordId: String)
    
    @Query("DELETE FROM data_deletion_records WHERE userId = :userId")
    suspend fun deleteAllDeletionRecordsForUser(userId: String)
}