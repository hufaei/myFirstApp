package com.example.lifelab.feature.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.feature.profile.data.InMemoryProfileRepository
import com.example.lifelab.feature.profile.domain.DefaultTaskFilter
import com.example.lifelab.feature.profile.domain.ProfileOverview
import com.example.lifelab.feature.profile.domain.ProfileRepository
import com.example.lifelab.feature.profile.domain.ProfileSession
import com.example.lifelab.feature.profile.domain.ProfileState
import com.example.lifelab.feature.profile.domain.ThemeMode
import com.example.lifelab.feature.profile.domain.UserPreference
import com.example.lifelab.feature.profile.domain.toOverview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val overview: ProfileOverview = ProfileSession.Guest.toOverview(),
    val preference: UserPreference = UserPreference(),
)

sealed interface ProfileUiEvent {
    data class ThemeModeSelected(
        val themeMode: ThemeMode,
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.value = repository.getProfileState().toUiState()
        }
    }

    fun onEvent(event: ProfileUiEvent) {
        viewModelScope.launch {
            val profileState = when (event) {
                is ProfileUiEvent.ThemeModeSelected -> {
                    repository.updateThemeMode(event.themeMode)
                }

                is ProfileUiEvent.NotificationEnabledChanged -> {
                    repository.updateNotificationEnabled(event.enabled)
                }

                is ProfileUiEvent.DefaultTaskFilterSelected -> {
                    repository.updateDefaultTaskFilter(event.defaultTaskFilter)
                }

                is ProfileUiEvent.ContentInterestTagsChanged -> {
                    repository.updateContentInterestTags(event.tags)
                }
            }

            _uiState.value = profileState.toUiState()
        }
    }
}

private fun ProfileState.toUiState(): ProfileUiState =
    ProfileUiState(
        isLoading = false,
        overview = session.toOverview(),
        preference = preference,
    )
