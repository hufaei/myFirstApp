package com.example.lifelab.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

data class AppPreferences(
    val themeMode: ThemeMode = ThemeMode.System,
    val languageMode: LanguageMode = LanguageMode.System,
    val notificationEnabled: Boolean = true,
    val defaultTaskFilterName: String = "All",
)

enum class ThemeMode {
    System,
    Light,
    Dark,
}

enum class LanguageMode {
    System,
    Zh,
    En,
}

interface AppPreferencesRepository {
    val appPreferences: Flow<AppPreferences>

    suspend fun updateThemeMode(themeMode: ThemeMode)

    suspend fun updateLanguageMode(languageMode: LanguageMode)

    suspend fun updateNotificationEnabled(enabled: Boolean)

    suspend fun updateDefaultTaskFilterName(name: String)
}

val Context.appPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
)

class DataStoreAppPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : AppPreferencesRepository {

    override val appPreferences: Flow<AppPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences ->
            AppPreferences(
                themeMode = preferences[THEME_MODE_KEY].toThemeMode(),
                languageMode = preferences[LANGUAGE_MODE_KEY].toLanguageMode(),
                notificationEnabled = preferences[NOTIFICATION_ENABLED_KEY] ?: true,
                defaultTaskFilterName = preferences[DEFAULT_TASK_FILTER_KEY] ?: "All",
            )
        }

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = themeMode.name
        }
    }

    override suspend fun updateLanguageMode(languageMode: LanguageMode) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_MODE_KEY] = languageMode.name
        }
    }

    override suspend fun updateNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATION_ENABLED_KEY] = enabled
        }
    }

    override suspend fun updateDefaultTaskFilterName(name: String) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_TASK_FILTER_KEY] = name
        }
    }

    private companion object {
        val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        val LANGUAGE_MODE_KEY = stringPreferencesKey("language_mode")
        val NOTIFICATION_ENABLED_KEY = booleanPreferencesKey("notification_enabled")
        val DEFAULT_TASK_FILTER_KEY = stringPreferencesKey("default_task_filter")
    }
}

class InMemoryAppPreferencesRepository(
    initialPreferences: AppPreferences = AppPreferences(),
) : AppPreferencesRepository {

    private val preferences = MutableStateFlow(initialPreferences)

    override val appPreferences: Flow<AppPreferences> = preferences

    override suspend fun updateThemeMode(themeMode: ThemeMode) {
        preferences.value = preferences.value.copy(themeMode = themeMode)
    }

    override suspend fun updateLanguageMode(languageMode: LanguageMode) {
        preferences.value = preferences.value.copy(languageMode = languageMode)
    }

    override suspend fun updateNotificationEnabled(enabled: Boolean) {
        preferences.value = preferences.value.copy(notificationEnabled = enabled)
    }

    override suspend fun updateDefaultTaskFilterName(name: String) {
        preferences.value = preferences.value.copy(defaultTaskFilterName = name)
    }
}

private fun String?.toThemeMode(): ThemeMode =
    enumValueOrNull<ThemeMode>(this) ?: ThemeMode.System

private fun String?.toLanguageMode(): LanguageMode =
    enumValueOrNull<LanguageMode>(this) ?: LanguageMode.System

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String?): T? =
    value?.let { name ->
        enumValues<T>().firstOrNull { enumValue -> enumValue.name == name }
    }
