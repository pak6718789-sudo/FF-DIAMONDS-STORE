package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StoreDarkColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = Color(0xFF041320),
    primaryContainer = DeepBlueContainer,
    onPrimaryContainer = NeonCyan,
    secondary = NeonCyan,
    onSecondary = Color(0xFF041320),
    secondaryContainer = CyanContainer,
    onSecondaryContainer = TextPrimary,
    tertiary = GoldAccent,
    onTertiary = Color(0xFF211600),
    tertiaryContainer = GoldContainer,
    onTertiaryContainer = GoldAccent,
    background = StoreBackground,
    onBackground = TextPrimary,
    surface = StoreSurface,
    onSurface = TextPrimary,
    surfaceVariant = StoreSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = StoreBackground,
    surfaceContainerLow = StoreSurface,
    surfaceContainer = StoreSurfaceElevated,
    surfaceContainerHigh = StoreSurfaceVariant,
    outline = StoreOutline,
    outlineVariant = Color(0xFF1A2842),
    error = StatusError,
    onError = Color.White,
    errorContainer = StatusErrorContainer,
    onErrorContainer = Color(0xFFFCA5A5)
)

val StoreShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun FFDiamondStoreTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StoreDarkColorScheme,
        typography = StoreTypography,
        shapes = StoreShapes,
        content = content
    )
}
