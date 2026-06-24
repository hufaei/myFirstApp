package com.example.lifelab.feature.profile.presentation

import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.profile.data.InMemoryProfileRepository
import com.example.lifelab.feature.profile.domain.DefaultTaskFilter
import com.example.lifelab.feature.profile.domain.ProfileSession
import com.example.lifelab.feature.profile.domain.ProfileUser
import com.example.lifelab.feature.profile.domain.ThemeMode
import com.example.lifelab.feature.profile.domain.UserPreference
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProfileViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadShowsGuestOverviewAndDefaultPreference() = runTest {
        val viewModel = ProfileViewModel()

        val state = viewModel.uiState.value

        assertFalse(state.isLoading)
        assertEquals("Guest", state.overview.displayName)
        assertEquals("Sign in to sync your LifeLab profile.", state.overview.description)
        assertNull(state.overview.email)
        assertNull(state.overview.membershipLabel)
        assertEquals("G", state.overview.avatarInitial)
        assertEquals(ThemeMode.System, state.preference.themeMode)
        assertTrue(state.preference.notificationEnabled)
        assertEquals(DefaultTaskFilter.All, state.preference.defaultTaskFilter)
        assertEquals(emptyList(), state.preference.contentInterestTags)
    }

    @Test
    fun initialLoadMapsSignedInSessionToOverview() = runTest {
        val user = ProfileUser(
            id = "user-3",
            displayName = "Taylor Kim",
            email = "taylor@example.com",
            membershipLabel = "Pro member",
            avatarInitial = "T",
        )
        val repository = InMemoryProfileRepository(
            initialSession = ProfileSession.SignedIn(user),
        )
        val viewModel = ProfileViewModel(repository)

        val state = viewModel.uiState.value

        assertEquals("Taylor Kim", state.overview.displayName)
        assertEquals("Pro member", state.overview.description)
        assertEquals("taylor@example.com", state.overview.email)
        assertEquals("Pro member", state.overview.membershipLabel)
        assertEquals("T", state.overview.avatarInitial)
    }

    @Test
    fun themeModeSelectedUpdatesThemeMode() = runTest {
        val viewModel = ProfileViewModel()

        viewModel.onEvent(ProfileUiEvent.ThemeModeSelected(ThemeMode.Dark))

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.preference.themeMode)
    }

    @Test
    fun notificationEnabledChangedUpdatesNotificationEnabled() = runTest {
        val viewModel = ProfileViewModel()

        viewModel.onEvent(ProfileUiEvent.NotificationEnabledChanged(false))

        assertFalse(viewModel.uiState.value.preference.notificationEnabled)
    }

    @Test
    fun defaultTaskFilterSelectedUpdatesDefaultTaskFilter() = runTest {
        val viewModel = ProfileViewModel()

        viewModel.onEvent(ProfileUiEvent.DefaultTaskFilterSelected(DefaultTaskFilter.Completed))

        assertEquals(
            DefaultTaskFilter.Completed,
            viewModel.uiState.value.preference.defaultTaskFilter,
        )
    }

    @Test
    fun contentInterestTagsChangedReplacesContentInterestTags() = runTest {
        val repository = InMemoryProfileRepository(
            initialPreference = UserPreference(contentInterestTags = listOf("old")),
        )
        val viewModel = ProfileViewModel(repository)

        viewModel.onEvent(ProfileUiEvent.ContentInterestTagsChanged(listOf("focus", "reading")))

        assertEquals(
            listOf("focus", "reading"),
            viewModel.uiState.value.preference.contentInterestTags,
        )
    }
}
