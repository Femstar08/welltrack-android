package com.beaconledger.welltrack.data.security

import android.content.Context
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securePreferencesManager: SecurePreferencesManager
) : DefaultLifecycleObserver {
    
    companion object {
        private const val PREF_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val PREF_LOCK_TIMEOUT_MINUTES = "lock_timeout_minutes"
        private const val PREF_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val PREF_LAST_UNLOCK_TIME = "last_unlock_time"
        
        const val DEFAULT_TIMEOUT_MINUTES = 5
        const val MIN_TIMEOUT_MINUTES = 1
        const val MAX_TIMEOUT_MINUTES = 60
    }
    
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()
    
    private val _isUnlockRequired = MutableStateFlow(false)
    val isUnlockRequired: StateFlow<Boolean> = _isUnlockRequired.asStateFlow()
    
    private var lastUnlockTime: Long = 0L
    
    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        loadSettings()
    }
    
    private fun loadSettings() {
        lastUnlockTime = securePreferencesManager.getLong(PREF_LAST_UNLOCK_TIME, 0L)
        checkLockStatus()
    }
    
    fun isAppLockEnabled(): Boolean {
        return securePreferencesManager.getBoolean(PREF_APP_LOCK_ENABLED, false)
    }
    
    fun setAppLockEnabled(enabled: Boolean) {
        securePreferencesManager.putBoolean(PREF_APP_LOCK_ENABLED, enabled)
        if (!enabled) {
            unlockApp()
        } else {
            checkLockStatus()
        }
    }
    
    fun isBiometricEnabled(): Boolean {
        return securePreferencesManager.getBoolean(PREF_BIOMETRIC_ENABLED, false)
    }
    
    fun setBiometricEnabled(enabled: Boolean) {
        securePreferencesManager.putBoolean(PREF_BIOMETRIC_ENABLED, enabled)
    }
    
    fun getLockTimeoutMinutes(): Int {
        return securePreferencesManager.getInt(PREF_LOCK_TIMEOUT_MINUTES, DEFAULT_TIMEOUT_MINUTES)
    }
    
    fun setLockTimeoutMinutes(minutes: Int) {
        val validMinutes = minutes.coerceIn(MIN_TIMEOUT_MINUTES, MAX_TIMEOUT_MINUTES)
        securePreferencesManager.putInt(PREF_LOCK_TIMEOUT_MINUTES, validMinutes)
    }
    
    fun unlockApp() {
        lastUnlockTime = System.currentTimeMillis()
        securePreferencesManager.putLong(PREF_LAST_UNLOCK_TIME, lastUnlockTime)
        _isAppLocked.value = false
        _isUnlockRequired.value = false
    }
    
    fun lockApp() {
        _isAppLocked.value = true
        _isUnlockRequired.value = true
    }
    
    private fun checkLockStatus() {
        if (!isAppLockEnabled()) {
            _isAppLocked.value = false
            _isUnlockRequired.value = false
            return
        }
        
        val currentTime = System.currentTimeMillis()
        val timeoutMillis = getLockTimeoutMinutes() * 60 * 1000L
        val timeSinceLastUnlock = currentTime - lastUnlockTime
        
        val shouldLock = timeSinceLastUnlock > timeoutMillis
        _isAppLocked.value = shouldLock
        _isUnlockRequired.value = shouldLock
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        checkLockStatus()
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        // App is going to background, start the timeout timer
        if (isAppLockEnabled()) {
            // The lock status will be checked when the app comes back to foreground
        }
    }
    
    fun getTimeUntilLock(): Long {
        if (!isAppLockEnabled()) return Long.MAX_VALUE
        
        val currentTime = System.currentTimeMillis()
        val timeoutMillis = getLockTimeoutMinutes() * 60 * 1000L
        val timeSinceLastUnlock = currentTime - lastUnlockTime
        
        return (timeoutMillis - timeSinceLastUnlock).coerceAtLeast(0L)
    }
    
    fun extendSession() {
        if (isAppLockEnabled() && !_isAppLocked.value) {
            unlockApp() // This updates the last unlock time
        }
    }
}