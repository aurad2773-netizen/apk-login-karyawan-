package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = MintTealDark,
    onPrimary = OnDeepTealContainer,
    primaryContainer = DeepTealDarkContainer,
    onPrimaryContainer = SoftTealContainer,
    secondary = CoralAmberDark,
    onSecondary = OnTerracottaContainer,
    secondaryContainer = TerracottaDarkContainer,
    onSecondaryContainer = SoftTerracottaContainer,
    tertiary = GoldDark,
    onTertiary = OnAmberContainer,
    background = SlatePineBackground,
    onBackground = OffWhiteInk,
    surface = SlatePineSurface,
    onSurface = OffWhiteInk,
    surfaceVariant = SlatePineSurfaceVariant,
    onSurfaceVariant = SoftSageInk,
    outline = DarkOutline,
    error = ErrorRedDark,
    errorContainer = ErrorContainerDark
)

private val LightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = SoftTealContainer,
    onPrimaryContainer = OnDeepTealContainer,
    secondary = WarmTerracotta,
    onSecondary = Color.White,
    secondaryContainer = SoftTerracottaContainer,
    onSecondaryContainer = OnTerracottaContainer,
    tertiary = GoldenAmber,
    onTertiary = Color.White,
    tertiaryContainer = SoftAmberContainer,
    onTertiaryContainer = OnAmberContainer,
    background = CreamBackground,
    onBackground = CharcoalInk,
    surface = WarmSurface,
    onSurface = CharcoalInk,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedSageInk,
    outline = SubtleOutline,
    error = ErrorRed,
    errorContainer = ErrorContainerLight
)

val AppShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = AppShapes,
        content = content
    )
}
