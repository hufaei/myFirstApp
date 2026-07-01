package com.example.lifelab.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.AppPreferencesRepository
import com.example.lifelab.core.notifications.AndroidNotificationPermissionStatusReader
import com.example.lifelab.feature.notifications.domain.ChangeMessageStatusUseCase
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus
import com.example.lifelab.feature.notifications.domain.UpdateNotificationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationRepository,
    private val appPreferencesRepository: AppPreferencesRepository? = null,
    private val notificationPermissionStatusReader: AndroidNotificationPermissionStatusReader =
        AndroidNotificationPermissionStatusReader(
            com.example.lifelab.core.notifications.AndroidNotificationPermissionStatus.NotRequired,
        ),
) : ViewModel() {

    private val changeMessageStatus = ChangeMessageStatusUseCase(repository)
    private val updateNotificationSettings = UpdateNotificationSettingsUseCase(repository)
    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    private var latestSettings: NotificationSettings? = null
    private var latestAppNotificationPreferenceEnabled: Boolean = true

    init {
        observeRepository()
        refresh()
    }

    fun onEvent(event: NotificationsUiEvent) {
        when (event) {
            is NotificationsUiEvent.MarkRead -> changeStatus(event.messageId, NotificationStatus.Read)
            is NotificationsUiEvent.Archive -> changeStatus(event.messageId, NotificationStatus.Archived)
            is NotificationsUiEvent.SetInAppMessagesEnabled -> updateSettings(
                inAppMessagesEnabled = event.enabled,
                systemNotificationsEnabled = latestSettings?.systemNotificationsEnabled ?: false,
            )
            NotificationsUiEvent.RefreshSystemNotificationPermission -> refreshSystemIntegrationState()
            NotificationsUiEvent.RetryRefresh -> refresh()
        }
    }

    private fun observeRepository() {
        viewModelScope.launch {
            val appPreferences = appPreferencesRepository?.appPreferences ?: flowOf(AppPreferences())
            combine(repository.messages, repository.settings, appPreferences) { messages, settings, preferences ->
                latestAppNotificationPreferenceEnabled = preferences.notificationEnabled
                val effectiveSettings = settings.copy(
                    inAppMessagesEnabled = settings.inAppMessagesEnabled && preferences.notificationEnabled,
                )
                val visibleMessages = if (effectiveSettings.inAppMessagesEnabled) {
                    messages.filterNot { it.status == NotificationStatus.Archived }
                } else {
                    emptyList()
                }
                settings to (effectiveSettings to visibleMessages)
            }.collect { (baseSettings, effectiveState) ->
                val (effectiveSettings, visibleMessages) = effectiveState
                latestSettings = baseSettings
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        messages = visibleMessages,
                        settings = effectiveSettings,
                        systemIntegration = toSystemIntegrationUiState(),
                    )
                }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.refresh()) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, errorMessage = null) }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.error.displayMessage(),
                    )
                }
            }
        }
    }

    private fun changeStatus(
        messageId: String,
        status: NotificationStatus,
    ) {
        viewModelScope.launch {
            when (val result = changeMessageStatus(messageId, status)) {
                is AppResult.Success -> _uiState.update { it.copy(errorMessage = null) }
                is AppResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.displayMessage()) }
            }
        }
    }

    private fun updateSettings(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ) {
        viewModelScope.launch {
            when (
                val result = updateNotificationSettings(
                    inAppMessagesEnabled = inAppMessagesEnabled,
                    systemNotificationsEnabled = systemNotificationsEnabled,
                )
            ) {
                is AppResult.Success -> {
                    latestSettings = result.value
                    _uiState.update {
                        val effectiveSettings = result.value.copy(
                            inAppMessagesEnabled = result.value.inAppMessagesEnabled &&
                                latestAppNotificationPreferenceEnabled,
                        )
                        it.copy(
                            settings = effectiveSettings,
                            systemIntegration = toSystemIntegrationUiState(),
                            errorMessage = null,
                        )
                    }
                }
                is AppResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.displayMessage()) }
            }
        }
    }

    private fun refreshSystemIntegrationState() {
        _uiState.update { current ->
            current.copy(systemIntegration = toSystemIntegrationUiState())
        }
    }

    private fun toSystemIntegrationUiState(): SystemNotificationIntegrationUiState =
        SystemNotificationIntegrationUiState(
            appNotificationPreferenceEnabled = latestAppNotificationPreferenceEnabled,
            androidPermissionStatus = notificationPermissionStatusReader.currentStatus(),
        )
}

private fun AppError.displayMessage(): String = message
