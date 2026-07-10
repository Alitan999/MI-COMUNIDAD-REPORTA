package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CivicPrimaryDark,
    onPrimary = CivicOnPrimaryDark,
    primaryContainer = CivicPrimaryContainerDark,
    onPrimaryContainer = Color(0xFFD3E4FF),
    secondary = CivicSecondaryDark,
    onSecondary = CivicOnSecondaryDark,
    secondaryContainer = CivicSecondaryContainerDark,
    onSecondaryContainer = Color(0xFFEADDFF),
    background = CivicBackgroundDark,
    onBackground = CivicOnBackgroundDark,
    surface = CivicSurfaceDark,
    onSurface = CivicOnSurfaceDark,
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF938F99),
    outlineVariant = Color(0xFF49454F),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

private val LightColorScheme = lightColorScheme(
    primary = CivicPrimary,
    onPrimary = CivicOnPrimary,
    primaryContainer = CivicPrimaryContainer,
    onPrimaryContainer = Color(0xFF001C38),
    secondary = CivicSecondary,
    onSecondary = CivicOnSecondary,
    secondaryContainer = CivicSecondaryContainer,
    onSecondaryContainer = Color(0xFF21005D),
    background = CivicBackground,
    onBackground = CivicOnBackground,
    surface = CivicSurface,
    onSurface = CivicOnSurface,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    errorContainer = Color(0xFFF2B8B5),
    onErrorContainer = Color(0xFF601410)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Keep dynamicColor false by default to ensure our highly polished custom civic theme is preserved and visible!
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
