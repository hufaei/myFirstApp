package com.example.lifelab.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.lifelab.core.datastore.ThemeMode

val LifeSage = Color(0xFF2F6658)
val LifeSageContainer = Color(0xFFD8EADF)
val LifeLavender = Color(0xFF5F5B7A)
val LifeClay = Color(0xFF725D3A)
val LifeInk = Color(0xFF17211D)
val LifeDark = Color(0xFF0F1714)
val LifeDarkSurface = Color(0xFF17231F)

private val LifeLabLightColors = lightColorScheme(
    primary = LifeSage,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = LifeSageContainer,
    onPrimaryContainer = Color(0xFF0F2A22),
    secondary = LifeLavender,
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E1F0),
    onSecondaryContainer = Color(0xFF24213A),
    tertiary = LifeClay,
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF0E1C8),
    onTertiaryContainer = Color(0xFF2A1F0F),
    background = Color(0xFFF7F8F4),
    onBackground = LifeInk,
    surface = Color(0xFFFEFFFB),
    onSurface = LifeInk,
    surfaceVariant = Color(0xFFE7ECE5),
    onSurfaceVariant = Color(0xFF46524B),
    outline = Color(0xFF747D75),
)

private val LifeLabDarkColors = darkColorScheme(
    primary = Color(0xFFA9D8C2),
    onPrimary = Color(0xFF12362C),
    primaryContainer = Color(0xFF244D42),
    onPrimaryContainer = Color(0xFFD8EADF),
    secondary = Color(0xFFCEC7EA),
    onSecondary = Color(0xFF302B4A),
    secondaryContainer = Color(0xFF46405F),
    onSecondaryContainer = Color(0xFFE9E4F8),
    tertiary = Color(0xFFE4C891),
    onTertiary = Color(0xFF3D2B0F),
    tertiaryContainer = Color(0xFF57411D),
    onTertiaryContainer = Color(0xFFF4E3C1),
    background = LifeDark,
    onBackground = Color(0xFFE8F0EA),
    surface = LifeDarkSurface,
    onSurface = Color(0xFFE8F0EA),
    surfaceVariant = Color(0xFF273630),
    onSurfaceVariant = Color(0xFFC7D2CB),
    outline = Color(0xFF8B978F),
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
