package com.beaconledger.welltrack.presentation.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for accessibility settings screen
 */
@HiltViewModel
class AccessibilitySettingsViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccessibilitySettingsUiState())
    val uiState: StateFlow<AccessibilitySettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadAccessibilitySettings()
    }
    
    private fun loadAccessibilitySettings() {
        viewModelScope.launch {
            // Load user preferences for accessibility settings
            userRepository.getUserPreferences("current_user_id").fold(
                onSuccess = { preferences ->
                    val accessibility = preferences.accessibilitySettings
                    _uiState.value = AccessibilitySettingsUiState(
                        highContrastEnabled = accessibility.highContrastEnabled,
                        reduceAnimationsEnabled = accessibility.reduceAnimationsEnabled,
                        largeTextEnabled = accessibility.largeTextEnabled,
                        screenReaderOptimizationEnabled = accessibility.screenReaderOptimizationEnabled,
                        audioDescriptionsEnabled = accessibility.audioDescriptionsEnabled,
                        largeTouchTargetsEnabled = accessibility.largeTouchTargetsEnabled,
                        reduceMotionEnabled = accessibility.reduceMotionEnabled,
                        simplifiedUIEnabled = accessibility.simplifiedUIEnabled,
                        extendedTimeoutsEnabled = accessibility.extendedTimeoutsEnabled
                    )
                },
                onFailure = { error ->
                    // Handle error - use default settings
                    _uiState.value = AccessibilitySettingsUiState(
                        error = error.message
                    )
                }
            )
        }
    }
    
    fun toggleHighContrast() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                highContrastEnabled = !_uiState.value.highContrastEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleReduceAnimations() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                reduceAnimationsEnabled = !_uiState.value.reduceAnimationsEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleLargeText() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                largeTextEnabled = !_uiState.value.largeTextEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleScreenReaderOptimization() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                screenReaderOptimizationEnabled = !_uiState.value.screenReaderOptimizationEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleAudioDescriptions() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                audioDescriptionsEnabled = !_uiState.value.audioDescriptionsEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleLargeTouchTargets() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                largeTouchTargetsEnabled = !_uiState.value.largeTouchTargetsEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleReduceMotion() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                reduceMotionEnabled = !_uiState.value.reduceMotionEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleSimplifiedUI() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                simplifiedUIEnabled = !_uiState.value.simplifiedUIEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    fun toggleExtendedTimeouts() {
        viewModelScope.launch {
            val newState = _uiState.value.copy(
                extendedTimeoutsEnabled = !_uiState.value.extendedTimeoutsEnabled
            )
            _uiState.value = newState
            saveAccessibilitySettings(newState)
        }
    }
    
    private suspend fun saveAccessibilitySettings(settings: AccessibilitySettingsUiState) {
        // Get current preferences and update accessibility settings
        userRepository.getUserPreferences("current_user_id").fold(
            onSuccess = { currentPreferences ->
                val newAccessibilitySettings = com.beaconledger.welltrack.data.model.AccessibilitySettings(
                    highContrastEnabled = settings.highContrastEnabled,
                    reduceAnimationsEnabled = settings.reduceAnimationsEnabled,
                    largeTextEnabled = settings.largeTextEnabled,
                    screenReaderOptimizationEnabled = settings.screenReaderOptimizationEnabled,
                    audioDescriptionsEnabled = settings.audioDescriptionsEnabled,
                    largeTouchTargetsEnabled = settings.largeTouchTargetsEnabled,
                    reduceMotionEnabled = settings.reduceMotionEnabled,
                    simplifiedUIEnabled = settings.simplifiedUIEnabled,
                    extendedTimeoutsEnabled = settings.extendedTimeoutsEnabled
                )

                val updatedPreferences = currentPreferences.copy(
                    accessibilitySettings = newAccessibilitySettings
                )

                userRepository.updateUserPreferences("current_user_id", updatedPreferences)
            },
            onFailure = { error ->
                // Handle error
                _uiState.value = _uiState.value.copy(error = error.message)
            }
        )
    }
}

/**
 * UI state for accessibility settings
 */
data class AccessibilitySettingsUiState(
    val highContrastEnabled: Boolean = false,
    val reduceAnimationsEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val screenReaderOptimizationEnabled: Boolean = true,
    val audioDescriptionsEnabled: Boolean = false,
    val largeTouchTargetsEnabled: Boolean = false,
    val reduceMotionEnabled: Boolean = false,
    val simplifiedUIEnabled: Boolean = false,
    val extendedTimeoutsEnabled: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)