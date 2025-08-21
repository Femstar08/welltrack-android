package com.beaconledger.welltrack.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val WellTrackDarkColorScheme = darkColorScheme(
    primary = HealthGreen80,
    onPrimary = Neutral10,
    primaryContainer = HealthGreen20,
    onPrimaryContainer = HealthGreen80,
    
    secondary = NutritionOrange80,
    onSecondary = Neutral10,
    secondaryContainer = NutritionOrange20,
    onSecondaryContainer = NutritionOrange80,
    
    tertiary = FitnessBlue80,
    onTertiary = Neutral10,
    tertiaryContainer = FitnessBlue20,
    onTertiaryContainer = FitnessBlue80,
    
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral20,
    onSurface = Neutral90,
    surfaceVariant = Neutral30,
    onSurfaceVariant = Neutral80,
    
    error = Error,
    onError = Neutral99,
    errorContainer = Neutral20,
    onErrorContainer = Error,
    
    outline = Neutral60,
    outlineVariant = Neutral40,
    scrim = Neutral0,
    
    surfaceTint = HealthGreen80,
    inverseSurface = Neutral90,
    inverseOnSurface = Neutral20,
    inversePrimary = HealthGreen40
)

private val WellTrackLightColorScheme = lightColorScheme(
    primary = HealthGreen40,
    onPrimary = Neutral99,
    primaryContainer = HealthGreen80,
    onPrimaryContainer = HealthGreen20,
    
    secondary = NutritionOrange40,
    onSecondary = Neutral99,
    secondaryContainer = NutritionOrange80,
    onSecondaryContainer = NutritionOrange20,
    
    tertiary = FitnessBlue40,
    onTertiary = Neutral99,
    tertiaryContainer = FitnessBlue80,
    onTertiaryContainer = FitnessBlue20,
    
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = Neutral40,
    
    error = Error,
    onError = Neutral99,
    errorContainer = Neutral95,
    onErrorContainer = Error,
    
    outline = Neutral60,
    outlineVariant = Neutral80,
    scrim = Neutral0,
    
    surfaceTint = HealthGreen40,
    inverseSurface = Neutral20,
    inverseOnSurface = Neutral95,
    inversePrimary = HealthGreen80
)

@Composable
fun WellTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to maintain consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> WellTrackDarkColorScheme
        else -> WellTrackLightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}