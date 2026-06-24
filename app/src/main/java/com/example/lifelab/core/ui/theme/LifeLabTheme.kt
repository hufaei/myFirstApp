package com.example.lifelab.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LifeLabLightColors = lightColorScheme()

@Composable
fun LifeLabTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LifeLabLightColors,
        content = content,
    )
}
