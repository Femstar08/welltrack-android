package com.beaconledger.welltrack.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.beaconledger.welltrack.accessibility.AccessibilitySettings
import com.beaconledger.welltrack.accessibility.AccessibilityUtils

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 45.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    )
)

/**
 * Extension function to create accessibility-friendly typography
 */
fun Typography.toAccessible(accessibilitySettings: AccessibilitySettings): Typography {
    val fontScale = accessibilitySettings.fontScale
    
    return Typography(
        bodyLarge = bodyLarge.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(bodyLarge.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(bodyLarge.lineHeight, fontScale)
        ),
        bodyMedium = bodyMedium.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(bodyMedium.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(bodyMedium.lineHeight, fontScale)
        ),
        bodySmall = bodySmall.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(bodySmall.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(bodySmall.lineHeight, fontScale)
        ),
        titleLarge = titleLarge.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(titleLarge.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(titleLarge.lineHeight, fontScale)
        ),
        titleMedium = titleMedium.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(titleMedium.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(titleMedium.lineHeight, fontScale)
        ),
        titleSmall = titleSmall.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(titleSmall.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(titleSmall.lineHeight, fontScale)
        ),
        labelLarge = labelLarge.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(labelLarge.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(labelLarge.lineHeight, fontScale)
        ),
        labelMedium = labelMedium.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(labelMedium.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(labelMedium.lineHeight, fontScale)
        ),
        labelSmall = labelSmall.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(labelSmall.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(labelSmall.lineHeight, fontScale)
        ),
        headlineLarge = headlineLarge.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(headlineLarge.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(headlineLarge.lineHeight, fontScale)
        ),
        headlineMedium = headlineMedium.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(headlineMedium.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(headlineMedium.lineHeight, fontScale)
        ),
        headlineSmall = headlineSmall.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(headlineSmall.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(headlineSmall.lineHeight, fontScale)
        ),
        displayLarge = displayLarge.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(displayLarge.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(displayLarge.lineHeight, fontScale)
        ),
        displayMedium = displayMedium.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(displayMedium.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(displayMedium.lineHeight, fontScale)
        ),
        displaySmall = displaySmall.copy(
            fontSize = AccessibilityUtils.getAccessibleTextSize(displaySmall.fontSize, fontScale),
            lineHeight = AccessibilityUtils.getAccessibleTextSize(displaySmall.lineHeight, fontScale)
        )
    )
}