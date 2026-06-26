package com.example.lifelab.feature.profile.domain

import com.example.lifelab.core.datastore.ThemeMode
import kotlin.test.Test
import kotlin.test.assertEquals

class UserPreferenceTest {

    @Test
    fun defaultPreferenceUsesStableProfileDefaults() {
        val preference = UserPreference()

        assertEquals(ThemeMode.System, preference.themeMode)
        assertEquals(true, preference.notificationEnabled)
        assertEquals(DefaultTaskFilter.All, preference.defaultTaskFilter)
        assertEquals(emptyList(), preference.contentInterestTags)
    }
}
