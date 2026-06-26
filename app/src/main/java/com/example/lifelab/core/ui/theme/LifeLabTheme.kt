package com.example.lifelab.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.lifelab.core.datastore.ThemeMode

private val LifeLabLightColors = lightColorScheme(
    primary = Color(0xFF0968D8),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD7E8FF),
    onPrimaryContainer = Color(0xFF001B3F),
    secondary = Color(0xFF34618E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD4E4F7),
    onSecondaryContainer = Color(0xFF071D32),
    tertiary = Color(0xFF006B7A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB9EBF4),
    onTertiaryContainer = Color(0xFF001F25),
    background = Color(0xFFF8FBFF),
    onBackground = Color(0xFF101828),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF101828),
    surfaceVariant = Color(0xFFE3EAF3),
    onSurfaceVariant = Color(0xFF415162),
    outline = Color(0xFF728197),
)

private val LifeLabDarkColors = darkColorScheme(
    primary = Color(0xFF8EC5FF),
    onPrimary = Color(0xFF002F67),
    primaryContainer = Color(0xFF064A98),
    onPrimaryContainer = Color(0xFFD7E8FF),
    secondary = Color(0xFFA6C8EA),
    onSecondary = Color(0xFF0B314F),
    secondaryContainer = Color(0xFF244A70),
    onSecondaryContainer = Color(0xFFD4E4F7),
    tertiary = Color(0xFF76D7E6),
    onTertiary = Color(0xFF00363F),
    tertiaryContainer = Color(0xFF00515C),
    onTertiaryContainer = Color(0xFFB9EBF4),
    background = Color(0xFF07111F),
    onBackground = Color(0xFFE6EEF8),
    surface = Color(0xFF0B1626),
    onSurface = Color(0xFFE6EEF8),
    surfaceVariant = Color(0xFF1D2A3A),
    onSurfaceVariant = Color(0xFFC2CEDB),
    outline = Color(0xFF8291A5),
)

@Composable
fun LifeLabTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit,
) {
    val useDarkColors = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }

    MaterialTheme(
        colorScheme = if (useDarkColors) LifeLabDarkColors else LifeLabLightColors,
        content = content,
    )
}
