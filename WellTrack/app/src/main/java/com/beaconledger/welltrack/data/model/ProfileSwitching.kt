package com.beaconledger.welltrack.data.model

data class ProfileSwitchItem(
    val profile: UserProfile,
    val isActive: Boolean = false,
    val lastUsed: String? = null
)

data class ActiveProfileSession(
    val userId: String,
    val profileId: String,
    val sessionStartTime: String,
    val lastActivityTime: String
)

enum class ProfileSwitchAction {
    SWITCH_TO_PROFILE,
    ADD_NEW_PROFILE,
    EDIT_PROFILE,
    DELETE_PROFILE
}

sealed class ProfileSwitchResult {
    object Loading : ProfileSwitchResult()
    data class Success(val activeProfile: UserProfile, val allProfiles: List<UserProfile>) : ProfileSwitchResult()
    data class Error(val message: String) : ProfileSwitchResult()
}

data class ProfileQuickAction(
    val title: String,
    val description: String,
    val iconName: String,
    val action: () -> Unit
)