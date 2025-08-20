package com.beaconledger.welltrack.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.domain.usecase.DataSyncUseCase
import com.beaconledger.welltrack.domain.repository.ConflictResolution
import com.beaconledger.welltrack.data.model.SyncResult
import com.beaconledger.welltrack.data.backup.ExportFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DataSyncViewModel @Inject constructor(
    private val dataSyncUseCase: DataSyncUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DataSyncUiState())
    val uiState: StateFlow<DataSyncUiState> = _uiState.asStateFlow()
    
    init {
        observeSyncStatus()
        loadSyncSummary()
    }
    
    private fun observeSyncStatus() {
        viewModelScope.launch {
            dataSyncUseCase.observeSyncStatus().collect { syncStats ->
                _uiState.value = _uiState.value.copy(
                    syncStats = syncStats,
                    isLoading = false
                )
            }
        }
    }
    
    private fun loadSyncSummary() {
        viewModelScope.launch {
            try {
                val summary = dataSyncUseCase.getSyncSummary()
                _uiState.value = _uiState.value.copy(
                    syncSummary = summary,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isLoading = false
                )
            }
        }
    }
    
    fun performFullSync() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = dataSyncUseCase.performFullSync()
                when (result) {
                    is SyncResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Sync completed successfully"
                        )
                    }
                    is SyncResult.Conflict -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            conflicts = result.conflicts,
                            message = "Sync completed with ${result.conflicts.size} conflicts"
                        )
                    }
                    is SyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is SyncResult.PartialSuccess -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Partial sync: ${result.successCount} succeeded, ${result.failureCount} failed"
                        )
                    }
                }
                loadSyncSummary()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun createBackup(userId: String, includeEncryption: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = dataSyncUseCase.createBackup(userId, includeEncryption)
                when (result) {
                    is com.beaconledger.welltrack.data.backup.BackupResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Backup created successfully"
                        )
                    }
                    is com.beaconledger.welltrack.data.backup.BackupResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun exportData(userId: String, format: ExportFormat) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = dataSyncUseCase.exportData(userId, format)
                when (result) {
                    is com.beaconledger.welltrack.data.backup.ExportResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            message = "Data exported successfully"
                        )
                    }
                    is com.beaconledger.welltrack.data.backup.ExportResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    fun resolveConflict(conflictId: String, resolution: ConflictResolution) {
        viewModelScope.launch {
            try {
                val result = dataSyncUseCase.resolveConflict(conflictId, resolution)
                when (result) {
                    is SyncResult.Success -> {
                        // Remove resolved conflict from UI state
                        val updatedConflicts = _uiState.value.conflicts.filter { it.entityId != conflictId }
                        _uiState.value = _uiState.value.copy(
                            conflicts = updatedConflicts,
                            message = "Conflict resolved successfully"
                        )
                    }
                    is SyncResult.Error -> {
                        _uiState.value = _uiState.value.copy(error = result.message)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearCache() {
        viewModelScope.launch {
            try {
                dataSyncUseCase.clearCache()
                _uiState.value = _uiState.value.copy(
                    message = "Cache cleared successfully"
                )
                loadSyncSummary()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
    
    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class DataSyncUiState(
    val isLoading: Boolean = false,
    val syncStats: com.beaconledger.welltrack.data.model.SyncStats? = null,
    val syncSummary: com.beaconledger.welltrack.domain.usecase.SyncSummary? = null,
    val conflicts: List<com.beaconledger.welltrack.data.model.SyncConflict> = emptyList(),
    val message: String? = null,
    val error: String? = null
)