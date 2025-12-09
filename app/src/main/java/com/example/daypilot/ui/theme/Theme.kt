package com.example.daypilot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ===============================================================
//  LIGHT THEME
// ===============================================================
private val LightColors = lightColorScheme(

    // Colores principales del UI
    primary = BrandBlue,
    secondary = BrandOrange,
    tertiary = BrandPurple,

    // Fondos
    background = BackgroundLight,
    surface = SurfaceLight,

    // Texto
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,

    // Errores
    error = BrandRed,
    onError = Color.White
)

// ===============================================================
//  DARK THEME
// ===============================================================
private val DarkColors = darkColorScheme(

    // Colores principales del UI
    primary = BrandBlue,
    secondary = BrandOrange,
    tertiary = BrandPurple,

    // Fondos
    background = BackgroundDark,
    surface = SurfaceDark,

    // Texto
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,

    // Errores
    error = BrandRedDeep,
    onError = Color.White
)

// ===============================================================
//  APLICACIÓN DEL TEMA
// ===============================================================
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