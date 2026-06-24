package com.example.lifelab.feature.notifications.domain

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult

class ChangeMessageStatusUseCase(
    private val repository: NotificationRepository,
) {

    suspend operator fun invoke(
        messageId: String,
        targetStatus: NotificationStatus,
    ): AppResult<NotificationMessage> {
        val message = repository.getMessage(messageId)
            ?: return validationFailure("Unknown notification message id: $messageId")

        return when {
            message.status == targetStatus -> AppResult.Success(message)
            message.status == NotificationStatus.Archived && targetStatus == NotificationStatus.Read ->
                validationFailure("Archived notification messages cannot be marked read.")
            targetStatus == NotificationStatus.Unread ->
                validationFailure("Notification messages cannot be moved back to unread.")
            targetStatus == NotificationStatus.Read && message.status == NotificationStatus.Unread ->
                repository.updateMessageStatus(messageId, NotificationStatus.Read)
            targetStatus == NotificationStatus.Archived ->
                repository.updateMessageStatus(messageId, NotificationStatus.Archived)
            else -> validationFailure("Unsupported notification status transition.")
        }
    }

    private fun validationFailure(message: String): AppResult.Failure =
        AppResult.Failure(AppError.Validation(message = message))
}
