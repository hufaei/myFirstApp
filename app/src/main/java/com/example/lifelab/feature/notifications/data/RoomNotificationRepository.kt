package com.example.lifelab.feature.notifications.data

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomNotificationRepository(
    private val notificationDao: NotificationDao,
    private val currentAccountId: String = "current-account",
) : NotificationRepository {

    override val messages: Flow<List<NotificationMessage>> =
        notificationDao.observeMessages()
            .onStart { seedIfEmpty() }
            .map { messages ->
                messages.map(NotificationMessageEntity::toDomain)
            }

    override val settings: Flow<NotificationSettings> =
        notificationDao.observeSettings(currentAccountId)
            .onStart { seedIfEmpty() }
            .map { settings ->
                settings?.toDomain() ?: defaultSettings()
            }

    override suspend fun getMessage(messageId: String): NotificationMessage? =
        notificationDao.getMessage(messageId)?.toDomain()

    override suspend fun updateMessageStatus(
        messageId: String,
        status: NotificationStatus,
    ): AppResult<NotificationMessage> {
        val entity = notificationDao.getMessage(messageId)
            ?: return AppResult.Failure(
                AppError.Validation(message = "未知通知消息：$messageId"),
            )

        val updatedEntity = entity.copy(status = status.name)
        notificationDao.upsertMessage(updatedEntity)
        return AppResult.Success(updatedEntity.toDomain())
    }

    override suspend fun updateSettings(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ): AppResult<NotificationSettings> {
        val updatedSettings = NotificationSettings(
            accountId = currentAccountId,
            inAppMessagesEnabled = inAppMessagesEnabled,
            systemNotificationsEnabled = systemNotificationsEnabled,
        )
        notificationDao.upsertSettings(updatedSettings.toEntity())
        return AppResult.Success(updatedSettings)
    }

    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)

    suspend fun seedIfEmpty() {
        if (notificationDao.countMessages() == 0) {
            defaultMessages().forEachIndexed { index, message ->
                notificationDao.upsertMessage(message.toEntity(sortOrder = index))
            }
        }
        if (notificationDao.getSettings(currentAccountId) == null) {
            notificationDao.upsertSettings(defaultSettings().toEntity())
        }
    }

    private fun defaultSettings(): NotificationSettings =
        NotificationSettings(
            accountId = currentAccountId,
            inAppMessagesEnabled = true,
            systemNotificationsEnabled = false,
        )

    private companion object {
        fun defaultMessages(): List<NotificationMessage> =
            listOf(
                NotificationMessage(
                    id = "welcome",
                    title = "欢迎使用 LifeLab",
                    body = "你的通知中心已准备就绪。",
                    category = "账号",
                    createdAtLabel = "今天",
                    status = NotificationStatus.Unread,
                ),
                NotificationMessage(
                    id = "weekly-summary",
                    title = "每周健康摘要",
                    body = "你的 LifeLab 每周摘要已经生成。",
                    category = "摘要",
                    createdAtLabel = "昨天",
                    status = NotificationStatus.Read,
                ),
            )
    }
}
