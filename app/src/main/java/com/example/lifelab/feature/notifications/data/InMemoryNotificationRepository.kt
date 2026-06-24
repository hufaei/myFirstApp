package com.example.lifelab.feature.notifications.data

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryNotificationRepository(
    private val currentAccountId: String = "current-account",
    messages: List<NotificationMessage> = defaultMessages(),
    settings: NotificationSettings = NotificationSettings(
        accountId = currentAccountId,
        inAppMessagesEnabled = true,
        systemNotificationsEnabled = false,
    ),
) : NotificationRepository {

    private val messageState = MutableStateFlow(messages)
    private val settingsState = MutableStateFlow(settings.copy(accountId = currentAccountId))

    override val messages: Flow<List<NotificationMessage>> = messageState.asStateFlow()
    override val settings: Flow<NotificationSettings> = settingsState.asStateFlow()

    override suspend fun getMessage(messageId: String): NotificationMessage? =
        messageState.value.firstOrNull { it.id == messageId }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: NotificationStatus,
    ): AppResult<NotificationMessage> {
        val currentMessages = messageState.value
        val messageIndex = currentMessages.indexOfFirst { it.id == messageId }
        if (messageIndex == -1) {
            return AppResult.Failure(
                AppError.Validation(message = "Unknown notification message id: $messageId"),
            )
        }

        val updatedMessage = currentMessages[messageIndex].copy(status = status)
        messageState.value = currentMessages.mapIndexed { index, message ->
            if (index == messageIndex) updatedMessage else message
        }
        return AppResult.Success(updatedMessage)
    }

    override suspend fun updateSettings(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ): AppResult<NotificationSettings> {
        val updatedSettings = settingsState.value.copy(
            accountId = currentAccountId,
            inAppMessagesEnabled = inAppMessagesEnabled,
            systemNotificationsEnabled = systemNotificationsEnabled,
        )
        settingsState.value = updatedSettings
        return AppResult.Success(updatedSettings)
    }

    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)
}

private fun defaultMessages(): List<NotificationMessage> = listOf(
    NotificationMessage(
        id = "welcome",
        title = "Welcome to LifeLab",
        body = "Your notification center is ready.",
        category = "Account",
        createdAtLabel = "Today",
        status = NotificationStatus.Unread,
    ),
    NotificationMessage(
        id = "weekly-summary",
        title = "Weekly health summary",
        body = "Your weekly LifeLab summary is available.",
        category = "Summary",
        createdAtLabel = "Yesterday",
        status = NotificationStatus.Read,
    ),
)
