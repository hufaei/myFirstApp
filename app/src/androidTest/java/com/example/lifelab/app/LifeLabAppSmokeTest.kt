package com.example.lifelab.app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LifeLabAppSmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun appShellShowsNewNavigation() {
        composeRule.onNodeWithText("Today").assertIsDisplayed()
        composeRule.onNodeWithText("Workbench").assertIsDisplayed()
        composeRule.onNodeWithText("Discover").assertIsDisplayed()
        composeRule.onNodeWithText("Me").assertIsDisplayed()
    }

    @Test
    fun appShellShowsTodayFocusSection() {
        composeRule.onNodeWithText("Today focus").assertIsDisplayed()
    }
}
