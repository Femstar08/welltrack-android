package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DataExportRepository
import com.beaconledger.welltrack.domain.repository.ImportPreview
import com.beaconledger.welltrack.domain.repository.ShareMethod
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

class DataExportUseCase @Inject constructor(
    private val dataExportRepository: DataExportRepository
) {
    
    // Export Operations
    suspend fun createExport(request: ExportRequest): Result<String> {
        return dataExportRepository.createExport(request)
    }
    
    suspend fun getExportHistory(userId: String): Flow<List<DataExport>> {
        return dataExportRepository.getExportHistory(userId)
    }
    
    suspend fun getExportById(exportId: String): DataExport? {
        return dataExportRepository.getExportById(exportId)
    }
    
    suspend fun cancelExport(exportId: String): Result<Unit> {
        return dataExportRepository.cancelExport(exportId)
    }
    
    suspend fun deleteExport(exportId: String): Result<Unit> {
        return dataExportRepository.deleteExport(exportId)
    }
    
    // Quick Export Methods
    suspend fun exportAllDataAsJson(userId: String): Result<String> {
        val request = ExportRequest(
            userId = userId,
            exportType = ExportType.FULL_BACKUP,
            format = ExportFormat.JSON,
            dateRange = null,
            includeHealthData = true,
            includeMealData = true,
            includeSupplementData = true,
            includeBiomarkerData = true,
            includeGoalData = true
        )
        
        return createExport(request)
    }
    
    suspend fun exportHealthReportAsPdf(
        userId: String,
        dateRange: DateRange? = null
    ): Result<String> {
        val actualDateRange = dateRange ?: getDefaultDateRange()
        
        val request = ExportRequest(
            userId = userId,
            exportType = ExportType.HEALTH_REPORT,
            format = ExportFormat.PDF,
            dateRange = actualDateRange,
            includeHealthData = true,
            includeMealData = true,
            includeSupplementData = true,
            includeBiomarkerData = true,
            includeGoalData = true
        )
        
        return createExport(request)
    }
    
    suspend fun exportMealHistoryAsCsv(
        userId: String,
        dateRange: DateRange? = null
    ): Result<String> {
        val request = ExportRequest(
            userId = userId,
            exportType = ExportType.MEAL_HISTORY,
            format = ExportFormat.CSV,
            dateRange = dateRange,
            includeHealthData = false,
            includeMealData = true,
            includeSupplementData = false,
            includeBiomarkerData = false,
            includeGoalData = false
        )
        
        return createExport(request)
    }
    
    suspend fun exportSupplementLogAsCsv(
        userId: String,
        dateRange: DateRange? = null
    ): Result<String> {
        val request = ExportRequest(
            userId = userId,
            exportType = ExportType.SUPPLEMENT_LOG,
            format = ExportFormat.CSV,
            dateRange = dateRange,
            includeHealthData = false,
            includeMealData = false,
            includeSupplementData = true,
            includeBiomarkerData = false,
            includeGoalData = false
        )
        
        return createExport(request)
    }
    
    // Import Operations
    suspend fun importData(request: ImportRequest): Result<Unit> {
        return dataExportRepository.importData(request)
    }
    
    suspend fun validateImportFile(filePath: String, dataType: ImportDataType): Result<Boolean> {
        return dataExportRepository.validateImportFile(filePath, dataType)
    }
    
    suspend fun previewImportData(filePath: String, dataType: ImportDataType): Result<ImportPreview> {
        return dataExportRepository.previewImportData(filePath, dataType)
    }
    
    suspend fun importFromOtherHealthApp(
        userId: String,
        sourceApp: String,
        filePath: String,
        mergeStrategy: MergeStrategy = MergeStrategy.MERGE_NEW_ONLY
    ): Result<Unit> {
        val dataType = when (sourceApp.lowercase()) {
            "myfitnesspal", "cronometer", "loseit" -> ImportDataType.MEAL_DATA
            "fitbit", "garmin", "samsung health" -> ImportDataType.HEALTH_DATA
            else -> ImportDataType.HEALTH_DATA
        }
        
        val request = ImportRequest(
            userId = userId,
            sourceApp = sourceApp,
            filePath = filePath,
            dataType = dataType,
            mergeStrategy = mergeStrategy
        )
        
        return importData(request)
    }
    
    // Data Portability and Compliance
    suspend fun generateGdprExport(userId: String): Result<File> {
        return dataExportRepository.generateGdprExport(userId)
    }
    
    suspend fun generateCcpaExport(userId: String): Result<File> {
        return dataExportRepository.generateCcpaExport(userId)
    }
    
    suspend fun scheduleDataDeletion(userId: String, deletionDate: LocalDateTime): Result<Unit> {
        return dataExportRepository.scheduleDataDeletion(userId, deletionDate)
    }
    
    suspend fun requestDataDeletion(userId: String, delayDays: Int = 30): Result<Unit> {
        val deletionDate = LocalDateTime.now().plusDays(delayDays.toLong())
        return scheduleDataDeletion(userId, deletionDate)
    }
    
    // Backup and Restore
    suspend fun createFullBackup(userId: String, includeMedia: Boolean = true): Result<File> {
        return dataExportRepository.createBackup(userId, includeMedia)
    }
    
    suspend fun restoreFromBackup(userId: String, backupFile: File): Result<Unit> {
        // Validate backup first
        val validationResult = dataExportRepository.validateBackup(backupFile)
        
        return validationResult.fold(
            onSuccess = { validation ->
                if (validation.isValid) {
                    dataExportRepository.restoreFromBackup(userId, backupFile)
                } else {
                    Result.failure(Exception("Invalid backup file: ${validation.errors.joinToString(", ")}"))
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    // Sharing and Healthcare Provider Integration
    suspend fun shareHealthReportWithProvider(
        userId: String,
        dateRange: DateRange,
        shareMethod: ShareMethod = ShareMethod.SECURE_LINK
    ): Result<String> {
        // Generate health report
        val reportResult = dataExportRepository.generatePdfHealthReport(userId, dateRange)
        
        return reportResult.fold(
            onSuccess = { reportFile ->
                when (shareMethod) {
                    ShareMethod.SECURE_LINK -> {
                        // Create export record and generate shareable link
                        val request = ExportRequest(
                            userId = userId,
                            exportType = ExportType.HEALTH_REPORT,
                            format = ExportFormat.PDF,
                            dateRange = dateRange
                        )
                        
                        createExport(request).fold(
                            onSuccess = { exportId ->
                                dataExportRepository.generateShareableLink(exportId, 72) // 72 hours expiration
                            },
                            onFailure = { Result.failure(it) }
                        )
                    }
                    else -> {
                        dataExportRepository.shareHealthReport(reportFile, shareMethod)
                            .map { "Report shared successfully" }
                    }
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
    
    suspend fun generateHealthcareProviderReport(
        userId: String,
        dateRange: DateRange,
        includeRecommendations: Boolean = false
    ): Result<File> {
        return dataExportRepository.generatePdfHealthReport(userId, dateRange)
    }
    
    // Utility Methods
    fun createCustomExportRequest(
        userId: String,
        exportType: ExportType,
        format: ExportFormat,
        dateRange: DateRange? = null,
        includeHealthData: Boolean = true,
        includeMealData: Boolean = true,
        includeSupplementData: Boolean = true,
        includeBiomarkerData: Boolean = true,
        includeGoalData: Boolean = true
    ): ExportRequest {
        return ExportRequest(
            userId = userId,
            exportType = exportType,
            format = format,
            dateRange = dateRange,
            includeHealthData = includeHealthData,
            includeMealData = includeMealData,
            includeSupplementData = includeSupplementData,
            includeBiomarkerData = includeBiomarkerData,
            includeGoalData = includeGoalData
        )
    }
    
    private fun getDefaultDateRange(): DateRange {
        val endDate = LocalDateTime.now()
        val startDate = endDate.minusMonths(3)
        return DateRange(startDate, endDate)
    }
    
    // Validation Methods
    fun validateExportRequest(request: ExportRequest): Result<Unit> {
        if (request.userId.isBlank()) {
            return Result.failure(Exception("User ID cannot be blank"))
        }
        
        if (!request.includeHealthData && !request.includeMealData && 
            !request.includeSupplementData && !request.includeBiomarkerData && 
            !request.includeGoalData) {
            return Result.failure(Exception("At least one data type must be included"))
        }
        
        request.dateRange?.let { range ->
            if (range.startDate.isAfter(range.endDate)) {
                return Result.failure(Exception("Start date cannot be after end date"))
            }
            
            if (range.startDate.isAfter(LocalDateTime.now())) {
                return Result.failure(Exception("Start date cannot be in the future"))
            }
        }
        
        return Result.success(Unit)
    }
    
    fun validateImportRequest(request: ImportRequest): Result<Unit> {
        if (request.userId.isBlank()) {
            return Result.failure(Exception("User ID cannot be blank"))
        }
        
        if (request.filePath.isBlank()) {
            return Result.failure(Exception("File path cannot be blank"))
        }
        
        val file = File(request.filePath)
        if (!file.exists()) {
            return Result.failure(Exception("Import file does not exist"))
        }
        
        if (file.length() == 0L) {
            return Result.failure(Exception("Import file is empty"))
        }
        
        return Result.success(Unit)
    }
}