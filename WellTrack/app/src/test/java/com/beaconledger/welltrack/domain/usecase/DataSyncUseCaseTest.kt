package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.domain.repository.DataSyncRepository
import com.beaconledger.welltrack.domain.repository.ConflictResolution
import com.beaconledger.welltrack.data.model.SyncResult
import com.beaconledger.welltrack.data.model.SyncStats
import com.beaconledger.welltrack.data.model.SyncConflict
import com.beaconledger.welltrack.data.backup.BackupResult
import com.beaconledger.welltrack.data.backup.ExportResult
import com.beaconledger.welltrack.data.backup.ExportFormat
import com.beaconledger.welltrack.data.cache.CacheStats
import android.net.Uri
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.time.LocalDateTime

class DataSyncUseCaseTest {
    
    private lateinit var dataSyncRepository: DataSyncRepository
    private lateinit var dataSyncUseCase: DataSyncUseCase
    
    @Before
    fun setup() {
        dataSyncRepository = mockk()
        dataSyncUseCase = DataSyncUseCase(dataSyncRepository)
    }
    
    @Test
    fun `performFullSync should return success when repository succeeds`() = runTest {
        // Given
        coEvery { dataSyncRepository.performFullSync() } returns SyncResult.Success
        
        // When
        val result = dataSyncUseCase.performFullSync()
        
        // Then
        assertEquals(SyncResult.Success, result)
        coVerify { dataSyncRepository.performFullSync() }
    }
    
    @Test
    fun `performFullSync should return error when repository fails`() = runTest {
        // Given
        val errorMessage = "Sync failed"
        coEvery { dataSyncRepository.performFullSync() } returns SyncResult.Error(errorMessage, null)
        
        // When
        val result = dataSyncUseCase.performFullSync()
        
        // Then
        assertTrue(result is SyncResult.Error)
        assertEquals(errorMessage, (result as SyncResult.Error).message)
    }
    
    @Test
    fun `syncSpecificData should call repository with correct entity type`() = runTest {
        // Given
        val entityType = "HealthMetric"
        coEvery { dataSyncRepository.syncEntityType(entityType) } returns SyncResult.Success
        
        // When
        val result = dataSyncUseCase.syncSpecificData(entityType)
        
        // Then
        assertEquals(SyncResult.Success, result)
        coVerify { dataSyncRepository.syncEntityType(entityType) }
    }
    
    @Test
    fun `getSyncStats should return correct statistics`() = runTest {
        // Given
        val expectedStats = SyncStats(
            pendingUpload = 5,
            pendingDownload = 3,
            conflicts = 2,
            failed = 1,
            synced = 10
        )
        coEvery { dataSyncRepository.getSyncStats() } returns expectedStats
        
        // When
        val result = dataSyncUseCase.getSyncStats()
        
        // Then
        assertEquals(expectedStats, result)
    }
    
    @Test
    fun `createBackup should return success when backup is created`() = runTest {
        // Given
        val userId = "user123"
        val backupFile = mockk<File>()
        coEvery { dataSyncRepository.createBackup(userId, true) } returns BackupResult.Success(backupFile)
        
        // When
        val result = dataSyncUseCase.createBackup(userId, true)
        
        // Then
        assertTrue(result is BackupResult.Success)
        assertEquals(backupFile, (result as BackupResult.Success).file)
    }
    
    @Test
    fun `exportData should return success when export is created`() = runTest {
        // Given
        val userId = "user123"
        val format = ExportFormat.JSON
        val exportFile = mockk<File>()
        coEvery { dataSyncRepository.exportData(userId, format) } returns ExportResult.Success(exportFile)
        
        // When
        val result = dataSyncUseCase.exportData(userId, format)
        
        // Then
        assertTrue(result is ExportResult.Success)
        assertEquals(exportFile, (result as ExportResult.Success).file)
    }
    
