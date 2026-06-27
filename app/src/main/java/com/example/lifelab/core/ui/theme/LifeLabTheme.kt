package com.example.lifelab.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.lifelab.core.datastore.ThemeMode

val LifeBlue = Color(0xFF89C2FF)
val LifeMist = Color(0xFFE6F7FF)
val LifeBlueStrong = Color(0xFF1F6FB8)
val LifeInk = Color(0xFF0D1B2A)
val LifeNavy = Color(0xFF061524)
val LifeNavySurface = Color(0xFF0D2033)

private val LifeLabLightColors = lightColorScheme(
    primary = LifeBlueStrong,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LifeBlue,
    onPrimaryContainer = Color(0xFF05213D),
    secondary = Color(0xFF4E6F8F),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LifeMist,
    onSecondaryContainer = Color(0xFF123044),
    tertiary = Color(0xFF287C89),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC9F2FA),
    onTertiaryContainer = Color(0xFF06343B),
    background = Color(0xFFF7FBFF),
    onBackground = LifeInk,
    surface = Color(0xFFFFFFFF),
    onSurface = LifeInk,
    surfaceVariant = LifeMist,
    onSurfaceVariant = Color(0xFF3E5266),
    outline = Color(0xFF7890A8),
)

private val LifeLabDarkColors = darkColorScheme(
    primary = LifeBlue,
    onPrimary = Color(0xFF08233D),
    primaryContainer = LifeBlueStrong,
    onPrimaryContainer = LifeMist,
    secondary = Color(0xFFC7E4FF),
    onSecondary = Color(0xFF15324A),
    secondaryContainer = Color(0xFF213D56),
    onSecondaryContainer = LifeMist,
    tertiary = Color(0xFF87DCE8),
    onTertiary = Color(0xFF09343B),
    tertiaryContainer = Color(0xFF164F59),
    onTertiaryContainer = Color(0xFFD6F6FB),
    background = LifeNavy,
    onBackground = Color(0xFFEAF3FC),
    surface = LifeNavySurface,
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
