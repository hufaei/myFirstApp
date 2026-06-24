package com.example.lifelab.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LifeLabLightColors = lightColorScheme(
    primary = LifeLabColorTokens.SkyPrimary,
    onPrimary = LifeLabColorTokens.Ink,
    primaryContainer = LifeLabColorTokens.IcePanel,
    onPrimaryContainer = LifeLabColorTokens.Ink,
    secondary = LifeLabColorTokens.SkyDeep,
    onSecondary = Color.White,
    secondaryContainer = LifeLabColorTokens.CloudPanel,
    onSecondaryContainer = LifeLabColorTokens.Ink,
    tertiary = LifeLabColorTokens.Success,
    background = LifeLabColorTokens.SnowBackground,
    onBackground = LifeLabColorTokens.Ink,
    surface = Color.White,
    onSurface = LifeLabColorTokens.Ink,
    surfaceVariant = LifeLabColorTokens.CloudPanel,
    onSurfaceVariant = LifeLabColorTokens.InkSoft,
    outline = LifeLabColorTokens.FogLine,
    outlineVariant = LifeLabColorTokens.FogLine,
    error = Color(0xFFB3261E),
)

@Composable
fun LifeLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LifeLabLightColors,
        typography = LifeLabTypography,
        shapes = LifeLabShapes,
        content = content,
    )
}
