package com.beaconledger.welltrack.presentation.dataexport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.domain.usecase.DataExportUseCase
import com.beaconledger.welltrack.domain.repository.ImportPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class DataExportViewModel @Inject constructor(
    private val dataExportUseCase: DataExportUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DataExportUiState())
    val uiState: StateFlow<DataExportUiState> = _uiState.asStateFlow()
    
    private val _exportHistory = MutableStateFlow<List<DataExport>>(emptyList())
    val exportHistory: StateFlow<List<DataExport>> = _exportHistory.asStateFlow()
    
    fun loadExportHistory(userId: String) {
        viewModelScope.launch {
            dataExportUseCase.getExportHistory(userId).collect { exports ->
                _exportHistory.value = exports
            }
        }
    }
    
    fun createExport(request: ExportRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            val validationResult = dataExportUseCase.validateExportRequest(request)
            if (validationResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = validationResult.exceptionOrNull()?.message
                )
                return@launch
            }
            
            dataExportUseCase.createExport(request).fold(
                onSuccess = { exportId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastExportId = exportId,
                        showSuccessMessage = "Export started successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun exportAllDataAsJson(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.exportAllDataAsJson(userId).fold(
                onSuccess = { exportId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastExportId = exportId,
                        showSuccessMessage = "Full data export started"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun exportHealthReportAsPdf(userId: String, dateRange: ExportDateRange? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.exportHealthReportAsPdf(userId, dateRange).fold(
                onSuccess = { exportId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastExportId = exportId,
                        showSuccessMessage = "Health report export started"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun exportMealHistoryAsCsv(userId: String, dateRange: ExportDateRange? = null) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.exportMealHistoryAsCsv(userId, dateRange).fold(
                onSuccess = { exportId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        lastExportId = exportId,
                        showSuccessMessage = "Meal history export started"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun cancelExport(exportId: String) {
        viewModelScope.launch {
            dataExportUseCase.cancelExport(exportId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        showSuccessMessage = "Export cancelled"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun deleteExport(exportId: String) {
        viewModelScope.launch {
            dataExportUseCase.deleteExport(exportId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        showSuccessMessage = "Export deleted"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Import functionality
    fun validateImportFile(filePath: String, dataType: ImportDataType) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isValidatingImport = true, importError = null)
            
            dataExportUseCase.validateImportFile(filePath, dataType).fold(
                onSuccess = { isValid ->
                    if (isValid) {
                        previewImportData(filePath, dataType)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isValidatingImport = false,
                            importError = "Invalid import file format"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isValidatingImport = false,
                        importError = error.message
                    )
                }
            )
        }
    }
    
    fun previewImportData(filePath: String, dataType: ImportDataType) {
        viewModelScope.launch {
            dataExportUseCase.previewImportData(filePath, dataType).fold(
                onSuccess = { preview ->
                    _uiState.value = _uiState.value.copy(
                        isValidatingImport = false,
                        importPreview = preview,
                        showImportPreview = true
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isValidatingImport = false,
                        importError = error.message
                    )
                }
            )
        }
    }
    
    fun importData(request: ImportRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true, importError = null)
            
            val validationResult = dataExportUseCase.validateImportRequest(request)
            if (validationResult.isFailure) {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    importError = validationResult.exceptionOrNull()?.message
                )
                return@launch
            }
            
            dataExportUseCase.importData(request).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        showImportPreview = false,
                        showSuccessMessage = "Data imported successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importError = error.message
                    )
                }
            )
        }
    }
    
    // Data Portability and Compliance
    fun generateGdprExport(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.generateGdprExport(userId).fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessMessage = "GDPR export generated: ${file.name}"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun generateCcpaExport(userId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.generateCcpaExport(userId).fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessMessage = "CCPA export generated: ${file.name}"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun requestDataDeletion(userId: String, delayDays: Int = 30) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.requestDataDeletion(userId, delayDays).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessMessage = "Data deletion scheduled for $delayDays days from now"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Backup and Restore
    fun createFullBackup(userId: String, includeMedia: Boolean = true) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.createFullBackup(userId, includeMedia).fold(
                onSuccess = { file ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessMessage = "Backup created: ${file.name}"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun restoreFromBackup(userId: String, backupFile: File) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.restoreFromBackup(userId, backupFile).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        showSuccessMessage = "Data restored from backup successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    // Healthcare Provider Sharing
    fun shareHealthReportWithProvider(
        userId: String,
        dateRange: ExportDateRange
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            dataExportUseCase.shareHealthReportWithProvider(userId, dateRange).fold(
                onSuccess = { shareLink ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        shareLink = shareLink,
                        showSuccessMessage = "Health report shared successfully"
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message
                    )
                }
            )
        }
    }
    
    // UI State Management
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearImportError() {
        _uiState.value = _uiState.value.copy(importError = null)
    }
    
    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(showSuccessMessage = null)
    }
    
    fun hideImportPreview() {
        _uiState.value = _uiState.value.copy(
            showImportPreview = false,
            importPreview = null
        )
    }
    
    fun updateExportRequest(request: ExportRequest) {
        _uiState.value = _uiState.value.copy(currentExportRequest = request)
    }
}

data class DataExportUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val showSuccessMessage: String? = null,
    val lastExportId: String? = null,
    val currentExportRequest: ExportRequest? = null,
    
    // Import states
    val isValidatingImport: Boolean = false,
    val isImporting: Boolean = false,
    val importError: String? = null,
    val importPreview: ImportPreview? = null,
    val showImportPreview: Boolean = false,
    
    // Sharing states
    val shareLink: String? = null
)