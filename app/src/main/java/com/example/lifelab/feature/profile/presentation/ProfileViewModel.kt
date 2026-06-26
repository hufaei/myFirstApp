package com.example.lifelab.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.AppPreferencesRepository
import com.example.lifelab.core.datastore.InMemoryAppPreferencesRepository
import com.example.lifelab.core.datastore.LanguageMode
import com.example.lifelab.core.datastore.ThemeMode
import com.example.lifelab.feature.profile.data.InMemoryProfileRepository
import com.example.lifelab.feature.profile.domain.DefaultTaskFilter
import com.example.lifelab.feature.profile.domain.ProfileOverview
import com.example.lifelab.feature.profile.domain.ProfileRepository
import com.example.lifelab.feature.profile.domain.ProfileSession
import com.example.lifelab.feature.profile.domain.ProfileState
import com.example.lifelab.feature.profile.domain.UserPreference
import com.example.lifelab.feature.profile.domain.toOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val overview: ProfileOverview = ProfileSession.Guest.toOverview(),
    val preference: UserPreference = UserPreference(),
    val appPreferences: AppPreferences = AppPreferences(),
)

sealed interface ProfileUiEvent {
    data class ThemeModeSelected(
        val themeMode: ThemeMode,
    ) : ProfileUiEvent

    data class LanguageModeSelected(
        val languageMode: LanguageMode,
    ) : ProfileUiEvent

    data class NotificationEnabledChanged(
        val enabled: Boolean,
    ) : ProfileUiEvent

    data class DefaultTaskFilterSelected(
        val defaultTaskFilter: DefaultTaskFilter,
    ) : ProfileUiEvent

    data class ContentInterestTagsChanged(
        val tags: List<String>,
    ) : ProfileUiEvent
}

class ProfileViewModel(
    private val repository: ProfileRepository = InMemoryProfileRepository(),
    private val appPreferencesRepository: AppPreferencesRepository = InMemoryAppPreferencesRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { currentState ->
                repository.getProfileState().toUiState(
                    appPreferences = currentState.appPreferences,
                )
            }
        }
        viewModelScope.launch {
            appPreferencesRepository.appPreferences.collect { appPreferences ->
                _uiState.update { currentState ->
                    currentState.copy(
                        preference = currentState.preference.copy(
                            notificationEnabled = appPreferences.notificationEnabled,
                            defaultTaskFilter = appPreferences.defaultTaskFilter(),
                        ),
                        appPreferences = appPreferences,
                    )
                }
            }
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        viewModelScope.launch {
            val profileState = when (event) {
                is ProfileUiEvent.ThemeModeSelected -> {
                    appPreferencesRepository.updateThemeMode(event.themeMode)
                    repository.getProfileState()
                }

                is ProfileUiEvent.LanguageModeSelected -> {
                    appPreferencesRepository.updateLanguageMode(event.languageMode)
                    repository.getProfileState()
                }

                is ProfileUiEvent.NotificationEnabledChanged -> {
                    appPreferencesRepository.updateNotificationEnabled(event.enabled)
                    repository.updateNotificationEnabled(event.enabled)
                }

                is ProfileUiEvent.DefaultTaskFilterSelected -> {
                    appPreferencesRepository.updateDefaultTaskFilterName(event.defaultTaskFilter.name)
                    repository.updateDefaultTaskFilter(event.defaultTaskFilter)
                }

                is ProfileUiEvent.ContentInterestTagsChanged -> {
                    repository.updateContentInterestTags(event.tags)
                }
            }

            _uiState.update { currentState ->
                profileState.toUiState(appPreferences = currentState.appPreferences)
            }
        }
    }
}

private fun AppPreferences.defaultTaskFilter(): DefaultTaskFilter =
    enumValues<DefaultTaskFilter>().firstOrNull { it.name == defaultTaskFilterName }
        ?: DefaultTaskFilter.All

private fun ProfileState.toUiState(
    appPreferences: AppPreferences,
): ProfileUiState =
    ProfileUiState(
        isLoading = false,
        overview = session.toOverview(),
        preference = preference.copy(
            notificationEnabled = appPreferences.notificationEnabled,
            defaultTaskFilter = appPreferences.defaultTaskFilter(),
        ),
        appPreferences = appPreferences,
    )
