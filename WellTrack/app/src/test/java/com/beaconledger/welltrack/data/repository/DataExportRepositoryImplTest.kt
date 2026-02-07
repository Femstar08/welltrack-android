package com.beaconledger.welltrack.data.repository

import com.beaconledger.welltrack.data.compliance.DataPortabilityManager
import com.beaconledger.welltrack.data.database.dao.DataExportDao
import com.beaconledger.welltrack.data.export.DataExportManager
import com.beaconledger.welltrack.data.export.PdfReportGenerator
import com.beaconledger.welltrack.data.import.DataImportManager
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.repository.*
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDateTime

class DataExportRepositoryImplTest {

    private lateinit var dataExportDao: DataExportDao
    private lateinit var dataExportManager: DataExportManager
    private lateinit var dataImportManager: DataImportManager
    private lateinit var pdfReportGenerator: PdfReportGenerator
    private lateinit var dataPortabilityManager: DataPortabilityManager
    private lateinit var dataExportRepository: DataExportRepositoryImpl

    private val testUserId = "test-user-123"
    private val testExportId = "export-123"

    @Before
    fun setup() {
        dataExportDao = mockk(relaxed = true)
        dataExportManager = mockk(relaxed = true)
        dataImportManager = mockk(relaxed = true)
        pdfReportGenerator = mockk(relaxed = true)
        dataPortabilityManager = mockk(relaxed = true)

        dataExportRepository = DataExportRepositoryImpl(
            dataExportDao,
            dataExportManager,
            dataImportManager,
            pdfReportGenerator,
            dataPortabilityManager
        )
    }

