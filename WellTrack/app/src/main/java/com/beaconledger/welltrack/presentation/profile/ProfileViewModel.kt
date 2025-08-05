package com.beaconledger.welltrack.presentation.profile

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.beaconledger.welltrack.data.model.*
import com.beaconledger.welltrack.data.profile.ProfileSessionManager
import com.beaconledger.welltrack.domain.repository.AuthRepository
import com.beaconledger.welltrack.domain.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val authRepository: AuthRepository,
    private val profileSessionManager: ProfileSessionManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileResult>(ProfileResult.Loading)
    val profileState: StateFlow<ProfileResult> = _profileState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _allProfiles = MutableStateFlow<List<UserProfile>>(emptyList())
    val allProfiles: StateFlow<List<UserProfile>> = _allProfiles.asStateFlow()

    // Multi-user profile state
    val activeProfile: StateFlow<UserProfile?> = profileSessionManager.activeProfile
    
    private val _hasMultipleProfiles = MutableStateFlow(false)
    val hasMultipleProfiles: StateFlow<Boolean> = _hasMultipleProfiles.asStateFlow()
    
    private val _profileSwitchResult = MutableStateFlow<ProfileSwitchResult>(ProfileSwitchResult.Loading)
    val profileSwitchResult: StateFlow<ProfileSwitchResult> = _profileSwitchResult.asStateFlow()

    init {
        loadCurrentUserProfile()
        loadAllProfiles()
        observeProfileSession()
    }

    private fun observeProfileSession() {
        viewModelScope.launch {
            profileSessionManager.allProfiles.collect { profiles ->
                _allProfiles.value = profiles
                _hasMultipleProfiles.value = profiles.size > 1
                
                if (profiles.isNotEmpty() && profileSessionManager.activeProfile.value == null) {
                    profileSessionManager.setActiveProfile(profiles.first())
                }
                
                // Update profile switch result
                val activeProfile = profileSessionManager.activeProfile.value
                if (activeProfile != null) {
                    _profileSwitchResult.value = ProfileSwitchResult.Success(activeProfile, profiles)
                }
            }
        }
    }

    private fun loadCurrentUserProfile() {
        val userId = authRepository.getCurrentUserId()
        if (userId != null) {
            viewModelScope.launch {
                profileRepository.getProfileFlow(userId).collect { profile ->
                    _profileState.value = if (profile != null) {
                        ProfileResult.Success(profile)
                    } else {
                        ProfileResult.Error("Profile not found")
                    }
                }
            }
        } else {
            _profileState.value = ProfileResult.Error("User not authenticated")
        }
    }

    private fun loadAllProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { profiles ->
                _allProfiles.value = profiles
            }
        }
    }

    fun createProfile(request: ProfileCreationRequest) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = profileRepository.createProfile(userId, request)
            result.fold(
                onSuccess = { profile ->
                    _profileState.value = ProfileResult.Success(profile)
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to create profile"
                }
            )

            _isLoading.value = false
        }
    }

    fun updateProfile(request: ProfileUpdateRequest) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = profileRepository.updateProfile(userId, request)
            result.fold(
                onSuccess = { profile ->
                    _profileState.value = ProfileResult.Success(profile)
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to update profile"
                }
            )

            _isLoading.value = false
        }
    }

    fun updateProfilePhoto(photoUri: Uri) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // First upload the photo
            val uploadResult = profileRepository.uploadProfilePhoto(userId, photoUri)
            uploadResult.fold(
                onSuccess = { photoUrl ->
                    // Then update the profile with the new photo URL
                    val updateResult = profileRepository.updateProfilePhoto(userId, photoUrl)
                    updateResult.fold(
                        onSuccess = {
                            // Profile will be updated via the flow
                        },
                        onFailure = { exception ->
                            _errorMessage.value = exception.message ?: "Failed to update profile photo"
                        }
                    )
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to upload photo"
                }
            )

            _isLoading.value = false
        }
    }

    fun deleteProfile() {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = profileRepository.deleteProfile(userId)
            result.fold(
                onSuccess = {
                    _profileState.value = ProfileResult.Error("Profile deleted")
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete profile"
                }
            )

            _isLoading.value = false
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun getCurrentProfile(): UserProfile? {
        return when (val state = _profileState.value) {
            is ProfileResult.Success -> state.profile
            else -> null
        }
    }

    fun hasProfile(): Boolean {
        return _profileState.value is ProfileResult.Success
    }

    // Multi-user profile methods
    fun switchToProfile(profileId: String) {
        viewModelScope.launch {
            _profileSwitchResult.value = ProfileSwitchResult.Loading
            
            val success = profileSessionManager.switchToProfile(profileId)
            if (success) {
                profileSessionManager.updateLastActivity()
                // Reload data for the new active profile
                loadCurrentUserProfile()
                
                val activeProfile = profileSessionManager.activeProfile.value
                val allProfiles = _allProfiles.value
                if (activeProfile != null) {
                    _profileSwitchResult.value = ProfileSwitchResult.Success(activeProfile, allProfiles)
                }
            } else {
                _errorMessage.value = "Failed to switch to profile"
                _profileSwitchResult.value = ProfileSwitchResult.Error("Failed to switch to profile")
            }
        }
    }

    fun addNewProfile(request: ProfileCreationRequest) {
        val userId = authRepository.getCurrentUserId()
        if (userId == null) {
            _errorMessage.value = "User not authenticated"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Create a unique profile ID for family member
            val profileId = "${userId}_${System.currentTimeMillis()}"
            val result = profileRepository.createProfile(profileId, request)
            
            result.fold(
                onSuccess = { profile ->
                    // Add to all profiles list
                    val updatedProfiles = _allProfiles.value + profile
                    _allProfiles.value = updatedProfiles
                    profileSessionManager.updateAllProfiles(updatedProfiles)
                    
                    // Switch to the new profile
                    profileSessionManager.setActiveProfile(profile)
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to create profile"
                }
            )

            _isLoading.value = false
        }
    }

    fun getActiveProfileId(): String? {
        return profileSessionManager.getActiveProfileId()
    }

    fun getProfileSwitchCount(): Int {
        return profileSessionManager.getProfileSwitchCount()
    }

    fun hasMultipleProfilesValue(): Boolean {
        return profileSessionManager.hasMultipleProfiles()
    }

    fun updateProfileSessionActivity() {
        profileSessionManager.updateLastActivity()
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            // Don't allow deleting the active profile if it's the only one
            val profiles = _allProfiles.value
            if (profiles.size == 1) {
                _errorMessage.value = "Cannot delete the only profile"
                _isLoading.value = false
                return@launch
            }

            // Don't allow deleting the currently active profile
            val activeProfileId = profileSessionManager.getActiveProfileId()
            if (profileId == activeProfileId) {
                _errorMessage.value = "Cannot delete the active profile. Switch to another profile first."
                _isLoading.value = false
                return@launch
            }

            val result = profileRepository.deleteProfile(profileId)
            result.fold(
                onSuccess = {
                    // Remove from local list
                    val updatedProfiles = _allProfiles.value.filter { it.userId != profileId }
                    _allProfiles.value = updatedProfiles
                    profileSessionManager.updateAllProfiles(updatedProfiles)
                    _hasMultipleProfiles.value = updatedProfiles.size > 1
                },
                onFailure = { exception ->
                    _errorMessage.value = exception.message ?: "Failed to delete profile"
                }
            )

            _isLoading.value = false
        }
    }

    fun getProfileById(profileId: String): UserProfile? {
        return _allProfiles.value.find { it.userId == profileId }
    }

    fun refreshProfiles() {
        loadAllProfiles()
    }

    fun clearProfileSwitchResult() {
        val activeProfile = profileSessionManager.activeProfile.value
        val allProfiles = _allProfiles.value
        if (activeProfile != null) {
            _profileSwitchResult.value = ProfileSwitchResult.Success(activeProfile, allProfiles)
        }
    }
}