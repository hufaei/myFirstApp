package com.example.lifelab.feature.profile.presentation

import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.AppPreferencesRepository
import com.example.lifelab.core.datastore.LanguageMode
import com.example.lifelab.core.datastore.ThemeMode
import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.profile.data.InMemoryProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class ProfileAppPreferencesTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadShowsPersistedThemeAndLanguagePreference() = runTest {
        val appPreferencesRepository = FakeAppPreferencesRepository(
            AppPreferences(
                themeMode = ThemeMode.Dark,
                languageMode = LanguageMode.Zh,
            ),
        )
        val viewModel = ProfileViewModel(
            repository = InMemoryProfileRepository(),
            appPreferencesRepository = appPreferencesRepository,
        )

        assertEquals(
            AppPreferences(
                themeMode = ThemeMode.Dark,
                languageMode = LanguageMode.Zh,
            ),
            viewModel.uiState.value.appPreferences,
        )
    }

    @Test
    fun themeModeSelectedPersistsAppThemePreference() = runTest {
        val appPreferencesRepository = FakeAppPreferencesRepository()
        val viewModel = ProfileViewModel(
            repository = InMemoryProfileRepository(),
            appPreferencesRepository = appPreferencesRepository,
        )

        viewModel.onEvent(ProfileUiEvent.ThemeModeSelected(ThemeMode.Light))

        assertEquals(ThemeMode.Light, appPreferencesRepository.preferences.value.themeMode)
        assertEquals(ThemeMode.Light, viewModel.uiState.value.appPreferences.themeMode)
    }

    @Test
    fun languageModeSelectedPersistsAppLanguagePreference() = runTest {
        val appPreferencesRepository = FakeAppPreferencesRepository()
        val viewModel = ProfileViewModel(
            repository = InMemoryProfileRepository(),
            appPreferencesRepository = appPreferencesRepository,
        )

        viewModel.onEvent(ProfileUiEvent.LanguageModeSelected(LanguageMode.En))

        assertEquals(LanguageMode.En, appPreferencesRepository.preferences.value.languageMode)
        assertEquals(LanguageMode.En, viewModel.uiState.value.appPreferences.languageMode)
    }
}

private class FakeAppPreferencesRepository(
    initialPreferences: AppPreferences = AppPreferences(),
) : AppPreferencesRepository {

    val preferences = MutableStateFlow(initialPreferences)

    override val appPreferences: Flow<AppPreferences> = preferences

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        preferences.value = preferences.value.copy(themeMode = themeMode)
    }

    override suspend fun updateLanguageMode(languageMode: LanguageMode) {
        preferences.value = preferences.value.copy(languageMode = languageMode)
    }
}
