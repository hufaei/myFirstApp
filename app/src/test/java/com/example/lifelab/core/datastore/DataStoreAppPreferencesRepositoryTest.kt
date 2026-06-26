package com.example.lifelab.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreAppPreferencesRepositoryTest {

    @get:Rule
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher())

    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = {
                File(temporaryFolder.root, "app-preferences.preferences_pb")
            },
        )
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun emitsSystemThemeAndSystemLanguageBeforeUserSelection() = runTest {
        val repository = DataStoreAppPreferencesRepository(dataStore)

        val preferences = repository.appPreferences.first()

        assertEquals(
            AppPreferences(
                themeMode = ThemeMode.System,
                languageMode = LanguageMode.System,
            ),
            preferences,
        )
    }

    @Test
    fun persistsThemeAndLanguageSelections() = runTest {
        val repository = DataStoreAppPreferencesRepository(dataStore)

        repository.updateThemeMode(ThemeMode.Dark)
        repository.updateLanguageMode(LanguageMode.En)

        assertEquals(
            AppPreferences(
                themeMode = ThemeMode.Dark,
                languageMode = LanguageMode.En,
            ),
            repository.appPreferences.first(),
        )
    }
}
