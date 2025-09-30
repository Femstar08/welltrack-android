package com.beaconledger.welltrack.data.database.dao

import androidx.room.*
import com.beaconledger.welltrack.data.model.DataExport
import com.beaconledger.welltrack.data.model.ExportStatus
import com.beaconledger.welltrack.data.model.ExportType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface DataExportDao {
    
    @Query("SELECT * FROM data_exports WHERE userId = :userId ORDER BY createdAt DESC")
    fun getExportHistory(userId: String): Flow<List<DataExport>>
    
    @Query("SELECT * FROM data_exports WHERE id = :exportId")
    suspend fun getExportById(exportId: String): DataExport?
    
    @Query("SELECT * FROM data_exports WHERE userId = :userId AND status = :status")
    suspend fun getExportsByStatus(userId: String, status: ExportStatus): List<DataExport>
    
    @Query("SELECT * FROM data_exports WHERE userId = :userId AND exportType = :type ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestExportByType(userId: String, type: ExportType): DataExport?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExport(export: DataExport): Long
    
    @Update
    suspend fun updateExport(export: DataExport)
    
    @Query("UPDATE data_exports SET status = :status, completedAt = :completedAt, filePath = :filePath, fileSize = :fileSize WHERE id = :exportId")
    suspend fun updateExportStatus(
        exportId: String,
        status: ExportStatus,
        completedAt: LocalDateTime?,
        filePath: String?,
        fileSize: Long?
    )
    
    @Query("UPDATE data_exports SET status = :status, errorMessage = :errorMessage WHERE id = :exportId")
    suspend fun updateExportError(exportId: String, status: ExportStatus, errorMessage: String)
    
    @Query("DELETE FROM data_exports WHERE id = :exportId")
    suspend fun deleteExport(exportId: String)
    
    @Query("DELETE FROM data_exports WHERE userId = :userId AND createdAt < :cutoffDate")
    suspend fun deleteOldExports(userId: String, cutoffDate: LocalDateTime)
    
    @Query("SELECT COUNT(*) FROM data_exports WHERE userId = :userId AND status = 'IN_PROGRESS'")
    suspend fun getActiveExportsCount(userId: String): Int
    
    @Query("SELECT SUM(fileSize) FROM data_exports WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun getTotalExportSize(userId: String): Long?
}