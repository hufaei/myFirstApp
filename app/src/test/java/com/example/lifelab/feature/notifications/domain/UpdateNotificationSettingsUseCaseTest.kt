package com.example.lifelab.feature.notifications.domain

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.notifications.data.InMemoryNotificationRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class UpdateNotificationSettingsUseCaseTest {

    @Test
    fun updatesInAppAndSystemNotificationSettings() = runBlocking {
        val repository = InMemoryNotificationRepository(
            currentAccountId = "account-123",
            settings = NotificationSettings(
                accountId = "account-123",
                inAppMessagesEnabled = true,
                systemNotificationsEnabled = false,
            ),
        )
        val useCase = UpdateNotificationSettingsUseCase(repository)

        val result = useCase(
            inAppMessagesEnabled = false,
            systemNotificationsEnabled = true,
        )

        val updated = assertIs<AppResult.Success<NotificationSettings>>(result).value
        assertEquals(false, updated.inAppMessagesEnabled)
        assertEquals(true, updated.systemNotificationsEnabled)
        assertEquals(updated, repository.settings.first())
    }

    @Test
    fun settingsRemainAssociatedWithCurrentAccountWhenUpdated() = runBlocking {
        val repository = InMemoryNotificationRepository(
            currentAccountId = "account-456",
            settings = NotificationSettings(
                accountId = "account-456",
                inAppMessagesEnabled = true,
                systemNotificationsEnabled = true,
            ),
        )
        val useCase = UpdateNotificationSettingsUseCase(repository)

        val result = useCase(
            inAppMessagesEnabled = false,
            systemNotificationsEnabled = false,
        )

        val updated = assertIs<AppResult.Success<NotificationSettings>>(result).value
        assertEquals("account-456", updated.accountId)
        assertEquals("account-456", repository.settings.first().accountId)
    }
}