    @Test
    fun `resolveConflict should call repository with correct parameters`() = runTest {
        // Given
        val conflictId = "conflict123"
        val resolution = ConflictResolution.USE_LOCAL
        coEvery { dataSyncRepository.resolveConflict(conflictId, resolution) } returns SyncResult.Success
        
        // When
        val result = dataSyncUseCase.resolveConflict(conflictId, resolution)
        
        // Then
        assertEquals(SyncResult.Success, result)
        coVerify { dataSyncRepository.resolveConflict(conflictId, resolution) }
    }
    
    @Test
    fun `isSyncNeeded should return true when there are pending items`() = runTest {
        // Given
        val stats = SyncStats(
            pendingUpload = 5,
            pendingDownload = 0,
            conflicts = 0,
            failed = 0,
            synced = 10
        )
        coEvery { dataSyncRepository.getSyncStats() } returns stats
        
        // When
        val result = dataSyncUseCase.isSyncNeeded()
        
        // Then
        assertTrue(result)
    }
    
    @Test
    fun `isSyncNeeded should return false when no pending items`() = runTest {
        // Given
        val stats = SyncStats(
            pendingUpload = 0,
            pendingDownload = 0,
            conflicts = 0,
            failed = 0,
            synced = 10
        )
        coEvery { dataSyncRepository.getSyncStats() } returns stats
        
        // When
        val result = dataSyncUseCase.isSyncNeeded()
        
        // Then
        assertFalse(result)
    }
    
    @Test
    fun `getSyncSummary should return correct summary with conflicts`() = runTest {
        // Given
        val syncStats = SyncStats(
            pendingUpload = 2,
            pendingDownload = 1,
            conflicts = 3,
            failed = 0,
            synced = 10
        )
        val cacheStats = CacheStats(
            totalCachedItems = 5,
            pendingUploads = 2,
            pendingDownloads = 1,
            conflicts = 3,
            lastSyncAttempt = LocalDateTime.now(),
            isConnected = true
        )
        
        coEvery { dataSyncRepository.getSyncStats() } returns syncStats
        coEvery { dataSyncRepository.getCacheStats() } returns cacheStats
        
        // When
        val result = dataSyncUseCase.getSyncSummary()
        
        // Then
        assertTrue(result.isOnline)
        assertTrue(result.hasPendingChanges)
        assertTrue(result.hasConflicts)
        assertEquals(3, result.totalPendingItems)
        assertEquals(SyncStatusType.CONFLICTS, result.syncStatus)
    }
    
    @Test
    fun `getSyncSummary should return synced status when no pending items`() = runTest {
        // Given
        val syncStats = SyncStats(
            pendingUpload = 0,
            pendingDownload = 0,
            conflicts = 0,
            failed = 0,
            synced = 10
        )
        val cacheStats = CacheStats(
            totalCachedItems = 0,
            pendingUploads = 0,
            pendingDownloads = 0,
            conflicts = 0,
            lastSyncAttempt = LocalDateTime.now(),
            isConnected = true
        )
        
        coEvery { dataSyncRepository.getSyncStats() } returns syncStats
        coEvery { dataSyncRepository.getCacheStats() } returns cacheStats
        
        // When
        val result = dataSyncUseCase.getSyncSummary()
        
        // Then
        assertEquals(SyncStatusType.SYNCED, result.syncStatus)
        assertFalse(result.hasPendingChanges)
        assertFalse(result.hasConflicts)
    }
    
    @Test
    fun `clearCache should call repository clearCache`() = runTest {
        // Given
        coEvery { dataSyncRepository.clearCache() } returns Unit
        
        // When
        dataSyncUseCase.clearCache()
        
        // Then
        coVerify { dataSyncRepository.clearCache() }
    }
    
    @Test
    fun `preloadEssentialData should call repository with correct userId`() = runTest {
        // Given
        val userId = "user123"
        coEvery { dataSyncRepository.preloadEssentialData(userId) } returns Unit
        
        // When
        dataSyncUseCase.preloadEssentialData(userId)
        
        // Then
        coVerify { dataSyncRepository.preloadEssentialData(userId) }
    }
}