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
            ?: return validationFailure("未知通知消息：$messageId")

        return when {
            message.status == targetStatus -> AppResult.Success(message)
            message.status == NotificationStatus.Archived && targetStatus == NotificationStatus.Read ->
                validationFailure("已归档的通知不能标记为已读。")
            targetStatus == NotificationStatus.Unread ->
                validationFailure("通知不能恢复为未读。")
            targetStatus == NotificationStatus.Read && message.status == NotificationStatus.Unread ->
                repository.updateMessageStatus(messageId, NotificationStatus.Read)
            targetStatus == NotificationStatus.Archived ->
                repository.updateMessageStatus(messageId, NotificationStatus.Archived)
            else -> validationFailure("不支持这个通知状态变更。")
        }
    }

    private fun validationFailure(message: String): AppResult.Failure =
        AppResult.Failure(AppError.Validation(message = message))
}
