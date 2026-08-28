package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ChampagneGold,
    onPrimary = ObsidianCanvas,
    primaryContainer = ChampagneGoldDark,
    onPrimaryContainer = ChampagneGoldLight,
    secondary = Color(0xFF9EABB8),
    onSecondary = ObsidianCanvas,
    secondaryContainer = ObsidianSurfaceElevated,
    onSecondaryContainer = TextPrimaryDark,
    tertiary = StatusInfoDark,
    onTertiary = Color.White,
    background = ObsidianCanvas,
    onBackground = TextPrimaryDark,
    surface = ObsidianSurface,
    onSurface = TextPrimaryDark,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondaryDark,
    outline = ObsidianBorder,
    outlineVariant = ObsidianBorderSubtle,
    error = StatusDangerDark,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ChampagneGoldDark,
    onPrimary = Color.White,
    primaryContainer = ChampagneGoldLight,
    onPrimaryContainer = TextPrimaryLight,
    secondary = Color(0xFF5A6472),
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceElevated,
    onSecondaryContainer = TextPrimaryLight,
    tertiary = StatusInfoDark,
    onTertiary = Color.White,
    background = LightCanvas,
    onBackground = TextPrimaryLight,
    surface = LightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = LightSurfaceElevated,
    onSurfaceVariant = TextSecondaryLight,
    outline = LightBorder,
    outlineVariant = LightBorderSubtle,
    error = StatusDangerDark,
    onError = Color.White
)

@Composable
fun LiveWallpaperAdminTheme(
    darkTheme: Boolean = true, // Default Dark as specified
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
