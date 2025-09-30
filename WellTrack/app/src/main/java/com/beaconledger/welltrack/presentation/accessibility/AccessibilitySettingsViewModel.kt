package com.beaconledger.welltrack.presentation.accessibility

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for accessibility settings screen
 */
@HiltViewModel
class AccessibilitySettingsViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AccessibilitySettingsUiState())
    val uiState: StateFlow<AccessibilitySettingsUiState> = _uiState.asStateFlow()
    
    init {
        loadAccessibilitySettings()
    }
    
    private fun loadAccessibilitySettings() {
        viewModelScope.launch {
            // Load user preferences for accessibility settings
            // This would typically come from a repository
            _uiState.value = AccessibilitySettingsUiState(
                highContrastEnabled = false, // Load from preferences
                reduceAnimationsEnabled = false,
                largeTextEnabled = false,
                screenReaderOptimizationEnabled = true,
                audioDescriptionsEnabled = false,
                largeTouchTargetsEnabled = false,
                reduceMotionEnabled = false,
                simplifiedUIEnabled = false,
                extendedTimeoutsEnabled = false
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
        // Save to user preferences repository
        // This would typically save to local storage or sync with backend
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