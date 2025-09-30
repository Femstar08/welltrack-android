package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.DataExportRepository
import com.beaconledger.welltrack.domain.repository.ImportPreview
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDateTime

class DataExportUseCaseTest {
    
    private lateinit var dataExportRepository: DataExportRepository
    private lateinit var dataExportUseCase: DataExportUseCase
    
    private val testUserId = "test-user-123"
    private val testExportId = "export-123"
    
    @Before
    fun setup() {
        dataExportRepository = mockk()
        dataExportUseCase = DataExportUseCase(dataExportRepository)
    }
    
    @Test
    fun `createExport should return success when repository succeeds`() = runTest {
        // Given
        val request = createTestExportRequest()
        coEvery { dataExportRepository.createExport(request) } returns Result.success(testExportId)
        
        // When
        val result = dataExportUseCase.createExport(request)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(testExportId, result.getOrNull())
        coVerify { dataExportRepository.createExport(request) }
    }
    
    @Test
    fun `createExport should return failure when repository fails`() = runTest {
        // Given
        val request = createTestExportRequest()
        val error = Exception("Export failed")
        coEvery { dataExportRepository.createExport(request) } returns Result.failure(error)
        
        // When
        val result = dataExportUseCase.createExport(request)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())
    }
    
    @Test
    fun `getExportHistory should return flow from repository`() = runTest {
        // Given
        val exports = listOf(createTestDataExport())
        coEvery { dataExportRepository.getExportHistory(testUserId) } returns flowOf(exports)
        
        // When
        val result = dataExportUseCase.getExportHistory(testUserId)
        
        // Then
        result.collect { exportList ->
            assertEquals(exports, exportList)
        }
        coVerify { dataExportRepository.getExportHistory(testUserId) }
    }
    
    @Test
    fun `exportAllDataAsJson should create correct export request`() = runTest {
        // Given
        coEvery { dataExportRepository.createExport(any()) } returns Result.success(testExportId)
        
        // When
        val result = dataExportUseCase.exportAllDataAsJson(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            dataExportRepository.createExport(
                match { request ->
                    request.userId == testUserId &&
                    request.exportType == ExportType.FULL_BACKUP &&
                    request.format == ExportFormat.JSON &&
                    request.includeHealthData &&
                    request.includeMealData &&
                    request.includeSupplementData &&
                    request.includeBiomarkerData &&
                    request.includeGoalData
                }
            )
        }
    }
    
    @Test
    fun `exportHealthReportAsPdf should create correct export request with date range`() = runTest {
        // Given
        val dateRange = DateRange(
            LocalDateTime.now().minusMonths(1),
            LocalDateTime.now()
        )
        coEvery { dataExportRepository.createExport(any()) } returns Result.success(testExportId)
        
        // When
        val result = dataExportUseCase.exportHealthReportAsPdf(testUserId, dateRange)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            dataExportRepository.createExport(
                match { request ->
                    request.userId == testUserId &&
                    request.exportType == ExportType.HEALTH_REPORT &&
                    request.format == ExportFormat.PDF &&
                    request.dateRange == dateRange
                }
            )
        }
    }
    
    @Test
    fun `exportMealHistoryAsCsv should create correct export request`() = runTest {
        // Given
        coEvery { dataExportRepository.createExport(any()) } returns Result.success(testExportId)
        
        // When
        val result = dataExportUseCase.exportMealHistoryAsCsv(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            dataExportRepository.createExport(
                match { request ->
                    request.userId == testUserId &&
                    request.exportType == ExportType.MEAL_HISTORY &&
                    request.format == ExportFormat.CSV &&
                    !request.includeHealthData &&
                    request.includeMealData &&
                    !request.includeSupplementData &&
                    !request.includeBiomarkerData &&
                    !request.includeGoalData
                }
            )
        }
    }
    
    @Test
    fun `importData should call repository with correct request`() = runTest {
        // Given
        val importRequest = ImportRequest(
            userId = testUserId,
            sourceApp = "TestApp",
            filePath = "/test/path",
            dataType = ImportDataType.HEALTH_DATA,
            mergeStrategy = MergeStrategy.MERGE_NEW_ONLY
        )
        coEvery { dataExportRepository.importData(importRequest) } returns Result.success(Unit)
        
        // When
        val result = dataExportUseCase.importData(importRequest)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { dataExportRepository.importData(importRequest) }
    }
    
    @Test
    fun `validateImportFile should call repository with correct parameters`() = runTest {
        // Given
        val filePath = "/test/file.json"
        val dataType = ImportDataType.MEAL_DATA
        coEvery { dataExportRepository.validateImportFile(filePath, dataType) } returns Result.success(true)
        
        // When
        val result = dataExportUseCase.validateImportFile(filePath, dataType)
        
        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        coVerify { dataExportRepository.validateImportFile(filePath, dataType) }
    }
    
    @Test
    fun `previewImportData should return preview from repository`() = runTest {
        // Given
        val filePath = "/test/file.json"
        val dataType = ImportDataType.HEALTH_DATA
        val preview = ImportPreview(
            recordCount = 100,
            dataTypes = listOf("Health Metrics"),
            dateRange = null,
            conflicts = emptyList(),
            warnings = emptyList()
        )
        coEvery { dataExportRepository.previewImportData(filePath, dataType) } returns Result.success(preview)
        
        // When
        val result = dataExportUseCase.previewImportData(filePath, dataType)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(preview, result.getOrNull())
    }
    
    @Test
    fun `generateGdprExport should call repository`() = runTest {
        // Given
        val file = mockk<File>()
        coEvery { dataExportRepository.generateGdprExport(testUserId) } returns Result.success(file)
        
        // When
        val result = dataExportUseCase.generateGdprExport(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataExportRepository.generateGdprExport(testUserId) }
    }
    
    @Test
    fun `generateCcpaExport should call repository`() = runTest {
        // Given
        val file = mockk<File>()
        coEvery { dataExportRepository.generateCcpaExport(testUserId) } returns Result.success(file)
        
        // When
        val result = dataExportUseCase.generateCcpaExport(testUserId)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataExportRepository.generateCcpaExport(testUserId) }
    }
    
    @Test
    fun `requestDataDeletion should schedule deletion with correct delay`() = runTest {
        // Given
        val delayDays = 30
        coEvery { dataExportRepository.scheduleDataDeletion(eq(testUserId), any()) } returns Result.success(Unit)
        
        // When
        val result = dataExportUseCase.requestDataDeletion(testUserId, delayDays)
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { 
            dataExportRepository.scheduleDataDeletion(
                eq(testUserId),
                match { date -> 
                    date.isAfter(LocalDateTime.now().plusDays(delayDays.toLong() - 1))
                }
            )
        }
    }
    
    @Test
    fun `createFullBackup should call repository with correct parameters`() = runTest {
        // Given
        val file = mockk<File>()
        val includeMedia = true
        coEvery { dataExportRepository.createBackup(testUserId, includeMedia) } returns Result.success(file)
        
        // When
        val result = dataExportUseCase.createFullBackup(testUserId, includeMedia)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataExportRepository.createBackup(testUserId, includeMedia) }
    }
    
    @Test
    fun `validateExportRequest should return success for valid request`() {
        // Given
        val validRequest = createTestExportRequest()
        
        // When
        val result = dataExportUseCase.validateExportRequest(validRequest)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `validateExportRequest should return failure for blank user ID`() {
        // Given
        val invalidRequest = createTestExportRequest().copy(userId = "")
        
        // When
        val result = dataExportUseCase.validateExportRequest(invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("User ID cannot be blank") == true)
    }
    
    @Test
    fun `validateExportRequest should return failure when no data types selected`() {
        // Given
        val invalidRequest = createTestExportRequest().copy(
            includeHealthData = false,
            includeMealData = false,
            includeSupplementData = false,
            includeBiomarkerData = false,
            includeGoalData = false
        )
        
        // When
        val result = dataExportUseCase.validateExportRequest(invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("At least one data type must be included") == true)
    }
    
    @Test
    fun `validateExportRequest should return failure for invalid date range`() {
        // Given
        val invalidDateRange = DateRange(
            LocalDateTime.now(),
            LocalDateTime.now().minusDays(1) // End before start
        )
        val invalidRequest = createTestExportRequest().copy(dateRange = invalidDateRange)
        
        // When
        val result = dataExportUseCase.validateExportRequest(invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Start date cannot be after end date") == true)
    }
    
    @Test
    fun `validateImportRequest should return success for valid request`() {
        // Given
        val validRequest = ImportRequest(
            userId = testUserId,
            sourceApp = "TestApp",
            filePath = createTempFile().absolutePath,
            dataType = ImportDataType.HEALTH_DATA,
            mergeStrategy = MergeStrategy.MERGE_NEW_ONLY
        )
        
        // When
        val result = dataExportUseCase.validateImportRequest(validRequest)
        
        // Then
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `validateImportRequest should return failure for blank user ID`() {
        // Given
        val invalidRequest = ImportRequest(
            userId = "",
            sourceApp = "TestApp",
            filePath = "/test/path",
            dataType = ImportDataType.HEALTH_DATA,
            mergeStrategy = MergeStrategy.MERGE_NEW_ONLY
        )
        
        // When
        val result = dataExportUseCase.validateImportRequest(invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("User ID cannot be blank") == true)
    }
    
    @Test
    fun `validateImportRequest should return failure for non-existent file`() {
        // Given
        val invalidRequest = ImportRequest(
            userId = testUserId,
            sourceApp = "TestApp",
            filePath = "/non/existent/file.json",
            dataType = ImportDataType.HEALTH_DATA,
            mergeStrategy = MergeStrategy.MERGE_NEW_ONLY
        )
        
        // When
        val result = dataExportUseCase.validateImportRequest(invalidRequest)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Import file does not exist") == true)
    }
    
    // Helper methods
    private fun createTestExportRequest(): ExportRequest {
        return ExportRequest(
            userId = testUserId,
            exportType = ExportType.FULL_BACKUP,
            format = ExportFormat.JSON,
            dateRange = null,
            includeHealthData = true,
            includeMealData = true,
            includeSupplementData = true,
            includeBiomarkerData = true,
            includeGoalData = true
        )
    }
    
    private fun createTestDataExport(): DataExport {
        return DataExport(
            id = testExportId,
            userId = testUserId,
            exportType = ExportType.FULL_BACKUP,
            format = ExportFormat.JSON,
            status = ExportStatus.COMPLETED,
            filePath = "/test/export.json",
            fileSize = 1024L,
            dateRange = null,
            includeHealthData = true,
            includeMealData = true,
            includeSupplementData = true,
            includeBiomarkerData = true,
            includeGoalData = true,
            createdAt = LocalDateTime.now().minusHours(1),
            completedAt = LocalDateTime.now(),
            errorMessage = null
        )
    }
    
    private fun createTempFile(): File {
        val tempFile = File.createTempFile("test", ".json")
        tempFile.writeText("{\"test\": \"data\"}")
        tempFile.deleteOnExit()
        return tempFile
    }
}