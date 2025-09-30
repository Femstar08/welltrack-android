package com.beaconledger.welltrack.presentation.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.compliance.GarminComplianceResult
import com.beaconledger.welltrack.data.compliance.GarminLegalComplianceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Garmin compliance validation and display
 */
@HiltViewModel
class GarminComplianceViewModel @Inject constructor(
    private val garminLegalComplianceManager: GarminLegalComplianceManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(GarminComplianceUiState())
    val uiState: StateFlow<GarminComplianceUiState> = _uiState.asStateFlow()
    
    /**
     * Validate Garmin compliance requirements
     */
    fun validateCompliance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val complianceResult = garminLegalComplianceManager.validateDeveloperProgramCompliance()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    complianceResult = complianceResult,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to validate compliance: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Open Garmin privacy policy in browser
     */
    fun openGarminPrivacyPolicy() {
        garminLegalComplianceManager.openGarminPrivacyPolicy()
    }
    
    /**
     * Open Garmin developer documentation
     */
    fun openGarminDeveloperDocs() {
        garminLegalComplianceManager.openGarminDeveloperDocs()
    }
    
    /**
     * Get app store listing requirements
     */
    fun getAppStoreRequirements() = garminLegalComplianceManager.getAppStoreListingRequirements()
    
    /**
     * Get legal disclaimers
     */
    fun getLegalDisclaimers() = garminLegalComplianceManager.getGarminLegalDisclaimers()
}

/**
 * UI state for Garmin compliance screen
 */
data class GarminComplianceUiState(
    val isLoading: Boolean = false,
    val complianceResult: GarminComplianceResult? = null,
    val error: String? = null
)