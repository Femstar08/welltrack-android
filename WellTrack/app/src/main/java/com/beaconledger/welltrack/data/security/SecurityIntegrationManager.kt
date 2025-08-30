package com.beaconledger.welltrack.data.security

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurityIntegrationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val biometricAuthManager: BiometricAuthManager,
    private val appLockManager: AppLockManager,
    private val privacyControlsManager: PrivacyControlsManager,
    private val secureDataDeletionManager: SecureDataDeletionManager,
    private val auditLogger: AuditLogger,
    private val securePreferencesManager: SecurePreferencesManager
) : DefaultLifecycleObserver {
    
    private val securityScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    companion object {
        private const val PREF_SECURITY_INITIALIZED = "security_initialized"
        private const val PREF_LAST_SECURITY_CHECK = "last_security_check"
        private const val SECURITY_CHECK_INTERVAL_HOURS = 24
    }
    
    data class SecurityStatus(
        val isAppLocked: Boolean = false,
        val biometricAvailable: Boolean = false,
        val biometricEnabled: Boolean = false,
        val privacyControlsActive: Boolean = false,
        val auditingEnabled: Boolean = true,
        val securityLevel: SecurityLevel = SecurityLevel.BASIC,
        val lastSecurityCheck: Long = 0L,
        val pendingSecurityActions: List<SecurityAction> = emptyList()
    )
    
    enum class SecurityLevel {
        BASIC,      // App lock only
        ENHANCED,   // App lock + biometric
        MAXIMUM     // All security features enabled
    }
    
    sealed class SecurityAction {
        object EnableAppLock : SecurityAction()
        object SetupBiometric : SecurityAction()
        object ReviewPrivacySettings : SecurityAction()
        object UpdateSecuritySettings : SecurityAction()
        data class SecurityAlert(val message: String, val severity: AlertSeverity) : SecurityAction()
    }
    
    enum class AlertSeverity {
        INFO, WARNING, CRITICAL
    }
    
    private val _securityStatus = MutableStateFlow(SecurityStatus())
    val securityStatus: StateFlow<SecurityStatus> = _securityStatus.asStateFlow()
    
    private val _securityAlerts = MutableSharedFlow<SecurityAction.SecurityAlert>()
    val securityAlerts: SharedFlow<SecurityAction.SecurityAlert> = _securityAlerts.asSharedFlow()
    
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        initializeSecurity()
        observeSecurityComponents()
    }
    
    private fun initializeSecurity() {
        securityScope.launch {
            try {
                // Check if this is the first time initializing security
                val isFirstTime = !securePreferencesManager.getBoolean(PREF_SECURITY_INITIALIZED, false)
                
                if (isFirstTime) {
                    performInitialSecuritySetup()
                    securePreferencesManager.putBoolean(PREF_SECURITY_INITIALIZED, true)
                }
                
                // Perform regular security check
                performSecurityCheck()
                updateSecurityStatus()
                
            } catch (e: Exception) {
                auditLogger.logSecuritySettingsChange(
                    userId = null,
                    action = "SECURITY_INITIALIZATION_FAILED",
                    settingType = "SYSTEM",
                    newValue = e.message
                )
            }
        }
    }
    
    private suspend fun performInitialSecuritySetup() {
        // Set default privacy settings (privacy-first approach)
        val defaultPrivacySettings = PrivacyControlsManager.PrivacySettings(
            dataSharingEnabled = false,
            analyticsEnabled = false,
            crashReportingEnabled = true, // Keep for app stability
            healthDataSharingEnabled = false,
            mealDataSharingEnabled = false,
            recipeSharingEnabled = false,
            socialFeaturesEnabled = false,
            locationSharingEnabled = false,
            thirdPartyIntegrationsEnabled = false,
            dataExportAllowed = true,
            marketingCommunicationsEnabled = false,
            personalizedAdsEnabled = false
        )
        
        privacyControlsManager.updatePrivacySettings(defaultPrivacySettings)
        
        // Log initial setup
        auditLogger.logSecuritySettingsChange(
            userId = null,
            action = "INITIAL_SECURITY_SETUP_COMPLETED",
            settingType = "SYSTEM",
            newValue = "Privacy-first defaults applied"
        )
    }
    
    private suspend fun performSecurityCheck() {
        val lastCheck = securePreferencesManager.getLong(PREF_LAST_SECURITY_CHECK, 0L)
        val currentTime = System.currentTimeMillis()
        val timeSinceLastCheck = currentTime - lastCheck
        val checkInterval = SECURITY_CHECK_INTERVAL_HOURS * 60 * 60 * 1000L
        
        if (timeSinceLastCheck > checkInterval) {
            // Perform comprehensive security check
            val securityIssues = mutableListOf<SecurityAction>()
            
            // Check if app lock is enabled
            if (!appLockManager.isAppLockEnabled()) {
                securityIssues.add(SecurityAction.EnableAppLock)
            }
            
            // Check if biometric is available but not enabled
            val biometricResult = biometricAuthManager.isBiometricAvailable()
            if (biometricResult == BiometricAuthManager.BiometricResult.Success && 
                !appLockManager.isBiometricEnabled()) {
                securityIssues.add(SecurityAction.SetupBiometric)
            }
            
            // Check privacy settings
            val privacySettings = privacyControlsManager.privacySettings.value
            if (privacySettings.dataSharingEnabled || 
                privacySettings.thirdPartyIntegrationsEnabled) {
                securityIssues.add(SecurityAction.ReviewPrivacySettings)
            }
            
            // Update security status with pending actions
            _securityStatus.update { it.copy(pendingSecurityActions = securityIssues) }
            
            // Update last check time
            securePreferencesManager.putLong(PREF_LAST_SECURITY_CHECK, currentTime)
            
            // Log security check
            auditLogger.logSecuritySettingsChange(
                userId = null,
                action = "SECURITY_CHECK_PERFORMED",
                settingType = "SYSTEM",
                newValue = "Found ${securityIssues.size} security recommendations"
            )
        }
    }
    
    private fun observeSecurityComponents() {
        securityScope.launch {
            // Observe app lock status
            appLockManager.isAppLocked.collect { isLocked ->
                updateSecurityStatus()
                if (isLocked) {
                    auditLogger.logAuthentication(
                        userId = null,
                        eventType = AuditLogger.EVENT_APP_LOCK,
                        success = true,
                        method = "TIMEOUT"
                    )
                }
            }
        }
        
        securityScope.launch {
            // Observe privacy settings changes
            privacyControlsManager.privacySettings.collect { settings ->
                updateSecurityStatus()
                
                // Alert if sensitive settings are enabled
                if (settings.thirdPartyIntegrationsEnabled) {
                    _securityAlerts.emit(
                        SecurityAction.SecurityAlert(
                            "Third-party integrations enabled. Review data sharing permissions.",
                            AlertSeverity.WARNING
                        )
                    )
                }
            }
        }
    }
    
    private fun updateSecurityStatus() {
        securityScope.launch {
            val biometricResult = biometricAuthManager.isBiometricAvailable()
            val biometricAvailable = biometricResult == BiometricAuthManager.BiometricResult.Success
            val biometricEnabled = appLockManager.isBiometricEnabled()
            val appLockEnabled = appLockManager.isAppLockEnabled()
            val privacySettings = privacyControlsManager.privacySettings.value
            
            // Determine security level
            val securityLevel = when {
                appLockEnabled && biometricEnabled && !privacySettings.dataSharingEnabled -> SecurityLevel.MAXIMUM
                appLockEnabled && biometricEnabled -> SecurityLevel.ENHANCED
                appLockEnabled -> SecurityLevel.ENHANCED
                else -> SecurityLevel.BASIC
            }
            
            _securityStatus.update { currentStatus ->
                currentStatus.copy(
                    isAppLocked = appLockManager.isAppLocked.value,
                    biometricAvailable = biometricAvailable,
                    biometricEnabled = biometricEnabled,
                    privacyControlsActive = !privacySettings.dataSharingEnabled,
                    securityLevel = securityLevel,
                    lastSecurityCheck = securePreferencesManager.getLong(PREF_LAST_SECURITY_CHECK, 0L)
                )
            }
        }
    }
    
    suspend fun authenticateUser(
        activity: FragmentActivity,
        reason: String = "Authentication required"
    ): AuthenticationResult {
        return try {
            // Check if app is locked
            if (!appLockManager.isAppLocked.value) {
                return AuthenticationResult.Success
            }
            
            // Try biometric first if enabled
            if (appLockManager.isBiometricEnabled() && biometricAuthManager.canUseBiometricAuthentication()) {
                val biometricResult = biometricAuthManager.authenticateWithBiometric(
                    activity = activity,
                    title = "Unlock WellTrack",
                    subtitle = reason
                )
                
                when (biometricResult) {
                    is BiometricAuthManager.BiometricResult.Success -> {
                        appLockManager.unlockApp()
                        auditLogger.logAuthentication(
                            userId = null,
                            eventType = AuditLogger.EVENT_BIOMETRIC_AUTH_SUCCESS,
                            success = true,
                            method = "BIOMETRIC"
                        )
                        return AuthenticationResult.Success
                    }
                    is BiometricAuthManager.BiometricResult.UserCancelled -> {
                        return AuthenticationResult.Cancelled
                    }
                    is BiometricAuthManager.BiometricResult.Error -> {
                        auditLogger.logAuthentication(
                            userId = null,
                            eventType = AuditLogger.EVENT_BIOMETRIC_AUTH_FAILURE,
                            success = false,
                            method = "BIOMETRIC",
                            failureReason = biometricResult.message
                        )
                        return AuthenticationResult.Failed(biometricResult.message)
                    }
                    else -> {
                        return AuthenticationResult.Failed("Biometric authentication not available")
                    }
                }
            } else {
                // Fallback to app unlock (this would typically show a PIN/password screen)
                appLockManager.unlockApp()
                auditLogger.logAuthentication(
                    userId = null,
                    eventType = AuditLogger.EVENT_APP_UNLOCK,
                    success = true,
                    method = "MANUAL"
                )
                return AuthenticationResult.Success
            }
        } catch (e: Exception) {
            auditLogger.logAuthentication(
                userId = null,
                eventType = AuditLogger.EVENT_LOGIN_FAILURE,
                success = false,
                method = "UNKNOWN",
                failureReason = e.message
            )
            AuthenticationResult.Failed("Authentication failed: ${e.message}")
        }
    }
    
    suspend fun performSecureDataOperation(
        userId: String,
        operation: String,
        dataType: String,
        action: suspend () -> Unit
    ) {
        try {
            // Log the start of sensitive operation
            auditLogger.logSensitiveDataAccess(
                userId = userId,
                dataType = dataType,
                action = "${operation}_STARTED"
            )
            
            // Perform the operation
            action()
            
            // Log successful completion
            auditLogger.logSensitiveDataAccess(
                userId = userId,
                dataType = dataType,
                action = "${operation}_COMPLETED"
            )
            
        } catch (e: Exception) {
            // Log failure
            auditLogger.logSensitiveDataAccess(
                userId = userId,
                dataType = dataType,
                action = "${operation}_FAILED"
            )
            throw e
        }
    }
    
    fun getSecurityRecommendations(): List<SecurityRecommendation> {
        val recommendations = mutableListOf<SecurityRecommendation>()
        val currentStatus = _securityStatus.value
        
        if (!appLockManager.isAppLockEnabled()) {
            recommendations.add(
                SecurityRecommendation(
                    title = "Enable App Lock",
                    description = "Protect your health data with app lock",
                    priority = RecommendationPriority.HIGH,
                    action = SecurityAction.EnableAppLock
                )
            )
        }
        
        if (currentStatus.biometricAvailable && !currentStatus.biometricEnabled) {
            recommendations.add(
                SecurityRecommendation(
                    title = "Setup Biometric Authentication",
                    description = "Use fingerprint or face unlock for quick access",
                    priority = RecommendationPriority.MEDIUM,
                    action = SecurityAction.SetupBiometric
                )
            )
        }
        
        val privacySettings = privacyControlsManager.privacySettings.value
        if (privacySettings.dataSharingEnabled || privacySettings.thirdPartyIntegrationsEnabled) {
            recommendations.add(
                SecurityRecommendation(
                    title = "Review Privacy Settings",
                    description = "Some data sharing features are enabled",
                    priority = RecommendationPriority.MEDIUM,
                    action = SecurityAction.ReviewPrivacySettings
                )
            )
        }
        
        return recommendations
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        securityScope.launch {
            performSecurityCheck()
            updateSecurityStatus()
        }
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App is going to background - security measures will be handled by AppLockManager
    }
    
    data class SecurityRecommendation(
        val title: String,
        val description: String,
        val priority: RecommendationPriority,
        val action: SecurityAction
    )
    
    enum class RecommendationPriority {
        LOW, MEDIUM, HIGH, CRITICAL
    }
    
    sealed class AuthenticationResult {
        object Success : AuthenticationResult()
        object Cancelled : AuthenticationResult()
        data class Failed(val message: String) : AuthenticationResult()
    }
}