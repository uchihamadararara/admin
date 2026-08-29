package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Geometric Balance Color Scheme
private val GeometricColorScheme = lightColorScheme(
    primary = GeoPrimary,
    onPrimary = Color.White,
    primaryContainer = GeoPrimaryContainer,
    onPrimaryContainer = GeoOnPrimaryContainer,
    secondary = GeoSecondary,
    onSecondary = Color.White,
    secondaryContainer = GeoSecondaryContainer,
    onSecondaryContainer = GeoOnSecondaryContainer,
    tertiary = GeoTertiary,
    onTertiary = Color.White,
    tertiaryContainer = GeoTertiaryContainer,
    onTertiaryContainer = GeoOnTertiaryContainer,
    background = GeoBackground,
    onBackground = TextPrimary,
    surface = GeoSurface,
    onSurface = TextPrimary,
    surfaceVariant = GeoSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = GeoSurfaceContainer,
    outline = GeoCardBorder,
    outlineVariant = GeoOutlineVariant,
    error = GeoRose,
    onError = Color.White,
    errorContainer = GeoRoseContainer,
    onErrorContainer = GeoRoseText
)

@Composable
fun RoyalAdminTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GeometricColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    RoyalAdminTheme(content = content)
}

@Composable
fun LiveWallpaperAdminTheme(
    content: @Composable () -> Unit
) {
    RoyalAdminTheme(content = content)
}

