package com.beaconledger.welltrack.presentation.compliance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.compliance.BrandComplianceResult
import com.beaconledger.welltrack.data.compliance.BrandComplianceValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for brand compliance validation screen
 * 
 * Manages the state of brand compliance validation including:
 * - Running comprehensive brand compliance checks
 * - Managing UI state for validation results
 * - Handling user interactions with compliance details
 */
@HiltViewModel
class BrandComplianceViewModel @Inject constructor(
    private val brandComplianceValidator: BrandComplianceValidator
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(BrandComplianceUiState())
    val uiState: StateFlow<BrandComplianceUiState> = _uiState.asStateFlow()
    
    /**
     * Validate brand compliance for all third-party integrations
     */
    fun validateBrandCompliance() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val result = brandComplianceValidator.validateBrandCompliance()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    complianceResult = result,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to validate brand compliance: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Toggle expanded state for compliance check details
     */
    fun toggleCheckDetails(requirement: String) {
        val currentExpanded = _uiState.value.expandedChecks
        val newExpanded = if (currentExpanded.contains(requirement)) {
            currentExpanded - requirement
        } else {
            currentExpanded + requirement
        }
        
        _uiState.value = _uiState.value.copy(
            expandedChecks = newExpanded
        )
    }
    
    /**
     * Clear any error state
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for brand compliance screen
 */
data class BrandComplianceUiState(
    val isLoading: Boolean = false,
    val complianceResult: BrandComplianceResult? = null,
    val expandedChecks: Set<String> = emptySet(),
    val error: String? = null
)