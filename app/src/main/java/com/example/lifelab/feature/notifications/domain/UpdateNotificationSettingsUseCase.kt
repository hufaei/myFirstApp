package com.example.lifelab.feature.notifications.domain

import com.example.lifelab.core.common.AppResult

class UpdateNotificationSettingsUseCase(
    private val repository: NotificationRepository,
) {

    suspend operator fun invoke(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ): AppResult<NotificationSettings> = repository.updateSettings(
        inAppMessagesEnabled = inAppMessagesEnabled,
        systemNotificationsEnabled = systemNotificationsEnabled,
    )
}
