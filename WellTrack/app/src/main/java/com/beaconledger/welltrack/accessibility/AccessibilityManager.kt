package com.beaconledger.welltrack.accessibility

import android.content.Context
import android.content.res.Configuration
import android.view.accessibility.AccessibilityManager as AndroidAccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages accessibility features and settings for the WellTrack app
 * Provides utilities for TalkBack, large text, high contrast, and WCAG compliance
 */
@Singleton
class AccessibilityManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AndroidAccessibilityManager

    /**
     * Check if TalkBack or other screen readers are enabled
     */
    fun isTalkBackEnabled(): Boolean {
        return accessibilityManager.isEnabled && accessibilityManager.isTouchExplorationEnabled
    }

    /**
     * Check if high contrast mode is enabled
     */
    fun isHighContrastEnabled(): Boolean {
        return try {
            val configuration = context.resources.configuration
            (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get the current font scale for large text support
     */
    fun getFontScale(): Float {
        return context.resources.configuration.fontScale
    }

    /**
     * Check if large text is enabled (font scale > 1.0)
     */
    fun isLargeTextEnabled(): Boolean {
        return getFontScale() > 1.0f
    }

    /**
     * Get recommended minimum touch target size based on accessibility guidelines
     * WCAG 2.1 AA requires minimum 44dp touch targets
     */
    fun getMinimumTouchTargetSize(): Dp {
        return if (isTalkBackEnabled() || isLargeTextEnabled()) {
            48.dp // Larger for accessibility
        } else {
            44.dp // WCAG minimum
        }
    }

    /**
     * Get recommended spacing between interactive elements
     */
    fun getRecommendedSpacing(): Dp {
        return if (isTalkBackEnabled() || isLargeTextEnabled()) {
            16.dp
        } else {
            12.dp
        }
    }

    /**
     * Check if animations should be reduced for accessibility
     */
    fun shouldReduceAnimations(): Boolean {
        return try {
            val animationScale = android.provider.Settings.Global.getFloat(
                context.contentResolver,
                android.provider.Settings.Global.ANIMATOR_DURATION_SCALE,
                1.0f
            )
            animationScale == 0.0f
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get accessibility-friendly animation duration
     */
    fun getAccessibleAnimationDuration(): Long {
        return if (shouldReduceAnimations()) {
            0L
        } else if (isTalkBackEnabled()) {
            150L // Shorter for screen readers
        } else {
            300L // Standard duration
        }
    }
}

