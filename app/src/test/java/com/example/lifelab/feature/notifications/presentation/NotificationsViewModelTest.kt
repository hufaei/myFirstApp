package com.example.lifelab.feature.notifications.presentation

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.InMemoryAppPreferencesRepository
import com.example.lifelab.core.notifications.AndroidNotificationPermissionStatus
import com.example.lifelab.core.notifications.AndroidNotificationPermissionStatusReader
import com.example.lifelab.core.notifications.NotificationSelfTestResult
import com.example.lifelab.core.notifications.NotificationSelfTestScheduler
import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.notifications.data.InMemoryNotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialContentLoadExposesDefaultVisibleMessagesAndSettings() = runTest {
        val viewModel = NotificationsViewModel(InMemoryNotificationRepository())

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("welcome", "weekly-summary"), state.messages.map { it.id })
        assertEquals(listOf(NotificationStatus.Unread, NotificationStatus.Read), state.messages.map { it.status })
        val settings = assertNotNull(state.settings)
        assertTrue(settings.inAppMessagesEnabled)
        assertFalse(settings.systemNotificationsEnabled)
        assertEquals(
            AndroidNotificationPermissionStatus.NotRequired,
            state.systemIntegration.androidPermissionStatus,
        )
        assertEquals(
            HabitReminderDeliveryStatus.ControlledFromHabits,
            state.systemIntegration.habitReminderDeliveryStatus,
        )
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun markingUnreadMessageReadUpdatesState() = runTest {
        val viewModel = NotificationsViewModel(InMemoryNotificationRepository())
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.MarkRead("welcome"))
        advanceUntilIdle()

        val welcome = viewModel.uiState.value.messages.single { it.id == "welcome" }
        assertEquals(NotificationStatus.Read, welcome.status)
    }

    @Test
    fun archivingMessageRemovesItFromActiveList() = runTest {
        val viewModel = NotificationsViewModel(InMemoryNotificationRepository())
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.Archive("welcome"))
        advanceUntilIdle()

        assertEquals(listOf("weekly-summary"), viewModel.uiState.value.messages.map { it.id })
    }

    @Test
    fun togglingInAppMessagesUpdatesStateAndPreservesAndroidPermissionStatus() = runTest {
        val viewModel = NotificationsViewModel(
            repository = InMemoryNotificationRepository(),
            notificationPermissionStatusReader = AndroidNotificationPermissionStatusReader(
                AndroidNotificationPermissionStatus.Granted,
            ),
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.SetInAppMessagesEnabled(false))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        val settings = assertNotNull(state.settings)
        assertFalse(settings.inAppMessagesEnabled)
        assertEquals(
            AndroidNotificationPermissionStatus.Granted,
            state.systemIntegration.androidPermissionStatus,
        )
    }

    @Test
    fun settingEventPreservesSystemIntegrationBeforeSettingsFlowEmits() = runTest {
        val repository = DeferredSettingsNotificationRepository()
        val viewModel = NotificationsViewModel(
            repository = repository,
            notificationPermissionStatusReader = AndroidNotificationPermissionStatusReader(
                AndroidNotificationPermissionStatus.Granted,
            ),
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.SetInAppMessagesEnabled(false))
        advanceUntilIdle()

        repository.emitPendingSettings()
        advanceUntilIdle()

        val settings = assertNotNull(viewModel.uiState.value.settings)
        assertFalse(settings.inAppMessagesEnabled)
        assertEquals(
            AndroidNotificationPermissionStatus.Granted,
            viewModel.uiState.value.systemIntegration.androidPermissionStatus,
        )
    }

    @Test
    fun immediateSelfTestEventShowsTestNotificationAndStatusMessage() = runTest {
        val scheduler = FakeNotificationSelfTestScheduler()
        val viewModel = NotificationsViewModel(
            repository = InMemoryNotificationRepository(),
            notificationSelfTestScheduler = scheduler,
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.SendImmediateTestNotification)
        advanceUntilIdle()

        assertEquals(1, scheduler.immediateTestNotificationCount)
        assertEquals(0, scheduler.scheduledTestReminderCount)
        assertEquals(NotificationSelfTestMessage.ImmediateSent, viewModel.uiState.value.systemTestMessage)
    }

    @Test
    fun immediateSelfTestEventShowsBlockedMessageWhenNotificationCannotPost() = runTest {
        val scheduler = FakeNotificationSelfTestScheduler(
            immediateResult = NotificationSelfTestResult.Blocked,
        )
        val viewModel = NotificationsViewModel(
            repository = InMemoryNotificationRepository(),
            notificationSelfTestScheduler = scheduler,
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.SendImmediateTestNotification)
        advanceUntilIdle()

        assertEquals(1, scheduler.immediateTestNotificationCount)
        assertEquals(0, scheduler.scheduledTestReminderCount)
        assertEquals(
            NotificationSelfTestMessage.BlockedByAndroidNotifications,
            viewModel.uiState.value.systemTestMessage,
        )
    }

    @Test
    fun scheduledSelfTestEventSchedulesOneMinuteReminderAndStatusMessage() = runTest {
        val scheduler = FakeNotificationSelfTestScheduler()
        val viewModel = NotificationsViewModel(
            repository = InMemoryNotificationRepository(),
            notificationSelfTestScheduler = scheduler,
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.ScheduleOneMinuteTestReminder)
        advanceUntilIdle()

        assertEquals(0, scheduler.immediateTestNotificationCount)
        assertEquals(1, scheduler.scheduledTestReminderCount)
        assertEquals(NotificationSelfTestMessage.OneMinuteScheduled, viewModel.uiState.value.systemTestMessage)
    }

    @Test
    fun scheduledSelfTestEventShowsBlockedMessageWhenNotificationCannotPost() = runTest {
        val scheduler = FakeNotificationSelfTestScheduler(
            scheduledResult = NotificationSelfTestResult.Blocked,
        )
        val viewModel = NotificationsViewModel(
            repository = InMemoryNotificationRepository(),
            notificationSelfTestScheduler = scheduler,
        )
        advanceUntilIdle()

        viewModel.onEvent(NotificationsUiEvent.ScheduleOneMinuteTestReminder)
        advanceUntilIdle()

        assertEquals(0, scheduler.immediateTestNotificationCount)
        assertEquals(1, scheduler.scheduledTestReminderCount)
        assertEquals(
            NotificationSelfTestMessage.BlockedByAndroidNotifications,
            viewModel.uiState.value.systemTestMessage,
        )
    }

    @Test
    fun emptyStateWhenInAppMessagesAreDisabledOrAllMessagesArchived() = runTest {
        val disabledViewModel = NotificationsViewModel(InMemoryNotificationRepository())
        advanceUntilIdle()

        disabledViewModel.onEvent(NotificationsUiEvent.SetInAppMessagesEnabled(false))
        advanceUntilIdle()

        assertTrue(disabledViewModel.uiState.value.isEmpty)
        assertEquals(emptyList(), disabledViewModel.uiState.value.messages)

        val archivedViewModel = NotificationsViewModel(
            InMemoryNotificationRepository(
                messages = listOf(
                    message(id = "archived-1", status = NotificationStatus.Archived),
                    message(id = "archived-2", status = NotificationStatus.Archived),
                ),
            ),
        )
        advanceUntilIdle()

        assertTrue(archivedViewModel.uiState.value.isEmpty)
        assertEquals(emptyList(), archivedViewModel.uiState.value.messages)
    }

    @Test
    fun globalNotificationPreferenceDisablesVisibleMessagesWhilePreservingBlockedPermissionState() = runTest {
        val preferencesRepository = InMemoryAppPreferencesRepository(
            AppPreferences(notificationEnabled = false),
        )
        val viewModel = NotificationsViewModel(
            repository = InMemoryNotificationRepository(),
            appPreferencesRepository = preferencesRepository,
            notificationPermissionStatusReader = AndroidNotificationPermissionStatusReader(
                AndroidNotificationPermissionStatus.Blocked,
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertEquals(emptyList(), state.messages)
        assertFalse(assertNotNull(state.settings).inAppMessagesEnabled)
        assertEquals(
            AndroidNotificationPermissionStatus.Blocked,
            state.systemIntegration.androidPermissionStatus,
        )
        assertEquals(
            HabitReminderDeliveryStatus.BlockedByAndroidPermission,
            state.systemIntegration.habitReminderDeliveryStatus,
        )
        assertTrue(state.systemIntegration.habitReminderDeliveryBlocked)
    }

    @Test
    fun systemDisabledNotificationsBlockDeliveryWithoutRuntimePermissionPrompt() {
        val integration = SystemNotificationIntegrationUiState(
            androidPermissionStatus = AndroidNotificationPermissionStatus.DisabledInSystemSettings,
        )

        assertEquals(
            HabitReminderDeliveryStatus.BlockedByAndroidPermission,
            integration.habitReminderDeliveryStatus,
        )
        assertTrue(integration.habitReminderDeliveryBlocked)
        assertFalse(integration.canRequestAndroidNotificationPermission)
    }

    @Test
    fun repositoryRefreshErrorBecomesErrorState() = runTest {
        val viewModel = NotificationsViewModel(
            RefreshFailureNotificationRepository(
                refreshError = AppError.Network(message = "Unable to refresh notifications."),
            ),
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Unable to refresh notifications.", state.errorMessage)
        assertTrue(state.messages.isEmpty())
    }

    private fun message(
        id: String,
        status: NotificationStatus,
    ) = NotificationMessage(
        id = id,
        title = "Lab result ready",
        body = "Your latest LifeLab result is available.",
        category = "Results",
        createdAtLabel = "Today",
        status = status,
    )
}

private class RefreshFailureNotificationRepository(
    private val refreshError: AppError,
) : NotificationRepository {
    private val messageState = MutableStateFlow(emptyList<NotificationMessage>())
    private val settingsState = MutableStateFlow(
        NotificationSettings(
            accountId = "current-account",
            inAppMessagesEnabled = true,
            systemNotificationsEnabled = false,
        ),
    )

    override val messages: Flow<List<NotificationMessage>> = messageState
    override val settings: Flow<NotificationSettings> = settingsState

    override suspend fun getMessage(messageId: String): NotificationMessage? =
        messageState.value.firstOrNull { it.id == messageId }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: NotificationStatus,
    ): AppResult<NotificationMessage> =
        AppResult.Failure(AppError.Validation(message = "Missing message."))

    override suspend fun updateSettings(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ): AppResult<NotificationSettings> {
        val updated = settingsState.value.copy(
            inAppMessagesEnabled = inAppMessagesEnabled,
            systemNotificationsEnabled = systemNotificationsEnabled,
        )
        settingsState.value = updated
        return AppResult.Success(updated)
    }

    override suspend fun refresh(): AppResult<Unit> = AppResult.Failure(refreshError)
}

private class DeferredSettingsNotificationRepository : NotificationRepository {
    private val messageState = MutableStateFlow(emptyList<NotificationMessage>())
    private val settingsState = MutableStateFlow(
        NotificationSettings(
            accountId = "current-account",
            inAppMessagesEnabled = true,
            systemNotificationsEnabled = false,
        ),
    )
    private var pendingSettings = settingsState.value

    override val messages: Flow<List<NotificationMessage>> = messageState
    override val settings: Flow<NotificationSettings> = settingsState

    override suspend fun getMessage(messageId: String): NotificationMessage? =
        messageState.value.firstOrNull { it.id == messageId }

    override suspend fun updateMessageStatus(
        messageId: String,
        status: NotificationStatus,
    ): AppResult<NotificationMessage> =
        AppResult.Failure(AppError.Validation(message = "Missing message."))

    override suspend fun updateSettings(
        inAppMessagesEnabled: Boolean,
        systemNotificationsEnabled: Boolean,
    ): AppResult<NotificationSettings> {
        pendingSettings = pendingSettings.copy(
            inAppMessagesEnabled = inAppMessagesEnabled,
            systemNotificationsEnabled = systemNotificationsEnabled,
        )
        return AppResult.Success(pendingSettings)
    }

    override suspend fun refresh(): AppResult<Unit> = AppResult.Success(Unit)

    fun emitPendingSettings() {
        settingsState.value = pendingSettings
    }
}

private class FakeNotificationSelfTestScheduler(
    private val immediateResult: NotificationSelfTestResult = NotificationSelfTestResult.Sent,
    private val scheduledResult: NotificationSelfTestResult = NotificationSelfTestResult.Scheduled,
) : NotificationSelfTestScheduler() {
    var immediateTestNotificationCount = 0
        private set
    var scheduledTestReminderCount = 0
        private set

    override fun showTestNotification(): NotificationSelfTestResult {
        immediateTestNotificationCount += 1
        return immediateResult
    }

    override fun scheduleTestReminderOneMinuteFromNow(): NotificationSelfTestResult {
        scheduledTestReminderCount += 1
        return scheduledResult
    }
}
