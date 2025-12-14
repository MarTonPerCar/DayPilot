package com.example.daypilot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===============================================================
//  LIGHT THEME
// ===============================================================
private val LightColors = lightColorScheme(
    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = BrandBlue,

    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFE0B2),
    onSecondaryContainer = BrandOrange,

    tertiary = BrandPurple,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,

    surface = SurfaceLight,
    onSurface = TextPrimaryLight,

    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = TextSecondaryLight,

    outline = DividerLight,
    error = ErrorLight,
    onError = Color.White
)

// ===============================================================
//  DARK THEME
// ===============================================================
private val DarkColors = darkColorScheme(

    primary = BrandBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0D47A1),
    onPrimaryContainer = Color.White,

    secondary = BrandOrange,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE65100),
    onSecondaryContainer = Color.White,

    tertiary = BrandPurple,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,

    surface = SurfaceDark,
    onSurface = TextPrimaryDark,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondaryDark,

    outline = DividerDark,
    error = ErrorDark,
    onError = Color.White
)

// ===============================================================
//  APLICACIÓN DEL TEMA
// ===============================================================
@Composable
fun DayPilotTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
