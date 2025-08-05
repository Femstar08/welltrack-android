package com.beaconledger.welltrack.data.auth

import android.content.Context
import android.content.SharedPreferences
import com.beaconledger.welltrack.data.model.AuthSession
import com.beaconledger.welltrack.data.model.AuthUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    
    private val _isSessionValid = MutableStateFlow(false)
    val isSessionValid: StateFlow<Boolean> = _isSessionValid.asStateFlow()
    
    private val _sessionExpiryTime = MutableStateFlow(0L)
    val sessionExpiryTime: StateFlow<Long> = _sessionExpiryTime.asStateFlow()

    companion object {
        private const val PREF_SESSION_VALID = "session_valid"
        private const val PREF_LAST_ACTIVITY = "last_activity"
        private const val SESSION_TIMEOUT = 30 * 60 * 1000L // 30 minutes in milliseconds
    }

    init {
        checkSessionValidity()
    }

    fun updateLastActivity() {
        val currentTime = System.currentTimeMillis()
        sharedPreferences.edit()
            .putLong(PREF_LAST_ACTIVITY, currentTime)
            .apply()
        
        _sessionExpiryTime.value = currentTime + SESSION_TIMEOUT
        _isSessionValid.value = true
    }

    fun checkSessionValidity(): Boolean {
        val lastActivity = sharedPreferences.getLong(PREF_LAST_ACTIVITY, 0)
        val currentTime = System.currentTimeMillis()
        
        val isValid = if (lastActivity == 0L) {
            false
        } else {
            (currentTime - lastActivity) < SESSION_TIMEOUT
        }
        
        _isSessionValid.value = isValid
        
        if (isValid) {
            _sessionExpiryTime.value = lastActivity + SESSION_TIMEOUT
        }
        
        return isValid
    }

    fun invalidateSession() {
        sharedPreferences.edit()
            .remove(PREF_LAST_ACTIVITY)
            .putBoolean(PREF_SESSION_VALID, false)
            .apply()
        
        _isSessionValid.value = false
        _sessionExpiryTime.value = 0L
    }

    fun getRemainingSessionTime(): Long {
        val lastActivity = sharedPreferences.getLong(PREF_LAST_ACTIVITY, 0)
        if (lastActivity == 0L) return 0L
        
        val currentTime = System.currentTimeMillis()
        val elapsed = currentTime - lastActivity
        val remaining = SESSION_TIMEOUT - elapsed
        
        return if (remaining > 0) remaining else 0L
    }

    fun extendSession() {
        updateLastActivity()
    }
}