package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FGGreenPrimary,
    onPrimary = Color.Black,
    primaryContainer = FGGreenContainer,
    onPrimaryContainer = FGGreenSecondary,
    secondary = FGGreenSecondary,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF1E3A2E),
    onSecondaryContainer = Color.White,
    tertiary = FGPurpleAccent,
    onTertiary = Color.White,
    background = FGGreenDarkBackground,
    onBackground = FGTextPrimaryLight,
    surface = FGGreenDarkSurface,
    onSurface = FGTextPrimaryLight,
    surfaceVariant = Color(0xFF1C342A),
    onSurfaceVariant = FGTextSecondaryLight,
    outline = FGDividerDark,
    outlineVariant = Color(0xFF2C4E40)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF00963F),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC7F3DA),
    onPrimaryContainer = Color(0xFF003816),
    secondary = Color(0xFF12372A),
    onSecondary = Color.White,
    secondaryContainer = FGSoftGreenCard,
    onSecondaryContainer = Color(0xFF0D281F),
    tertiary = Color(0xFF651FFF),
    onTertiary = Color.White,
    background = FGSoftGreenBackground,
    onBackground = FGTextPrimaryDark,
    surface = FGSoftGreenSurface,
    onSurface = FGTextPrimaryDark,
    surfaceVariant = FGSoftGreenCard,
    onSurfaceVariant = FGTextSecondaryDark,
    outline = FGDividerLight,
    outlineVariant = Color(0xFFBCE3CD)
)

@Composable
fun FGTimeTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
