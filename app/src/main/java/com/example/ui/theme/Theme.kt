package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ElegantDarkColorScheme = darkColorScheme(
    primary = ElegantLavenderPrimary,
    onPrimary = ElegantLavenderOnPrimary,
    primaryContainer = ElegantDeepPurpleContainer,
    onPrimaryContainer = ElegantLavenderOnContainer,
    secondary = ElegantPurpleSecondary,
    onSecondary = ElegantPurpleOnSecondary,
    secondaryContainer = ElegantPurpleSecondaryContainer,
    onSecondaryContainer = ElegantPurpleOnSecondaryContainer,
    tertiary = ElegantAccentRose,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = ElegantDarkBackground,
    onBackground = ElegantTextPrimaryDark,
    surface = ElegantDarkSurface,
    onSurface = ElegantTextPrimaryDark,
    surfaceVariant = ElegantDarkSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondaryDark,
    outline = ElegantDarkOutline,
    outlineVariant = ElegantDarkOutlineBorder
)

private val ElegantLightColorScheme = lightColorScheme(
    primary = ElegantPurplePrimaryLight,
    onPrimary = Color.White,
    primaryContainer = ElegantPurpleContainerLight,
    onPrimaryContainer = ElegantPurpleOnContainerLight,
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFD8E4),
    onTertiaryContainer = Color(0xFF31111D),
    background = ElegantLightBackground,
    onBackground = ElegantTextPrimaryLight,
    surface = ElegantLightSurface,
    onSurface = ElegantTextPrimaryLight,
    surfaceVariant = ElegantLightSurfaceVariant,
    onSurfaceVariant = ElegantTextSecondaryLight,
    outline = ElegantLightOutline,
    outlineVariant = Color(0xFF79747E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

