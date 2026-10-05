package com.example.agendatenis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = TennisGreenLight,
    onPrimary = Color.White,
    primaryContainer = TennisGreenDark,
    onPrimaryContainer = TennisGreenContainer,
    secondary = TennisYellowAction,
    onSecondary = TennisOnYellow,
    secondaryContainer = TennisYellowActionContainer,
    onSecondaryContainer = TennisOnYellow,
    error = TennisRedDestructive,
    onError = TennisOnRed,
    errorContainer = TennisRedContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnBackground,
    outline = DarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = TennisGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = TennisGreenContainer,
    onPrimaryContainer = TennisOnGreenContainer,
    secondary = TennisYellowAction,
    onSecondary = TennisOnYellow,
    secondaryContainer = TennisYellowActionContainer,
    onSecondaryContainer = TennisOnYellow,
    error = TennisRedDestructive,
    onError = TennisOnRed,
    errorContainer = TennisRedContainer,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurface,
    outline = LightOutline
)

@Composable
fun AgendaTenisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
