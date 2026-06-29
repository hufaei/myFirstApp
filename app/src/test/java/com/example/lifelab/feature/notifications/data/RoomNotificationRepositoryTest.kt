package com.example.lifelab.feature.notifications.data

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RoomNotificationRepositoryTest {

    @Test
    fun updateMessageStatusPersistsUpdatedMessage() = runTest {
        val dao = FakeNotificationDao(
            messages = listOf(
                NotificationMessageEntity(
                    id = "welcome",
                    title = "Welcome",
                    body = "Ready",
                    category = "Account",
                    createdAtLabel = "Today",
                    status = NotificationStatus.Unread.name,
                    sortOrder = 0,
                ),
            ),
        )
        val repository = RoomNotificationRepository(dao)

        val result = repository.updateMessageStatus("welcome", NotificationStatus.Read)

        val updated = assertIs<AppResult.Success<NotificationMessage>>(result).value
        assertEquals(NotificationStatus.Read, updated.status)
        assertEquals(NotificationStatus.Read, repository.messages.first().single().status)
    }

    @Test
    fun updateSettingsUpsertsCurrentAccountSettings() = runTest {
        val dao = FakeNotificationDao()
        val repository = RoomNotificationRepository(dao, currentAccountId = "account-1")

        val result = repository.updateSettings(
            inAppMessagesEnabled = false,
            systemNotificationsEnabled = true,
        )

        val settings = assertIs<AppResult.Success<NotificationSettings>>(result).value
        assertEquals("account-1", settings.accountId)
        assertEquals(settings, repository.settings.first())
    }

    @Test
    fun missingMessageStatusUpdateReturnsValidationFailure() = runTest {
        val repository = RoomNotificationRepository(FakeNotificationDao())

        val result = repository.updateMessageStatus("missing", NotificationStatus.Read)

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
    }

    private class FakeNotificationDao(
        messages: List<NotificationMessageEntity> = emptyList(),
    ) : NotificationDao {
        private val messageEntities = MutableStateFlow(messages)
        private val settingEntities = MutableStateFlow(emptyList<NotificationSettingsEntity>())

        override fun observeMessages(): Flow<List<NotificationMessageEntity>> = messageEntities

        override suspend fun getMessages(): List<NotificationMessageEntity> = messageEntities.value

        override suspend fun getMessage(id: String): NotificationMessageEntity? =
            messageEntities.value.firstOrNull { it.id == id }

        override suspend fun upsertMessage(message: NotificationMessageEntity) {
            messageEntities.value = messageEntities.value.filterNot { it.id == message.id } + message
        }

        override fun observeSettings(accountId: String): Flow<NotificationSettingsEntity?> =
            settingEntities.map { settings ->
                settings.firstOrNull { it.accountId == accountId }
            }

        override suspend fun getSettings(accountId: String): NotificationSettingsEntity? =
            settingEntities.value.firstOrNull { it.accountId == accountId }

        override suspend fun upsertSettings(settings: NotificationSettingsEntity) {
            settingEntities.value = settingEntities.value.filterNot {
                it.accountId == settings.accountId
            } + settings
        }

        override suspend fun countMessages(): Int = messageEntities.value.size
    }
}
