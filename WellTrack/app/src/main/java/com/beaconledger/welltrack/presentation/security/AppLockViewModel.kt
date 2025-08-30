package com.beaconledger.welltrack.presentation.security

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.security.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val securityIntegrationManager: SecurityIntegrationManager,
    private val biometricAuthManager: BiometricAuthManager,
    private val appLockManager: AppLockManager,
    private val auditLogger: AuditLogger,
    private val securePreferencesManager: SecurePreferencesManager
) : ViewModel() {
    
    companion object {
        private const val PREF_USER_PIN = "user_pin_hash"
        private const val MAX_PIN_ATTEMPTS = 5
        private const val PREF_PIN_ATTEMPTS = "pin_attempts"
        private const val PREF_LAST_FAILED_ATTEMPT = "last_failed_attempt"
        private const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }
    
    data class AppLockUiState(
        val isUnlocked: Boolean = false,
        val biometricAvailable: Boolean = false,
        val showPinEntry: Boolean = false,
        val isAuthenticating: Boolean = false,
        val authenticationMethod: String? = null,
        val errorMessage: String? = null,
        val remainingAttempts: Int = MAX_PIN_ATTEMPTS,
        val isLockedOut: Boolean = false,
        val lockoutTimeRemaining: Long = 0L
    )
    
    private val _uiState = MutableStateFlow(AppLockUiState())
    val uiState: StateFlow<AppLockUiState> = _uiState.asStateFlow()
    
    init {
        observeAppLockState()
        checkLockoutStatus()
    }
    
    private fun observeAppLockState() {
        viewModelScope.launch {
            appLockManager.isAppLocked.collect { isLocked ->
                _uiState.update { it.copy(isUnlocked = !isLocked) }
            }
        }
    }
    
    private fun checkLockoutStatus() {
        viewModelScope.launch {
            val attempts = securePreferencesManager.getInt(PREF_PIN_ATTEMPTS, 0)
            val lastFailedAttempt = securePreferencesManager.getLong(PREF_LAST_FAILED_ATTEMPT, 0L)
            val currentTime = System.currentTimeMillis()
            
            if (attempts >= MAX_PIN_ATTEMPTS) {
                val timeSinceLastAttempt = currentTime - lastFailedAttempt
                if (timeSinceLastAttempt < LOCKOUT_DURATION_MS) {
                    val remainingTime = LOCKOUT_DURATION_MS - timeSinceLastAttempt
                    _uiState.update { 
                        it.copy(
                            isLockedOut = true,
                            lockoutTimeRemaining = remainingTime,
                            remainingAttempts = 0
                        )
                    }
                    
                    // Start countdown timer
                    startLockoutCountdown(remainingTime)
                } else {
                    // Lockout period expired, reset attempts
                    resetPinAttempts()
                }
            } else {
                _uiState.update { it.copy(remainingAttempts = MAX_PIN_ATTEMPTS - attempts) }
            }
        }
    }
    
    private fun startLockoutCountdown(initialTime: Long) {
        viewModelScope.launch {
            var remainingTime = initialTime
            while (remainingTime > 0) {
                _uiState.update { it.copy(lockoutTimeRemaining = remainingTime) }
                kotlinx.coroutines.delay(1000L)
                remainingTime -= 1000L
            }
            
            // Lockout expired
            resetPinAttempts()
            _uiState.update { 
                it.copy(
                    isLockedOut = false,
                    lockoutTimeRemaining = 0L,
                    remainingAttempts = MAX_PIN_ATTEMPTS
                )
            }
        }
    }
    
    fun checkAuthenticationMethods() {
        viewModelScope.launch {
            val biometricResult = biometricAuthManager.isBiometricAvailable()
            val biometricAvailable = biometricResult == BiometricAuthManager.BiometricResult.Success &&
                    appLockManager.isBiometricEnabled()
            
            _uiState.update { it.copy(biometricAvailable = biometricAvailable) }
            
            // Auto-trigger biometric if available and enabled
            if (biometricAvailable && !_uiState.value.isLockedOut) {
                // Small delay to ensure UI is ready
                kotlinx.coroutines.delay(500L)
                // Note: Auto-biometric would need FragmentActivity context
                // This would typically be handled by the calling screen
            }
        }
    }
    
    fun authenticateWithBiometric(activity: FragmentActivity) {
        if (_uiState.value.isLockedOut) {
            showError("Account temporarily locked. Please wait.")
            return
        }
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isAuthenticating = true, 
                    authenticationMethod = "biometric",
                    errorMessage = null
                )
            }
            
            try {
                val result = securityIntegrationManager.authenticateUser(
                    activity = activity,
                    reason = "Unlock WellTrack to access your health data"
                )
                
                when (result) {
                    is SecurityIntegrationManager.AuthenticationResult.Success -> {
                        _uiState.update { 
                            it.copy(
                                isUnlocked = true,
                                isAuthenticating = false,
                                authenticationMethod = null,
                                errorMessage = null
                            )
                        }
                        resetPinAttempts()
                    }
                    is SecurityIntegrationManager.AuthenticationResult.Cancelled -> {
                        _uiState.update { 
                            it.copy(
                                isAuthenticating = false,
                                authenticationMethod = null
                            )
                        }
                    }
                    is SecurityIntegrationManager.AuthenticationResult.Failed -> {
                        _uiState.update { 
                            it.copy(
                                isAuthenticating = false,
                                authenticationMethod = null,
                                errorMessage = result.message
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false,
                        authenticationMethod = null,
                        errorMessage = "Authentication failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun authenticateWithPin(pin: String) {
        if (_uiState.value.isLockedOut) {
            showError("Account temporarily locked. Please wait.")
            return
        }
        
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isAuthenticating = true,
                    authenticationMethod = "pin",
                    errorMessage = null
                )
            }
            
            try {
                val storedPinHash = securePreferencesManager.getString(PREF_USER_PIN)
                
                if (storedPinHash == null) {
                    // First time setup - store the PIN
                    val pinHash = hashPin(pin)
                    securePreferencesManager.putString(PREF_USER_PIN, pinHash)
                    
                    appLockManager.unlockApp()
                    _uiState.update { 
                        it.copy(
                            isUnlocked = true,
                            isAuthenticating = false,
                            authenticationMethod = null,
                            showPinEntry = false
                        )
                    }
                    
                    auditLogger.logAuthentication(
                        userId = null,
                        eventType = AuditLogger.EVENT_LOGIN_SUCCESS,
                        success = true,
                        method = "PIN_SETUP"
                    )
                } else {
                    // Verify PIN
                    val pinHash = hashPin(pin)
                    if (pinHash == storedPinHash) {
                        // PIN correct
                        appLockManager.unlockApp()
                        _uiState.update { 
                            it.copy(
                                isUnlocked = true,
                                isAuthenticating = false,
                                authenticationMethod = null,
                                showPinEntry = false
                            )
                        }
                        resetPinAttempts()
                        
                        auditLogger.logAuthentication(
                            userId = null,
                            eventType = AuditLogger.EVENT_LOGIN_SUCCESS,
                            success = true,
                            method = "PIN"
                        )
                    } else {
                        // PIN incorrect
                        handleFailedPinAttempt()
                        
                        auditLogger.logAuthentication(
                            userId = null,
                            eventType = AuditLogger.EVENT_LOGIN_FAILURE,
                            success = false,
                            method = "PIN",
                            failureReason = "Incorrect PIN"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isAuthenticating = false,
                        authenticationMethod = null,
                        errorMessage = "PIN verification failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun handleFailedPinAttempt() {
        val currentAttempts = securePreferencesManager.getInt(PREF_PIN_ATTEMPTS, 0) + 1
        securePreferencesManager.putInt(PREF_PIN_ATTEMPTS, currentAttempts)
        securePreferencesManager.putLong(PREF_LAST_FAILED_ATTEMPT, System.currentTimeMillis())
        
        val remainingAttempts = MAX_PIN_ATTEMPTS - currentAttempts
        
        if (currentAttempts >= MAX_PIN_ATTEMPTS) {
            // Lock out user
            _uiState.update { 
                it.copy(
                    isAuthenticating = false,
                    authenticationMethod = null,
                    errorMessage = "Too many failed attempts. Account locked for 5 minutes.",
                    isLockedOut = true,
                    remainingAttempts = 0,
                    showPinEntry = false
                )
            }
            startLockoutCountdown(LOCKOUT_DURATION_MS)
        } else {
            _uiState.update { 
                it.copy(
                    isAuthenticating = false,
                    authenticationMethod = null,
                    errorMessage = "Incorrect PIN. $remainingAttempts attempts remaining.",
                    remainingAttempts = remainingAttempts
                )
            }
        }
    }
    
    private fun resetPinAttempts() {
        securePreferencesManager.putInt(PREF_PIN_ATTEMPTS, 0)
        securePreferencesManager.putLong(PREF_LAST_FAILED_ATTEMPT, 0L)
    }
    
    private fun hashPin(pin: String): String {
        // Simple hash for demo - in production, use proper password hashing like bcrypt
        return pin.hashCode().toString()
    }
    
    fun showPinEntry() {
        if (!_uiState.value.isLockedOut) {
            _uiState.update { it.copy(showPinEntry = true) }
        }
    }
    
    fun hidePinEntry() {
        _uiState.update { 
            it.copy(
                showPinEntry = false,
                errorMessage = null,
                isAuthenticating = false,
                authenticationMethod = null
            )
        }
    }
    
    private fun showError(message: String) {
        _uiState.update { it.copy(errorMessage = message) }
    }
    
    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}