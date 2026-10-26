package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = FruitGreen,
    onPrimary = Color.White,
    primaryContainer = FruitGreenContainer,
    onPrimaryContainer = FruitGreenDark,
    secondary = FruitOrange,
    onSecondary = Color.White,
    secondaryContainer = FruitOrangeContainer,
    onSecondaryContainer = FruitOrange,
    tertiary = FruitGreenLight,
    background = SurfaceBg,
    onBackground = TextDark,
    surface = SurfaceCard,
    onSurface = TextDark,
    surfaceVariant = Color(0xFFF1F5F2),
    onSurfaceVariant = TextMuted,
    outline = BorderSubtle,
    error = DangerRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D26),
    onPrimaryContainer = Color(0xFFA5D6A7),
    secondary = FruitOrangeLight,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5A2500),
    onSecondaryContainer = Color(0xFFFFCC99),
    background = Color(0xFF121A14),
    onBackground = Color(0xFFE8EDE9),
    surface = Color(0xFF1C261F),
    onSurface = Color(0xFFE8EDE9),
    outline = Color(0xFF2E3D31),
    error = Color(0xFFEF5350)
)

@Composable
fun FruitXpressTheme(
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
