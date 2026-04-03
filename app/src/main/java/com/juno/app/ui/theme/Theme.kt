package com.juno.app.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class AppTheme {
    LIGHT, DARK, MORANDI, MINIMALIST
}

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    error = DarkError,
    onError = DarkOnError,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    error = LightError,
    onError = LightOnError,
    background = Color(0xFFF8F9FF),
    onBackground = LightOnBackground,
    surface = Color(0xFFFFFFFF),
    onSurface = LightOnSurface,
    surfaceVariant = Color(0xFFF0F4FF),
    onSurfaceVariant = LightOnSurfaceVariant
)

private val MorandiColorScheme = lightColorScheme(
    primary = MorandiPrimary,
    onPrimary = MorandiOnPrimary,
    primaryContainer = MorandiSecondary,
    onPrimaryContainer = MorandiOnSecondary,
    secondary = MorandiSecondary,
    onSecondary = MorandiOnSecondary,
    secondaryContainer = MorandiSurface,
    onSecondaryContainer = MorandiOnSurface,
    tertiary = MorandiPrimary,
    onTertiary = MorandiOnPrimary,
    background = MorandiBackground,
    onBackground = MorandiOnBackground,
    surface = MorandiSurface,
    onSurface = MorandiOnSurface,
    surfaceVariant = MorandiSurfaceVariant,
    onSurfaceVariant = MorandiOnSurface,
    outline = MorandiOutline
)

private val MinimalistColorScheme = lightColorScheme(
    primary = MinimalistPrimary,
    onPrimary = MinimalistOnPrimary,
    primaryContainer = MinimalistSurface,
    onPrimaryContainer = MinimalistOnSurface,
    secondary = MinimalistSecondary,
    onSecondary = MinimalistOnSecondary,
    secondaryContainer = MinimalistSurface,
    onSecondaryContainer = MinimalistOnSurface,
    tertiary = MinimalistTertiary,
    onTertiary = MinimalistOnPrimary,
    background = MinimalistBackground,
    onBackground = MinimalistOnBackground,
    surface = MinimalistSurface,
    onSurface = MinimalistOnSurface,
    surfaceVariant = MinimalistSurface,
    onSurfaceVariant = MinimalistOnSurface
)

@Composable
fun JunoTheme(
    theme: AppTheme = AppTheme.LIGHT,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDarkTheme = theme == AppTheme.DARK
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        theme == AppTheme.DARK -> DarkColorScheme
        theme == AppTheme.MORANDI -> MorandiColorScheme
        theme == AppTheme.MINIMALIST -> MinimalistColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
