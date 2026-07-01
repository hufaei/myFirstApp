package com.example.lifelab.feature.notifications.presentation

import com.example.lifelab.core.notifications.AndroidNotificationPermissionStatus
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
    val appNotificationPreferenceEnabled: Boolean = true,
    val androidPermissionStatus: AndroidNotificationPermissionStatus = AndroidNotificationPermissionStatus.NotRequired,
) {
    val habitReminderDeliveryStatus: HabitReminderDeliveryStatus
        get() = when (androidPermissionStatus) {
            AndroidNotificationPermissionStatus.Blocked -> HabitReminderDeliveryStatus.BlockedByAndroidPermission
            AndroidNotificationPermissionStatus.Granted,
            AndroidNotificationPermissionStatus.NotRequired,
            -> HabitReminderDeliveryStatus.ControlledFromHabits
        }

    val habitReminderDeliveryBlocked: Boolean
        get() = habitReminderDeliveryStatus == HabitReminderDeliveryStatus.BlockedByAndroidPermission

    val canRequestAndroidNotificationPermission: Boolean
        get() = androidPermissionStatus == AndroidNotificationPermissionStatus.Blocked
}

enum class HabitReminderDeliveryStatus {
    ControlledFromHabits,
    BlockedByAndroidPermission,
}

sealed interface NotificationsUiEvent {
    data class MarkRead(val messageId: String) : NotificationsUiEvent
    data class Archive(val messageId: String) : NotificationsUiEvent
    data class SetInAppMessagesEnabled(val enabled: Boolean) : NotificationsUiEvent
    data object RefreshSystemNotificationPermission : NotificationsUiEvent
    data object RetryRefresh : NotificationsUiEvent
}
