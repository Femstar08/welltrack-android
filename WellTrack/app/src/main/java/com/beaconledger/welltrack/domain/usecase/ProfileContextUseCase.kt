package com.beaconledger.welltrack.domain.usecase

import com.beaconledger.welltrack.data.model.UserProfile
import com.beaconledger.welltrack.data.profile.UserContextManager
import com.beaconledger.welltrack.data.repository.ProfileDataRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for managing profile context throughout the application
 */
@Singleton
class ProfileContextUseCase @Inject constructor(
    private val userContextManager: UserContextManager,
    private val profileDataRepository: ProfileDataRepository
) {
    
    /**
     * Gets the current active profile
     */
    val currentProfile: StateFlow<UserProfile?> = userContextManager.currentProfile
    
    /**
     * Gets the current user ID
     */
    val currentUserId: StateFlow<String?> = userContextManager.currentUserId
    
    /**
     * Switches to a different profile context
     */
    suspend fun switchToProfile(profileId: String): Result<Unit> {
        return try {
            val success = userContextManager.switchContext(profileId)
            if (success) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to switch to profile: $profileId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sets the current profile context
     */
    fun setCurrentProfile(profile: UserProfile) {
        userContextManager.setCurrentContext(profile)
    }
    
    /**
     * Checks if there's an active profile context
     */
    fun hasActiveContext(): Boolean {
        return userContextManager.hasActiveContext()
    }
    
    /**
     * Gets the current profile, throwing an exception if none is active
     */
    fun requireCurrentProfile(): UserProfile {
        return userContextManager.requireCurrentProfile()
    }
    
    /**
     * Gets the current user ID, throwing an exception if none is active
     */
    fun requireCurrentUserId(): String {
        return userContextManager.requireCurrentUserId()
    }
    
    /**
     * Gets the display name for the current profile
     */
    fun getCurrentDisplayName(): String {
        return userContextManager.getCurrentDisplayName()
    }
    
    /**
     * Checks if the current user has multiple profiles
     */
    fun hasMultipleProfiles(): Boolean {
        return userContextManager.hasMultipleProfiles()
    }
    
    /**
     * Clears the current profile context (e.g., on logout)
     */
    fun clearContext() {
        userContextManager.clearContext()
    }
    
    /**
     * Validates that the current context is valid
     */
    fun validateContext(): Boolean {
        return profileDataRepository.validateCurrentContext()
    }
    
    /**
     * Gets a summary of data for the current profile
     */
    suspend fun getCurrentProfileDataSummary(): Result<ProfileDataSummary> {
        return try {
            val profileId = userContextManager.requireCurrentUserId()
            val summary = profileDataRepository.getProfileDataSummary(profileId)
            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Ensures that all operations are performed within a valid profile context
     */
    suspend fun <T> withProfileContext(operation: suspend () -> T): Result<T> {
        return try {
            if (!hasActiveContext()) {
                Result.failure(Exception("No active profile context"))
            } else {
                val result = operation()
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ProfileDataSummary(
    val profileId: String,
    val totalMeals: Int,
    val totalHealthMetrics: Int,
    val activeHabits: Int
)