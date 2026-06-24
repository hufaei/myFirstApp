package com.example.lifelab.core.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals

class LifeLabThemeTokensTest {

    @Test
    fun usesApprovedBlueWhitePalette() {
        assertEquals(Color(0xFF89C2FF), LifeLabColorTokens.SkyPrimary)
        assertEquals(Color(0xFFE6F7FF), LifeLabColorTokens.SnowBackground)
        assertEquals(Color(0xFF18324A), LifeLabColorTokens.Ink)
    }
}
