package com.beaconledger.welltrack.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_preferences")
data class UserPreferences(
    @PrimaryKey
    val userId: String,
    val notificationsEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val language: String = "en",
    val accessibilitySettings: AccessibilitySettings = AccessibilitySettings()
)

data class AccessibilitySettings(
    val highContrastEnabled: Boolean = false,
    val reduceAnimationsEnabled: Boolean = false,
    val largeTextEnabled: Boolean = false,
    val screenReaderOptimizationEnabled: Boolean = true,
    val audioDescriptionsEnabled: Boolean = false,
    val largeTouchTargetsEnabled: Boolean = false,
    val reduceMotionEnabled: Boolean = false,
    val simplifiedUIEnabled: Boolean = false,
    val extendedTimeoutsEnabled: Boolean = false
)