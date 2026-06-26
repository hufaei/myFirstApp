package com.example.lifelab.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.lifelab.core.datastore.ThemeMode

private val LifeLabLightColors = lightColorScheme(
    primary = Color(0xFF2F7FCE),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF89C2FF),
    onPrimaryContainer = Color(0xFF05213D),
    secondary = Color(0xFF4E6F8F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE6F7FF),
    onSecondaryContainer = Color(0xFF123044),
    tertiary = Color(0xFF287C89),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC9F2FA),
    onTertiaryContainer = Color(0xFF06343B),
    background = Color(0xFFF7FBFF),
    onBackground = Color(0xFF111827),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE6F7FF),
    onSurfaceVariant = Color(0xFF3E5266),
    outline = Color(0xFF7890A8),
)

private val LifeLabDarkColors = darkColorScheme(
    primary = Color(0xFF89C2FF),
    onPrimary = Color(0xFF08233D),
    primaryContainer = Color(0xFF164A78),
    onPrimaryContainer = Color(0xFFE6F7FF),
    secondary = Color(0xFFC7E4FF),
    onSecondary = Color(0xFF15324A),
    secondaryContainer = Color(0xFF213D56),
    onSecondaryContainer = Color(0xFFE6F7FF),
    tertiary = Color(0xFF87DCE8),
    onTertiary = Color(0xFF09343B),
    tertiaryContainer = Color(0xFF164F59),
    onTertiaryContainer = Color(0xFFD6F6FB),
    background = Color(0xFF07131F),
    onBackground = Color(0xFFEAF3FC),
    surface = Color(0xFF0B1724),
    onSurface = Color(0xFFEAF3FC),
    surfaceVariant = Color(0xFF17283A),
    onSurfaceVariant = Color(0xFFC6D6E6),
    outline = Color(0xFF8095AA),
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
