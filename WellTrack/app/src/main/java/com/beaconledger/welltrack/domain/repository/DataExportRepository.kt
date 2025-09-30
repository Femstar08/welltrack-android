package com.beaconledger.welltrack.domain.repository

import com.beaconledger.welltrack.data.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface DataExportRepository {
    
    // Export Operations
    suspend fun createExport(request: ExportRequest): Result<String>
    suspend fun getExportHistory(userId: String): Flow<List<DataExport>>
    suspend fun getExportById(exportId: String): DataExport?
    suspend fun cancelExport(exportId: String): Result<Unit>
    suspend fun deleteExport(exportId: String): Result<Unit>
    
    // Export Generation
    suspend fun exportToJson(request: ExportRequest): Result<File>
    suspend fun exportToCsv(request: ExportRequest): Result<List<File>>
    suspend fun exportToPdf(request: ExportRequest): Result<File>
    suspend fun createFullBackup(userId: String): Result<File>
    
    // Health Reports
    suspend fun generateHealthReport(userId: String, dateRange: DateRange): Result<HealthReport>
    suspend fun generatePdfHealthReport(userId: String, dateRange: DateRange): Result<File>
    
    // Import Operations
    suspend fun importData(request: ImportRequest): Result<Unit>
    suspend fun validateImportFile(filePath: String, dataType: ImportDataType): Result<Boolean>
    suspend fun previewImportData(filePath: String, dataType: ImportDataType): Result<ImportPreview>
    
    // Data Portability Compliance
    suspend fun generateGdprExport(userId: String): Result<File>
    suspend fun generateCcpaExport(userId: String): Result<File>
    suspend fun scheduleDataDeletion(userId: String, deletionDate: java.time.LocalDateTime): Result<Unit>
    
    // Backup and Restore
    suspend fun createBackup(userId: String, includeMedia: Boolean): Result<File>
    suspend fun restoreFromBackup(userId: String, backupFile: File): Result<Unit>
    suspend fun validateBackup(backupFile: File): Result<BackupValidation>
    
    // Sharing
    suspend fun shareHealthReport(reportFile: File, shareMethod: ShareMethod): Result<Unit>
    suspend fun generateShareableLink(exportId: String, expirationHours: Int): Result<String>
}

data class ImportPreview(
    val recordCount: Int,
    val dataTypes: List<String>,
    val dateRange: DateRange?,
    val conflicts: List<ImportConflict>,
    val warnings: List<String>
)

data class ImportConflict(
    val recordId: String,
    val conflictType: String,
    val existingValue: String,
    val newValue: String,
    val recommendedAction: String
)

data class BackupValidation(
    val isValid: Boolean,
    val version: String,
    val userId: String,
    val createdAt: java.time.LocalDateTime,
    val dataIntegrity: Boolean,
    val missingTables: List<String>,
    val errors: List<String>
)

enum class ShareMethod {
    EMAIL,
    CLOUD_STORAGE,
    SECURE_LINK,
    HEALTHCARE_PROVIDER_PORTAL
}