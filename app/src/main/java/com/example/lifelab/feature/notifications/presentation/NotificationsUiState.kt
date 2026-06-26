package com.example.lifelab.feature.notifications.presentation

import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationSettings

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val messages: List<NotificationMessage> = emptyList(),
    val settings: NotificationSettings? = null,
    val systemIntegration: SystemNotificationIntegrationUiState = SystemNotificationIntegrationUiState(),
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = !isLoading && errorMessage == null && (
            settings?.inAppMessagesEnabled == false || messages.isEmpty()
            )
}

data class SystemNotificationIntegrationUiState(
    val enabled: Boolean = false,
    val statusLabel: String = "系统通知已关闭",
)

sealed interface NotificationsUiEvent {
    data class MarkRead(val messageId: String) : NotificationsUiEvent
    data class Archive(val messageId: String) : NotificationsUiEvent
    data class SetInAppMessagesEnabled(val enabled: Boolean) : NotificationsUiEvent
    data class SetSystemNotificationsEnabled(val enabled: Boolean) : NotificationsUiEvent
    data object RetryRefresh : NotificationsUiEvent
}
