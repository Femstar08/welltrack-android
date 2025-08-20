package com.beaconledger.welltrack.presentation.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.HealthMetric
import com.beaconledger.welltrack.data.model.HealthMetricType
import com.beaconledger.welltrack.domain.usecase.HealthConnectUseCase
import com.beaconledger.welltrack.domain.usecase.HealthSummary
import com.beaconledger.welltrack.domain.usecase.HealthTrends
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HealthConnectViewModel @Inject constructor(
    private val healthConnectUseCase: HealthConnectUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HealthConnectUiState())
    val uiState: StateFlow<HealthConnectUiState> = _uiState.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    
    init {
        checkHealthConnectAvailability()
    }
    
    fun setCurrentUser(userId: String) {
        _currentUserId.value = userId
        checkPermissions()
        loadTodaysSummary()
    }
    
    private fun checkHealthConnectAvailability() {
        viewModelScope.launch {
            try {
                val isAvailable = healthConnectUseCase.isHealthConnectAvailable()
                _uiState.update { it.copy(isHealthConnectAvailable = isAvailable) }
                
                if (isAvailable) {
                    checkPermissions()
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isHealthConnectAvailable = false,
                        error = "Failed to check Health Connect availability: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun checkPermissions() {
        viewModelScope.launch {
            try {
                val hasPermissions = healthConnectUseCase.hasAllPermissions()
                val requiredPermissions = healthConnectUseCase.getRequiredPermissions()
                
                _uiState.update { 
                    it.copy(
                        hasAllPermissions = hasPermissions,
                        requiredPermissions = requiredPermissions
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to check permissions: ${e.message}")
                }
            }
        }
    }
    
    fun syncHealthData(days: Int = 7) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true, error = null) }
            
            try {
                val result = healthConnectUseCase.syncHealthData(userId, days)
                
                if (result.isSuccess) {
                    _uiState.update { 
                        it.copy(
                            isSyncing = false,
                            lastSyncTime = System.currentTimeMillis()
                        )
                    }
                    loadTodaysSummary()
                    loadHealthTrends()
                } else {
                    _uiState.update { 
                        it.copy(
                            isSyncing = false,
                            error = "Sync failed: ${result.exceptionOrNull()?.message}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSyncing = false,
                        error = "Sync failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun loadTodaysSummary() {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                val summary = healthConnectUseCase.getHealthSummaryForToday(userId)
                _uiState.update { it.copy(todaysSummary = summary) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load today's summary: ${e.message}")
                }
            }
        }
    }
    
    private fun loadHealthTrends(days: Int = 30) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                val trends = healthConnectUseCase.getHealthTrends(userId, days)
                _uiState.update { it.copy(healthTrends = trends) }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to load health trends: ${e.message}")
                }
            }
        }
    }
    
    fun loadHealthMetrics(
        type: HealthMetricType,
        startDate: LocalDate,
        endDate: LocalDate
    ) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            healthConnectUseCase.getHealthMetrics(userId, type, startDate, endDate)
                .catch { e ->
                    _uiState.update { 
                        it.copy(error = "Failed to load health metrics: ${e.message}")
                    }
                }
                .collect { metrics ->
                    _uiState.update { 
                        it.copy(selectedMetrics = metrics)
                    }
                }
        }
    }
    
    fun getLatestMetric(type: HealthMetricType) {
        val userId = _currentUserId.value ?: return
        
        viewModelScope.launch {
            try {
                val metric = healthConnectUseCase.getLatestHealthMetric(userId, type)
                _uiState.update { 
                    it.copy(latestMetric = metric)
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to get latest metric: ${e.message}")
                }
            }
        }
    }
    
    fun insertManualHealthMetric(healthMetric: HealthMetric) {
        viewModelScope.launch {
            try {
                val result = healthConnectUseCase.insertHealthMetric(healthMetric)
                
                if (result.isSuccess) {
                    loadTodaysSummary()
                    _uiState.update { 
                        it.copy(error = null)
                    }
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to save metric: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to save metric: ${e.message}")
                }
            }
        }
    }
    
    fun deleteHealthMetric(metricId: String) {
        viewModelScope.launch {
            try {
                val result = healthConnectUseCase.deleteHealthMetric(metricId)
                
                if (result.isSuccess) {
                    loadTodaysSummary()
                    _uiState.update { 
                        it.copy(error = null)
                    }
                } else {
                    _uiState.update { 
                        it.copy(error = "Failed to delete metric: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to delete metric: ${e.message}")
                }
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun refreshData() {
        checkPermissions()
        loadTodaysSummary()
        loadHealthTrends()
    }
}

data class HealthConnectUiState(
    val isHealthConnectAvailable: Boolean = false,
    val hasAllPermissions: Boolean = false,
    val requiredPermissions: Set<String> = emptySet(),
    val isSyncing: Boolean = false,
    val lastSyncTime: Long? = null,
    val todaysSummary: HealthSummary? = null,
    val healthTrends: HealthTrends? = null,
    val selectedMetrics: List<HealthMetric> = emptyList(),
    val latestMetric: HealthMetric? = null,
    val error: String? = null
)