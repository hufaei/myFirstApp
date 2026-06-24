package com.example.lifelab.feature.profile.data

import com.example.lifelab.feature.profile.domain.DefaultTaskFilter
import com.example.lifelab.feature.profile.domain.ProfileSession
import com.example.lifelab.feature.profile.domain.ProfileUser
import com.example.lifelab.feature.profile.domain.ThemeMode
import com.example.lifelab.feature.profile.domain.UserPreference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

class InMemoryProfileRepositoryTest {

    @Test
    fun updatingThemeModeKeepsPreviousPreferenceImmutableAndOnlyChangesTheme() {
        val initialPreference = UserPreference(
            themeMode = ThemeMode.System,
            notificationEnabled = true,
            defaultTaskFilter = DefaultTaskFilter.Active,
            contentInterestTags = listOf("focus", "reading"),
        )
        val repository = InMemoryProfileRepository(initialPreference = initialPreference)

        val updatedState = repository.updateThemeMode(ThemeMode.Dark)

        assertEquals(ThemeMode.System, initialPreference.themeMode)
        assertNotSame(initialPreference, updatedState.preference)
        assertEquals(ThemeMode.Dark, updatedState.preference.themeMode)
        assertEquals(true, updatedState.preference.notificationEnabled)
        assertEquals(DefaultTaskFilter.Active, updatedState.preference.defaultTaskFilter)
        assertEquals(listOf("focus", "reading"), updatedState.preference.contentInterestTags)
    }

    @Test
    fun updatingTaskFilterKeepsPreviousPreferenceImmutableAndOnlyChangesFilter() {
        val initialPreference = UserPreference(
            themeMode = ThemeMode.Light,
            notificationEnabled = false,
            defaultTaskFilter = DefaultTaskFilter.All,
            contentInterestTags = listOf("habit"),
        )
        val repository = InMemoryProfileRepository(initialPreference = initialPreference)

        val updatedState = repository.updateDefaultTaskFilter(DefaultTaskFilter.Completed)

        assertEquals(DefaultTaskFilter.All, initialPreference.defaultTaskFilter)
        assertNotSame(initialPreference, updatedState.preference)
        assertEquals(ThemeMode.Light, updatedState.preference.themeMode)
        assertEquals(false, updatedState.preference.notificationEnabled)
        assertEquals(DefaultTaskFilter.Completed, updatedState.preference.defaultTaskFilter)
        assertEquals(listOf("habit"), updatedState.preference.contentInterestTags)
    }

    @Test
    fun repositoryExposesSignedInStateWhenSeededWithUserSession() {
        val user = ProfileUser(
            id = "user-2",
            displayName = "Morgan Lee",
            email = "morgan@example.com",
            membershipLabel = "Starter",
            avatarInitial = "M",
        )
        val repository = InMemoryProfileRepository(
            initialSession = ProfileSession.SignedIn(user),
        )

        val state = repository.getProfileState()

        assertEquals(ProfileSession.SignedIn(user), state.session)
    }
}
