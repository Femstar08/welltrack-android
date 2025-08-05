package com.beaconledger.welltrack.data.profile

import android.content.SharedPreferences
import com.beaconledger.welltrack.data.model.ActiveProfileSession
import com.beaconledger.welltrack.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileSessionManager @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    
    private val _activeProfile = MutableStateFlow<UserProfile?>(null)
    val activeProfile: StateFlow<UserProfile?> = _activeProfile.asStateFlow()
    
    private val _allProfiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val allProfiles: StateFlow<List<UserProfile>> = _allProfiles.asStateFlow()

    companion object {
        private const val PREF_ACTIVE_PROFILE_ID = "active_profile_id"
        private const val PREF_ACTIVE_USER_ID = "active_user_id"
        private const val PREF_SESSION_START_TIME = "session_start_time"
        private const val PREF_LAST_ACTIVITY_TIME = "last_activity_time"
        private const val PREF_PROFILE_SWITCH_COUNT = "profile_switch_count"
    }

    fun setActiveProfile(profile: UserProfile) {
        val currentTime = LocalDateTime.now().toString()
        
        // Save to SharedPreferences
        sharedPreferences.edit().apply {
            putString(PREF_ACTIVE_PROFILE_ID, profile.userId)
            putString(PREF_ACTIVE_USER_ID, profile.userId)
            putString(PREF_SESSION_START_TIME, currentTime)
            putString(PREF_LAST_ACTIVITY_TIME, currentTime)
            apply()
        }
        
        // Update state
        _activeProfile.value = profile
        
        // Increment switch count
        incrementSwitchCount()
    }

    fun updateAllProfiles(profiles: List<UserProfile>) {
        _allProfiles.value = profiles
        
        // If no active profile is set, set the first one as active
        if (_activeProfile.value == null && profiles.isNotEmpty()) {
            setActiveProfile(profiles.first())
        }
    }

    fun getActiveProfileId(): String? {
        return sharedPreferences.getString(PREF_ACTIVE_PROFILE_ID, null)
    }

    fun getActiveSession(): ActiveProfileSession? {
        val profileId = sharedPreferences.getString(PREF_ACTIVE_PROFILE_ID, null)
        val userId = sharedPreferences.getString(PREF_ACTIVE_USER_ID, null)
        val sessionStart = sharedPreferences.getString(PREF_SESSION_START_TIME, null)
        val lastActivity = sharedPreferences.getString(PREF_LAST_ACTIVITY_TIME, null)
        
        return if (profileId != null && userId != null && sessionStart != null && lastActivity != null) {
            ActiveProfileSession(
                userId = userId,
                profileId = profileId,
                sessionStartTime = sessionStart,
                lastActivityTime = lastActivity
            )
        } else {
            null
        }
    }

    fun updateLastActivity() {
        val currentTime = LocalDateTime.now().toString()
        sharedPreferences.edit()
            .putString(PREF_LAST_ACTIVITY_TIME, currentTime)
            .apply()
    }

    fun switchToProfile(profileId: String): Boolean {
        val profile = _allProfiles.value.find { it.userId == profileId }
        return if (profile != null) {
            setActiveProfile(profile)
            true
        } else {
            false
        }
    }

    fun clearActiveProfile() {
        sharedPreferences.edit().apply {
            remove(PREF_ACTIVE_PROFILE_ID)
            remove(PREF_ACTIVE_USER_ID)
            remove(PREF_SESSION_START_TIME)
            remove(PREF_LAST_ACTIVITY_TIME)
            apply()
        }
        
        _activeProfile.value = null
    }

    private fun incrementSwitchCount() {
        val currentCount = sharedPreferences.getInt(PREF_PROFILE_SWITCH_COUNT, 0)
        sharedPreferences.edit()
            .putInt(PREF_PROFILE_SWITCH_COUNT, currentCount + 1)
            .apply()
    }

    fun getProfileSwitchCount(): Int {
        return sharedPreferences.getInt(PREF_PROFILE_SWITCH_COUNT, 0)
    }

    fun hasMultipleProfiles(): Boolean {
        return _allProfiles.value.size > 1
    }

    fun getProfileById(profileId: String): UserProfile? {
        return _allProfiles.value.find { it.userId == profileId }
    }
}