package com.beaconledger.welltrack.presentation.config

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.config.ConfigurationStatus
import com.beaconledger.welltrack.config.EnvironmentConfig
import com.beaconledger.welltrack.config.SecureConfigLoader
import com.beaconledger.welltrack.config.SecurityStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfigurationViewModel @Inject constructor(
    private val environmentConfig: EnvironmentConfig,
    private val secureConfigLoader: SecureConfigLoader
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConfigurationUiState())
    val uiState: StateFlow<ConfigurationUiState> = _uiState.asStateFlow()

    fun validateConfiguration() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Validate environment configuration
                val configStatus = environmentConfig.validateConfiguration()
                
                // Validate security configuration
                val securityStatus = secureConfigLoader.validateSecurityConfiguration()
                
                // Get configuration summary
                val configSummary = environmentConfig.getConfigurationSummary()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    configStatus = configStatus,
                    securityStatus = securityStatus,
                    configSummary = configSummary,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to validate configuration: ${e.message}"
                )
            }
        }
    }

    fun openConfigurationGuide() {
        // This would typically open a web browser or in-app guide
        // For now, we'll just log the action
        println("Opening configuration guide...")
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ConfigurationUiState(
    val isLoading: Boolean = false,
    val configStatus: ConfigurationStatus? = null,
    val securityStatus: SecurityStatus? = null,
    val configSummary: Map<String, String> = emptyMap(),
    val error: String? = null
)