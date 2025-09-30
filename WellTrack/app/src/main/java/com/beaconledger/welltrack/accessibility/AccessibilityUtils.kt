package com.beaconledger.welltrack.accessibility

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * Utility functions for accessibility compliance and WCAG 2.1 AA standards
 */
object AccessibilityUtils {
    
    /**
     * Calculate contrast ratio between two colors
     * WCAG 2.1 AA requires minimum 4.5:1 for normal text, 3:1 for large text
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Double {
        val foregroundLuminance = calculateRelativeLuminance(foreground)
        val backgroundLuminance = calculateRelativeLuminance(background)
        
        val lighter = max(foregroundLuminance, backgroundLuminance)
        val darker = min(foregroundLuminance, backgroundLuminance)
        
        return (lighter + 0.05) / (darker + 0.05)
    }
    
    /**
     * Calculate relative luminance of a color
     */
    private fun calculateRelativeLuminance(color: Color): Double {
        val r = if (color.red <= 0.03928) color.red / 12.92 else kotlin.math.pow((color.red + 0.055) / 1.055, 2.4)
        val g = if (color.green <= 0.03928) color.green / 12.92 else kotlin.math.pow((color.green + 0.055) / 1.055, 2.4)
        val b = if (color.blue <= 0.03928) color.blue / 12.92 else kotlin.math.pow((color.blue + 0.055) / 1.055, 2.4)
        
        return 0.2126 * r + 0.7152 * g + 0.0722 * b
    }
    
    /**
     * Check if color combination meets WCAG AA standards
     */
    fun meetsWCAGAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val contrastRatio = calculateContrastRatio(foreground, background)
        return if (isLargeText) contrastRatio >= 3.0 else contrastRatio >= 4.5
    }
    
    /**
     * Check if color combination meets WCAG AAA standards
     */
    fun meetsWCAGAAA(foreground: Color, background: Color, isLargeText: Boolean = false): Boolean {
        val contrastRatio = calculateContrastRatio(foreground, background)
        return if (isLargeText) contrastRatio >= 4.5 else contrastRatio >= 7.0
    }
    
    /**
     * Get accessible text size based on font scale
     */
    fun getAccessibleTextSize(baseSize: TextUnit, fontScale: Float): TextUnit {
        return (baseSize.value * fontScale).sp
    }
    
    /**
     * Get accessible padding based on accessibility settings
     */
    fun getAccessiblePadding(
        basePadding: Dp,
        isTalkBackEnabled: Boolean,
        isLargeTextEnabled: Boolean
    ): PaddingValues {
        val multiplier = when {
            isTalkBackEnabled && isLargeTextEnabled -> 1.5f
            isTalkBackEnabled || isLargeTextEnabled -> 1.25f
            else -> 1.0f
        }
        
        val adjustedPadding = basePadding * multiplier
        return PaddingValues(adjustedPadding)
    }
    
    /**
     * Generate content description for complex UI elements
     */
    fun generateContentDescription(
        label: String,
        value: String? = null,
        state: String? = null,
        position: String? = null,
        additionalInfo: String? = null
    ): String {
        return buildString {
            append(label)
            value?.let { append(", $it") }
            state?.let { append(", $it") }
            position?.let { append(", $it") }
            additionalInfo?.let { append(", $it") }
        }
    }
    
    /**
     * Format number for screen reader announcement
     */
    fun formatNumberForScreenReader(number: Double, unit: String = ""): String {
        return when {
            number == number.toInt().toDouble() -> "${number.toInt()} $unit".trim()
            else -> "${"%.1f".format(number)} $unit".trim()
        }
    }
    
    /**
     * Format time for screen reader announcement
     */
    fun formatTimeForScreenReader(hours: Int, minutes: Int): String {
        return buildString {
            if (hours > 0) {
                append("$hours ${if (hours == 1) "hour" else "hours"}")
                if (minutes > 0) append(" and ")
            }
            if (minutes > 0) {
                append("$minutes ${if (minutes == 1) "minute" else "minutes"}")
            }
            if (hours == 0 && minutes == 0) {
                append("0 minutes")
            }
        }
    }
    
    /**
     * Format date for screen reader announcement
     */
    fun formatDateForScreenReader(day: Int, month: String, year: Int): String {
        return "$month $day, $year"
    }
    
    /**
     * Generate list position description for screen readers
     */
    fun generateListPositionDescription(currentIndex: Int, totalItems: Int): String {
        return "${currentIndex + 1} of $totalItems"
    }
    
    /**
     * Generate progress description for screen readers
     */
    fun generateProgressDescription(current: Int, total: Int, unit: String = ""): String {
        val percentage = if (total > 0) (current * 100) / total else 0
        return "$current of $total $unit, $percentage percent complete".trim()
    }
}

/**
 * Extension functions for accessibility
 */

/**
 * Check if text style is considered large text for WCAG purposes
 */
fun TextStyle.isLargeText(): Boolean {
    val fontSize = this.fontSize
    val fontWeight = this.fontWeight
    
    return when {
        fontSize.value >= 18 -> true
        fontSize.value >= 14 && fontWeight == FontWeight.Bold -> true
        else -> false
    }
}

/**
 * Get accessible text style with proper sizing
 */
@Composable
fun TextStyle.toAccessible(accessibilitySettings: AccessibilitySettings): TextStyle {
    return this.copy(
        fontSize = AccessibilityUtils.getAccessibleTextSize(this.fontSize, accessibilitySettings.fontScale)
    )
}

/**
 * Get minimum touch target size for accessibility
 */
@Composable
fun getMinimumTouchTarget(): Dp {
    val accessibilitySettings = rememberAccessibilitySettings()
    return accessibilitySettings.minimumTouchTargetSize
}

/**
 * Get recommended spacing for accessibility
 */
@Composable
fun getRecommendedSpacing(): Dp {
    val accessibilitySettings = rememberAccessibilitySettings()
    return accessibilitySettings.recommendedSpacing
}

/**
 * Get accessible animation duration
 */
@Composable
fun getAccessibleAnimationDuration(): Long {
    val accessibilitySettings = rememberAccessibilitySettings()
    return accessibilitySettings.animationDuration
}