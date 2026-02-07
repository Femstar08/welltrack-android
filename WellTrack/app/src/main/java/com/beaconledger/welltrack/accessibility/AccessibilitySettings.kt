package com.beaconledger.welltrack.accessibility

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.res.Configuration
import android.view.accessibility.AccessibilityManager as AndroidAccessibilityManager
import com.beaconledger.welltrack.data.model.AccessibilitySettings as UserAccessibilitySettings

/**
 * UI accessibility settings derived from system and user preferences
 */
data class UIAccessibilitySettings(
    val minimumTouchTargetSize: Dp = 48.dp,
    val fontScale: Float = 1f,
    val recommendedSpacing: Dp = 12.dp,
    val animationDuration: Long = 300L,
    val isTalkBackEnabled: Boolean = false,
    val isHighContrastEnabled: Boolean = false,
    val isLargeTextEnabled: Boolean = false,
    val shouldReduceAnimations: Boolean = false
)

/**
 * Remember accessibility settings based on system configuration
 */
@Composable
fun rememberAccessibilitySettings(): UIAccessibilitySettings {
    val context = LocalContext.current

    return remember(context) {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AndroidAccessibilityManager
        val systemFontScale = context.resources.configuration.fontScale

        val isTalkBackEnabled = accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
        val isLargeText = systemFontScale >= 1.3f
        val isHighContrast = try {
            val configuration = context.resources.configuration
            (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            false
        }
        val shouldReduceAnimations = try {
            val animationScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            animationScale == 0.0f
        } catch (e: Exception) {
            false
        }

        UIAccessibilitySettings(
            minimumTouchTargetSize = if (isTalkBackEnabled || isLargeText) 56.dp else 48.dp,
            fontScale = systemFontScale.coerceAtLeast(1f),
            recommendedSpacing = if (isTalkBackEnabled || isLargeText) 16.dp else 12.dp,
            animationDuration = when {
                shouldReduceAnimations -> 0L
                isTalkBackEnabled -> 150L
                else -> 300L
            },
            isTalkBackEnabled = isTalkBackEnabled,
            isHighContrastEnabled = isHighContrast,
            isLargeTextEnabled = isLargeText,
            shouldReduceAnimations = shouldReduceAnimations
        )
    }
}

/**
 * Get accessibility settings from user preferences
 */
@Composable
fun rememberUserAccessibilitySettings(
    userSettings: UserAccessibilitySettings? = null
): UIAccessibilitySettings {
    val systemSettings = rememberAccessibilitySettings()

    return remember(systemSettings, userSettings) {
        if (userSettings != null) {
            val enhancedLargeText = userSettings.largeTextEnabled || systemSettings.isLargeTextEnabled
            val enhancedTouchTargets = userSettings.largeTouchTargetsEnabled || systemSettings.isTalkBackEnabled

            systemSettings.copy(
                minimumTouchTargetSize = if (enhancedTouchTargets) 56.dp else systemSettings.minimumTouchTargetSize,
                fontScale = if (enhancedLargeText) (systemSettings.fontScale * 1.2f) else systemSettings.fontScale,
                recommendedSpacing = if (enhancedTouchTargets) 16.dp else systemSettings.recommendedSpacing,
                animationDuration = if (userSettings.reduceAnimationsEnabled || userSettings.reduceMotionEnabled) 0L else systemSettings.animationDuration,
                isHighContrastEnabled = userSettings.highContrastEnabled || systemSettings.isHighContrastEnabled,
                isLargeTextEnabled = enhancedLargeText,
                shouldReduceAnimations = userSettings.reduceAnimationsEnabled || userSettings.reduceMotionEnabled || systemSettings.shouldReduceAnimations
            )
        } else {
            systemSettings
        }
    }
}