package com.example.daypilot.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme

private val LightColors = lightColorScheme(
    primary = Red,
    secondary = Orange,
    tertiary = Blue,

    background = BackgroundLight,
    surface = SurfaceLight,

    onPrimary = BackgroundLight,
    onSecondary = BackgroundLight,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

private val DarkColors = darkColorScheme(
    primary = RedDarkMode,
    secondary = OrangeDarkMode,

    background = BackgroundDark,
    surface = SurfaceDark,

    onPrimary = TextPrimaryDark,
    onSecondary = TextPrimaryDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark
)

@Composable
fun DayPilotTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}