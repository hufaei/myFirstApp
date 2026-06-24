package com.example.lifelab.feature.notifications.domain

import com.example.lifelab.core.common.AppResult
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val messages: Flow<List<NotificationMessage>>
    val settings: Flow<NotificationSettings>

    suspend fun getMessage(messageId: String): NotificationMessage?
    suspend fun updateMessageStatus(
        messageId: String,
        status: NotificationStatus,
    ): AppResult<NotificationMessage>

    suspend fun updateSettings(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ): AppResult<NotificationSettings>

    suspend fun refresh(): AppResult<Unit>
}
