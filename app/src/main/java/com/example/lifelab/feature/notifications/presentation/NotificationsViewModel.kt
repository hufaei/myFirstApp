package com.example.lifelab.feature.notifications.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.notifications.domain.ChangeMessageStatusUseCase
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus
import com.example.lifelab.feature.notifications.domain.UpdateNotificationSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationsViewModel(
    private val repository: NotificationRepository,
    private val changeMessageStatus: ChangeMessageStatusUseCase = ChangeMessageStatusUseCase(repository),
    private val updateNotificationSettings: UpdateNotificationSettingsUseCase =
        UpdateNotificationSettingsUseCase(repository),
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()
    private var latestSettings: NotificationSettings? = null

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
            is NotificationsUiEvent.SetSystemNotificationsEnabled -> updateSettings(
                inAppMessagesEnabled = latestSettings?.inAppMessagesEnabled ?: true,
                systemNotificationsEnabled = event.enabled,
            )
            NotificationsUiEvent.RetryRefresh -> refresh()
        }
    }

    private fun observeRepository() {
        viewModelScope.launch {
            combine(repository.messages, repository.settings) { messages, settings ->
                val visibleMessages = if (settings.inAppMessagesEnabled) {
                    messages.filterNot { it.status == NotificationStatus.Archived }
                } else {
                    emptyList()
                }
                settings to visibleMessages
            }.collect { (settings, visibleMessages) ->
                latestSettings = settings
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        messages = visibleMessages,
                        settings = settings,
                        systemIntegration = settings.toSystemIntegrationUiState(),
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
                        it.copy(
                            settings = result.value,
                            systemIntegration = result.value.toSystemIntegrationUiState(),
                            errorMessage = null,
                        )
                    }
                }
                is AppResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.displayMessage()) }
            }
        }
    }
}

private fun NotificationSettings.toSystemIntegrationUiState(): SystemNotificationIntegrationUiState =
    SystemNotificationIntegrationUiState(
        enabled = systemNotificationsEnabled,
        statusLabel = if (systemNotificationsEnabled) {
            "System notifications enabled"
        } else {
            "System notifications disabled"
        },
    )

private fun AppError.displayMessage(): String = message
