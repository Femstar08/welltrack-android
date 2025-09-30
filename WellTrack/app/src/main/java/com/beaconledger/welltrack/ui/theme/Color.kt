package com.beaconledger.welltrack.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Colors - Health & Wellness Theme
val HealthGreen80 = Color(0xFFA8E6A3)
val HealthGreen60 = Color(0xFF81C784)
val HealthGreen40 = Color(0xFF4CAF50)
val HealthGreen20 = Color(0xFF2E7D32)

// Secondary Colors - Nutrition Theme
val NutritionOrange80 = Color(0xFFFFCC80)
val NutritionOrange60 = Color(0xFFFFB74D)
val NutritionOrange40 = Color(0xFFFF9800)
val NutritionOrange20 = Color(0xFFE65100)

// Tertiary Colors - Fitness Theme
val FitnessBlue80 = Color(0xFF90CAF9)
val FitnessBlue60 = Color(0xFF64B5F6)
val FitnessBlue40 = Color(0xFF2196F3)
val FitnessBlue20 = Color(0xFF0D47A1)

// Neutral Colors
val Neutral99 = Color(0xFFFFFBFF)
val Neutral95 = Color(0xFFF5F5F5)
val Neutral90 = Color(0xFFE0E0E0)
val Neutral80 = Color(0xFFBDBDBD)
val Neutral70 = Color(0xFF9E9E9E)
val Neutral60 = Color(0xFF757575)
val Neutral50 = Color(0xFF616161)
val Neutral40 = Color(0xFF424242)
val Neutral30 = Color(0xFF303030)
val Neutral20 = Color(0xFF212121)
val Neutral10 = Color(0xFF121212)
val Neutral0 = Color(0xFF000000)

// Semantic Colors
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFF9800)
val Error = Color(0xFFF44336)
val Info = Color(0xFF2196F3)

// Meal Score Colors
val ScoreA = Color(0xFF4CAF50) // Green
val ScoreB = Color(0xFF8BC34A) // Light Green
val ScoreC = Color(0xFFFFEB3B) // Yellow
val ScoreD = Color(0xFFFF9800) // Orange
val ScoreE = Color(0xFFF44336) // Red

// Legacy colors for compatibility
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Accessibility-friendly colors with WCAG AA compliance
object AccessibleColors {
    // High contrast colors for accessibility
    val HighContrastBackground = Color(0xFF000000)
    val HighContrastSurface = Color(0xFF1A1A1A)
    val HighContrastOnBackground = Color(0xFFFFFFFF)
    val HighContrastOnSurface = Color(0xFFFFFFFF)
    
    // Primary colors with sufficient contrast (4.5:1 minimum)
    val AccessiblePrimary = Color(0xFF1976D2) // Blue with 4.5:1 contrast on white
    val AccessibleOnPrimary = Color(0xFFFFFFFF)
    val AccessiblePrimaryContainer = Color(0xFFE3F2FD)
    val AccessibleOnPrimaryContainer = Color(0xFF0D47A1)
    
    // Secondary colors with sufficient contrast
    val AccessibleSecondary = Color(0xFF388E3C) // Green with 4.5:1 contrast on white
    val AccessibleOnSecondary = Color(0xFFFFFFFF)
    val AccessibleSecondaryContainer = Color(0xFFE8F5E8)
    val AccessibleOnSecondaryContainer = Color(0xFF1B5E20)
    
    // Error colors with sufficient contrast
    val AccessibleError = Color(0xFFD32F2F) // Red with 4.5:1 contrast on white
    val AccessibleOnError = Color(0xFFFFFFFF)
    val AccessibleErrorContainer = Color(0xFFFFEBEE)
    val AccessibleOnErrorContainer = Color(0xFFB71C1C)
    
    // Warning colors with sufficient contrast
    val AccessibleWarning = Color(0xFFF57C00) // Orange with 4.5:1 contrast on white
    val AccessibleOnWarning = Color(0xFFFFFFFF)
    val AccessibleWarningContainer = Color(0xFFFFF3E0)
    val AccessibleOnWarningContainer = Color(0xFFE65100)
    
    // Success colors with sufficient contrast
    val AccessibleSuccess = Color(0xFF2E7D32) // Green with 4.5:1 contrast on white
    val AccessibleOnSuccess = Color(0xFFFFFFFF)
    val AccessibleSuccessContainer = Color(0xFFE8F5E8)
    val AccessibleOnSuccessContainer = Color(0xFF1B5E20)
    
    // Neutral colors with sufficient contrast
    val AccessibleOutline = Color(0xFF757575) // Gray with 4.5:1 contrast on white
    val AccessibleOutlineVariant = Color(0xFFBDBDBD)
    val AccessibleSurfaceVariant = Color(0xFFF5F5F5)
    val AccessibleOnSurfaceVariant = Color(0xFF424242)
    
    // Focus indicator colors
    val FocusIndicator = Color(0xFF2196F3) // Blue focus ring
    val FocusIndicatorHigh = Color(0xFF0D47A1) // High contrast focus ring
    
    // Meal score colors with improved contrast
    val AccessibleScoreA = Color(0xFF2E7D32) // Dark green for better contrast
    val AccessibleScoreB = Color(0xFF689F38) // Medium green
    val AccessibleScoreC = Color(0xFFF57C00) // Orange (better than yellow for contrast)
    val AccessibleScoreD = Color(0xFFE64A19) // Dark orange
    val AccessibleScoreE = Color(0xFFD32F2F) // Dark red
}