package com.beaconledger.welltrack.data.profile

import com.beaconledger.welltrack.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages user context throughout the app to ensure all data operations
 * are scoped to the currently active profile
 */
@Singleton
class UserContextManager @Inject constructor(
    private val profileSessionManager: ProfileSessionManager
) {
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()
    
    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile.asStateFlow()
    
    init {
        // Observe active profile changes
        profileSessionManager.activeProfile.value?.let { profile ->
            setCurrentContext(profile)
        }
    }
    
    /**
     * Sets the current user context based on the active profile
     */
    fun setCurrentContext(profile: UserProfile) {
        _currentUserId.value = profile.userId
        _currentProfile.value = profile
        profileSessionManager.updateLastActivity()
    }
    
    /**
     * Gets the current user ID, throwing an exception if no user is active
     */
    fun requireCurrentUserId(): String {
        return _currentUserId.value 
            ?: throw IllegalStateException("No active user profile. User must be logged in and have an active profile.")
    }
    
    /**
     * Gets the current profile, throwing an exception if no profile is active
     */
    fun requireCurrentProfile(): UserProfile {
        return _currentProfile.value 
            ?: throw IllegalStateException("No active user profile. User must be logged in and have an active profile.")
    }
    
    /**
     * Checks if there's an active user context
     */
    fun hasActiveContext(): Boolean {
        return _currentUserId.value != null && _currentProfile.value != null
    }
    
    /**
     * Clears the current user context (e.g., on logout)
     */
    fun clearContext() {
        _currentUserId.value = null
        _currentProfile.value = null
        profileSessionManager.clearActiveProfile()
    }
    
    /**
     * Switches to a different profile context
     */
    fun switchContext(profileId: String): Boolean {
        val success = profileSessionManager.switchToProfile(profileId)
        if (success) {
            profileSessionManager.activeProfile.value?.let { profile ->
                setCurrentContext(profile)
            }
        }
        return success
    }
    
    /**
     * Gets the display name for the current profile
     */
    fun getCurrentDisplayName(): String {
        return _currentProfile.value?.name ?: "Unknown User"
    }
    
    /**
     * Checks if the current user has multiple profiles
     */
    fun hasMultipleProfiles(): Boolean {
        return profileSessionManager.hasMultipleProfiles()
    }
}