    @Test
    fun `createExport should insert export and process it`() = runTest {
        // Given
        val request = createTestExportRequest()
        coEvery { dataExportDao.insertExport(any()) } just Runs
        coEvery { dataExportManager.exportUserDataToJson(any(), any()) } returns Result.success(File("test.json"))

        // When
        val result = dataExportRepository.createExport(request)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataExportDao.insertExport(any()) }
        coVerify { dataExportDao.updateExportStatus(any(), ExportStatus.IN_PROGRESS, any(), any(), any()) }
        coVerify { dataExportManager.exportUserDataToJson(any(), any()) }
        coVerify { dataExportDao.updateExportStatus(any(), ExportStatus.COMPLETED, any(), any(), any()) }
    }

    @Test
    fun `getExportHistory should return flow from DAO`() = runTest {
        // Given
        val exports = listOf(createTestDataExport())
        every { dataExportDao.getExportHistory(testUserId) } returns flowOf(exports)

        // When
        val result = dataExportRepository.getExportHistory(testUserId)

        // Then
        result.collect { exportList ->
            assertEquals(exports, exportList)
        }
        verify { dataExportDao.getExportHistory(testUserId) }
    }

    @Test
    fun `getExportById should return export from DAO`() = runTest {
        // Given
        val export = createTestDataExport()
        coEvery { dataExportDao.getExportById(testExportId) } returns export

        // When
        val result = dataExportRepository.getExportById(testExportId)

        // Then
        assertEquals(export, result)
        coVerify { dataExportDao.getExportById(testExportId) }
    }

    @Test
    fun `cancelExport should update export status to cancelled`() = runTest {
        // Given
        coEvery { dataExportDao.updateExportError(testExportId, ExportStatus.CANCELLED, any()) } just Runs

        // When
        val result = dataExportRepository.cancelExport(testExportId)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataExportDao.updateExportError(testExportId, ExportStatus.CANCELLED, "Cancelled by user") }
    }

    @Test
    fun `deleteExport should delete file and export from DAO`() = runTest {
        // Given
        val tempFile = File.createTempFile("test", ".json")
        tempFile.writeText("test data")
        val export = createTestDataExport().copy(filePath = tempFile.absolutePath)
        coEvery { dataExportDao.getExportById(testExportId) } returns export
        coEvery { dataExportDao.deleteExport(testExportId) } just Runs

        // When
        val result = dataExportRepository.deleteExport(testExportId)

        // Then
        assertTrue(result.isSuccess)
        assertFalse(tempFile.exists())
        coVerify { dataExportDao.getExportById(testExportId) }
        coVerify { dataExportDao.deleteExport(testExportId) }
    }

    @Test
    fun `exportToJson should call dataExportManager`() = runTest {
        // Given
        val request = createTestExportRequest()
        val file = File("test.json")
        coEvery { dataExportManager.exportUserDataToJson(any(), any()) } returns Result.success(file)

        // When
        val result = dataExportRepository.exportToJson(request)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataExportManager.exportUserDataToJson(request.userId, request) }
    }

    @Test
    fun `exportToCsv should call dataExportManager`() = runTest {
        // Given
        val request = createTestExportRequest()
        val files = listOf(File("test.csv"))
        coEvery { dataExportManager.exportUserDataToCsv(any(), any()) } returns Result.success(files)

        // When
        val result = dataExportRepository.exportToCsv(request)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(files, result.getOrNull())
        coVerify { dataExportManager.exportUserDataToCsv(request.userId, request) }
    }

    @Test
    fun `exportToPdf should generate health report and then PDF`() = runTest {
        // Given
        val request = createTestExportRequest().copy(format = ExportFormat.PDF)
        val healthReport = createTestHealthReport()
        val outputFile = File("report.pdf")
        coEvery { dataExportRepository.generateHealthReport(any(), any()) } returns Result.success(healthReport)
        coEvery { pdfReportGenerator.generateHealthReport(any(), any()) } just Runs

        // When
        val result = dataExportRepository.exportToPdf(request)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataExportRepository.generateHealthReport(request.userId, any()) }
        coVerify { pdfReportGenerator.generateHealthReport(healthReport, any()) }
    }

    @Test
    fun `createFullBackup should call dataExportManager to export to JSON`() = runTest {
        // Given
        val file = File("backup.json")
        coEvery { dataExportManager.exportUserDataToJson(any(), any()) } returns Result.success(file)

        // When
        val result = dataExportRepository.createFullBackup(testUserId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify {
            dataExportManager.exportUserDataToJson(
                testUserId,
                match { req -> req.exportType == ExportType.FULL_BACKUP && req.format == ExportFormat.JSON }
            )
        }
    }

    @Test
    fun `generateHealthReport should return a simplified HealthReport`() = runTest {
        // Given
        val dateRange = ExportDateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now())

        // When
        val result = dataExportRepository.generateHealthReport(testUserId, dateRange)

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertEquals(testUserId, result.getOrNull()?.userId)
    }

    @Test
    fun `generatePdfHealthReport should generate health report and then PDF`() = runTest {
        // Given
        val dateRange = ExportDateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now())
        val healthReport = createTestHealthReport()
        val outputFile = File("report.pdf")
        coEvery { dataExportRepository.generateHealthReport(any(), any()) } returns Result.success(healthReport)
        coEvery { pdfReportGenerator.generateHealthReport(any(), any()) } just Runs

        // When
        val result = dataExportRepository.generatePdfHealthReport(testUserId, dateRange)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataExportRepository.generateHealthReport(testUserId, dateRange) }
        coVerify { pdfReportGenerator.generateHealthReport(healthReport, any()) }
    }

    @Test
    fun `importData should call dataImportManager`() = runTest {
        // Given
        val request = ImportRequest(
            userId = testUserId,
            sourceApp = "TestApp",
            filePath = "/test/path",
            dataType = ImportDataType.HEALTH_DATA,
            mergeStrategy = MergeStrategy.MERGE_NEW_ONLY
        )
        coEvery { dataImportManager.importData(any()) } returns Result.success(Unit)

        // When
        val result = dataExportRepository.importData(request)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataImportManager.importData(request) }
    }

    @Test
    fun `validateImportFile should call dataImportManager`() = runTest {
        // Given
        val filePath = "/test/file.json"
        val dataType = ImportDataType.MEAL_DATA
        coEvery { dataImportManager.validateImportFile(any(), any()) } returns Result.success(true)

        // When
        val result = dataExportRepository.validateImportFile(filePath, dataType)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
        coVerify { dataImportManager.validateImportFile(filePath, dataType) }
    }

    @Test
    fun `previewImportData should call dataImportManager`() = runTest {
        // Given
        val filePath = "/test/file.json"
        val dataType = ImportDataType.HEALTH_DATA
        val preview = ImportPreview(
            fileName = "test.json",
            fileSize = 100L,
            dataType = "Health Metrics",
            recordCount = 100,
            dateRange = null,
            conflicts = emptyList(),
            warnings = emptyList()
        )
        coEvery { dataImportManager.previewImportData(any(), any()) } returns Result.success(preview)

        // When
        val result = dataExportRepository.previewImportData(filePath, dataType)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(preview, result.getOrNull())
        coVerify { dataImportManager.previewImportData(filePath, dataType) }
    }

    @Test
    fun `generateGdprExport should call dataPortabilityManager`() = runTest {
        // Given
        val file = File("gdpr.zip")
        coEvery { dataPortabilityManager.generateGdprExport(any()) } returns Result.success(file)

        // When
        val result = dataExportRepository.generateGdprExport(testUserId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataPortabilityManager.generateGdprExport(testUserId) }
    }

    @Test
    fun `generateCcpaExport should call dataPortabilityManager`() = runTest {
        // Given
        val file = File("ccpa.zip")
        coEvery { dataPortabilityManager.generateCcpaExport(any()) } returns Result.success(file)

        // When
        val result = dataExportRepository.generateCcpaExport(testUserId)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataPortabilityManager.generateCcpaExport(testUserId) }
    }

    @Test
    fun `scheduleDataDeletion should call dataPortabilityManager`() = runTest {
        // Given
        val deletionDate = LocalDateTime.now().plusDays(30)
        coEvery { dataPortabilityManager.scheduleDataDeletion(any(), any()) } returns Result.success(Unit)

        // When
        val result = dataExportRepository.scheduleDataDeletion(testUserId, deletionDate)

        // Then
        assertTrue(result.isSuccess)
        coVerify { dataPortabilityManager.scheduleDataDeletion(testUserId, deletionDate) }
    }

    @Test
    fun `createBackup should call createFullBackup`() = runTest {
        // Given
        val file = File("backup.json")
        coEvery { dataExportManager.exportUserDataToJson(any(), any()) } returns Result.success(file)

        // When
        val result = dataExportRepository.createBackup(testUserId, true)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(file, result.getOrNull())
        coVerify { dataExportManager.exportUserDataToJson(testUserId, any()) }
    }

    @Test
    fun `restoreFromBackup should call dataImportManager`() = runTest {
        // Given
        val backupFile = File("backup.json")
        coEvery { dataImportManager.importData(any()) } returns Result.success(Unit)

        // When
        val result = dataExportRepository.restoreFromBackup(testUserId, backupFile)

        // Then
        assertTrue(result.isSuccess)
        coVerify {
            dataImportManager.importData(
                match { req ->
                    req.userId == testUserId &&
                    req.filePath == backupFile.absolutePath &&
                    req.dataType == ImportDataType.FULL_BACKUP &&
                    req.mergeStrategy == MergeStrategy.REPLACE_ALL
                }
            )
        }
    }

    @Test
    fun `validateBackup should return a valid BackupValidation`() = runTest {
        // When
        val result = dataExportRepository.validateBackup(File("backup.json"))

        // Then
        assertTrue(result.isSuccess)
        assertNotNull(result.getOrNull())
        assertTrue(result.getOrNull()!!.isValid)
    }

    @Test
    fun `shareHealthReport should return success`() = runTest {
        // Given
        val reportFile = File("report.pdf")
        val shareMethod = ShareMethod.EMAIL

        // When
        val result = dataExportRepository.shareHealthReport(reportFile, shareMethod)

        // Then
        assertTrue(result.isSuccess)
    }

    @Test
    fun `generateShareableLink should return a link`() = runTest {
        // When
        val result = dataExportRepository.generateShareableLink(testExportId, 24)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.startsWith("https://welltrack.app/shared/") == true)
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

    private fun createTestHealthReport(): HealthReport {
        return HealthReport(
            userId = testUserId,
            reportPeriod = ExportDateRange(LocalDateTime.now().minusMonths(1), LocalDateTime.now()),
            summary = HealthSummary(0, 0f, 0f, 0, 0, 0),
            nutritionAnalysis = NutritionAnalysis(0.0, emptyMap(), emptyMap(), 0.0, emptyMap()),
            fitnessMetrics = FitnessMetrics(0, null, 0, null, null),
            supplementAdherence = ExportSupplementAdherence(0, 0f, 0, emptyMap()),
            biomarkerTrends = emptyList(),
            goalProgress = emptyList(),
            recommendations = emptyList(),
            generatedAt = LocalDateTime.now()
        )
    }
}
