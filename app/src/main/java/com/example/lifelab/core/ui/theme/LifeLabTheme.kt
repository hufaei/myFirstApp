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
val LifeBlueStrong = Color(0xFF0F5FA8)
val LifeInk = Color(0xFF0D1B2A)
val LifeNavy = Color(0xFF061524)
val LifeNavySurface = Color(0xFF0D2033)

private val LifeLabLightColors = lightColorScheme(
    primary = LifeBlueStrong,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LifeBlue,
    onPrimaryContainer = LifeInk,
    secondary = Color(0xFF2D6F99),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = LifeMist,
    onSecondaryContainer = LifeInk,
    tertiary = Color(0xFF245A8D),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCBE8FF),
    onTertiaryContainer = LifeInk,
    background = Color(0xFFF3FAFF),
    onBackground = LifeInk,
    surface = Color(0xFFFFFFFF),
    onSurface = LifeInk,
    surfaceVariant = Color(0xFFDDF2FF),
    onSurfaceVariant = Color(0xFF3D5266),
    outline = Color(0xFF708499),
)

private val LifeLabDarkColors = darkColorScheme(
    primary = LifeBlue,
    onPrimary = Color(0xFF00345F),
    primaryContainer = LifeBlueStrong,
    onPrimaryContainer = LifeMist,
    secondary = Color(0xFF9BD8FF),
    onSecondary = Color(0xFF00344E),
    secondaryContainer = Color(0xFF164866),
    onSecondaryContainer = LifeMist,
    tertiary = Color(0xFFB3D6FF),
    onTertiary = Color(0xFF07325F),
    tertiaryContainer = Color(0xFF254D7A),
    onTertiaryContainer = Color(0xFFD7EBFF),
    background = LifeNavy,
    onBackground = Color(0xFFE6F2FF),
    surface = LifeNavySurface,
    onSurface = Color(0xFFE6F2FF),
    surfaceVariant = Color(0xFF19314A),
    onSurfaceVariant = Color(0xFFC5D8EA),
    outline = Color(0xFF7F94A8),
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
