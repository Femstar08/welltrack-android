package com.beaconledger.welltrack.presentation.security

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.security.*
import com.beaconledger.welltrack.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager,
    private val appLockManager: AppLockManager,
    private val privacyControlsManager: PrivacyControlsManager,
    private val secureDataDeletionManager: SecureDataDeletionManager,
    private val auditLogger: AuditLogger,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    data class SecuritySettingsUiState(
        val isLoading: Boolean = false,
        val appLockEnabled: Boolean = false,
        val lockTimeoutMinutes: Int = 5,
        val biometricAvailable: Boolean = false,
        val biometricEnabled: Boolean = false,
        val privacySettings: PrivacyControlsManager.PrivacySettings = PrivacyControlsManager.PrivacySettings(),
        val recentAuditLogsCount: Int = 0,
        val showAuditLogs: Boolean = false,
        val message: String? = null,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(SecuritySettingsUiState())
    val uiState: StateFlow<SecuritySettingsUiState> = _uiState.asStateFlow()
    
    private val currentUserId: String?
        get() = authRepository.getCurrentUserId()
    
    init {
        observePrivacySettings()
        observeAppLockSettings()
    }
    
    private fun observePrivacySettings() {
        viewModelScope.launch {
            privacyControlsManager.privacySettings.collect { settings ->
                _uiState.update { it.copy(privacySettings = settings) }
            }
        }
    }
    
    private fun observeAppLockSettings() {
        viewModelScope.launch {
            appLockManager.isAppLocked.collect { isLocked ->
                // Update UI based on lock state if needed
            }
        }
    }
    
    fun loadSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                val biometricResult = biometricAuthManager.isBiometricAvailable()
                val biometricAvailable = biometricResult == BiometricAuthManager.BiometricResult.Success
                
                val appLockEnabled = appLockManager.isAppLockEnabled()
                val lockTimeout = appLockManager.getLockTimeoutMinutes()
                val biometricEnabled = appLockManager.isBiometricEnabled()
                
                // Load recent audit logs count
                val recentLogsCount = currentUserId?.let { userId ->
                    auditLogger.getAuditLogsForUser(userId, limit = 10).size
                } ?: 0
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        appLockEnabled = appLockEnabled,
                        lockTimeoutMinutes = lockTimeout,
                        biometricAvailable = biometricAvailable,
                        biometricEnabled = biometricEnabled,
                        recentAuditLogsCount = recentLogsCount,
                        showAuditLogs = true
                    )
                }
                
                currentUserId?.let { userId ->
                    auditLogger.logSecuritySettingsChange(
                        userId = userId,
                        action = "SECURITY_SETTINGS_VIEWED",
                        settingType = "ALL"
                    )
                }
                
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load security settings: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun toggleAppLock(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appLockManager.setAppLockEnabled(enabled)
                _uiState.update { it.copy(appLockEnabled = enabled) }
                
                currentUserId?.let { userId ->
                    auditLogger.logSecuritySettingsChange(
                        userId = userId,
                        action = if (enabled) "APP_LOCK_ENABLED" else "APP_LOCK_DISABLED",
                        settingType = "APP_LOCK",
                        newValue = enabled.toString()
                    )
                }
                
                showMessage(if (enabled) "App lock enabled" else "App lock disabled")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update app lock: ${e.message}") }
            }
        }
    }
    
    fun setLockTimeout(minutes: Int) {
        viewModelScope.launch {
            try {
                val oldTimeout = appLockManager.getLockTimeoutMinutes()
                appLockManager.setLockTimeoutMinutes(minutes)
                _uiState.update { it.copy(lockTimeoutMinutes = minutes) }
                
                currentUserId?.let { userId ->
                    auditLogger.logSecuritySettingsChange(
                        userId = userId,
                        action = "LOCK_TIMEOUT_CHANGED",
                        settingType = "LOCK_TIMEOUT",
                        oldValue = oldTimeout.toString(),
                        newValue = minutes.toString()
                    )
                }
                
                showMessage("Lock timeout updated to $minutes minutes")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update lock timeout: ${e.message}") }
            }
        }
    }
    
    fun setupBiometric(activity: FragmentActivity) {
        viewModelScope.launch {
            try {
                val result = biometricAuthManager.authenticateWithBiometric(
                    activity = activity,
                    title = "Enable Biometric Authentication",
                    subtitle = "Authenticate to enable biometric unlock"
                )
                
                when (result) {
                    is BiometricAuthManager.BiometricResult.Success -> {
                        setBiometricEnabled(true)
                        showMessage("Biometric authentication enabled")
                    }
                    is BiometricAuthManager.BiometricResult.UserCancelled -> {
                        showMessage("Biometric setup cancelled")
                    }
                    is BiometricAuthManager.BiometricResult.Error -> {
                        _uiState.update { it.copy(error = "Biometric setup failed: ${result.message}") }
                    }
                    else -> {
                        _uiState.update { it.copy(error = "Biometric authentication not available") }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to setup biometric: ${e.message}") }
            }
        }
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appLockManager.setBiometricEnabled(enabled)
                _uiState.update { it.copy(biometricEnabled = enabled) }
                
                currentUserId?.let { userId ->
                    auditLogger.logSecuritySettingsChange(
                        userId = userId,
                        action = if (enabled) "BIOMETRIC_ENABLED" else "BIOMETRIC_DISABLED",
                        settingType = "BIOMETRIC_AUTH",
                        newValue = enabled.toString()
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update biometric setting: ${e.message}") }
            }
        }
    }
    
    fun updatePrivacySettings(settings: PrivacyControlsManager.PrivacySettings) {
        viewModelScope.launch {
            try {
                privacyControlsManager.updatePrivacySettings(settings, currentUserId)
                showMessage("Privacy settings updated")
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to update privacy settings: ${e.message}") }
            }
        }
    }
    
    fun exportUserData() {
        viewModelScope.launch {
            try {
                if (!privacyControlsManager.canExportData()) {
                    _uiState.update { it.copy(error = "Data export is disabled in privacy settings") }
                    return@launch
                }
                
                currentUserId?.let { userId ->
                    // Export user data (implementation would depend on requirements)
                    auditLogger.logSensitiveDataAccess(
                        userId = userId,
                        dataType = "ALL_USER_DATA",
                        action = "EXPORT_REQUESTED",
                        justification = "User requested data export"
                    )
                    
                    showMessage("Data export initiated. You will receive an email when ready.")
                } ?: run {
                    _uiState.update { it.copy(error = "No user logged in") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to export data: ${e.message}") }
            }
        }
    }
    
    fun deleteUserData(dataType: SecureDataDeletionManager.DataType?) {
        viewModelScope.launch {
            try {
                currentUserId?.let { userId ->
                    val result = if (dataType != null) {
                        secureDataDeletionManager.deleteSpecificDataType(userId, dataType)
                    } else {
                        secureDataDeletionManager.deleteAllUserData(userId, includeCloudData = true)
                    }
                    
                    when (result) {
                        is SecureDataDeletionManager.DeletionResult.Success -> {
                            showMessage("Data deleted successfully")
                            if (dataType == null) {
                                // Full account deletion - logout user
                                authRepository.signOut()
                            }
                        }
                        is SecureDataDeletionManager.DeletionResult.PartialSuccess -> {
                            showMessage("Data partially deleted. Some operations failed: ${result.failedOperations.joinToString(", ")}")
                        }
                        is SecureDataDeletionManager.DeletionResult.Error -> {
                            _uiState.update { it.copy(error = "Data deletion failed: ${result.message}") }
                        }
                    }
                } ?: run {
                    _uiState.update { it.copy(error = "No user logged in") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to delete data: ${e.message}") }
            }
        }
    }
    
    fun viewAuditLogs() {
        viewModelScope.launch {
            currentUserId?.let { userId ->
                auditLogger.logSensitiveDataAccess(
                    userId = userId,
                    dataType = "AUDIT_LOGS",
                    action = "VIEW_REQUESTED",
                    justification = "User requested to view audit logs"
                )
                
                // Navigate to audit logs screen (implementation depends on navigation setup)
                showMessage("Audit logs accessed")
            }
        }
    }
    
    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }
    
    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}