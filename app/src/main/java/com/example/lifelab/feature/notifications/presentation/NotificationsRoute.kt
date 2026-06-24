package com.example.lifelab.feature.notifications.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifelab.core.ui.component.ActionCard
import com.example.lifelab.core.ui.component.SectionHeader
import com.example.lifelab.core.ui.component.StatePanel
import com.example.lifelab.feature.notifications.data.InMemoryNotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationRepository
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus

@Composable
fun NotificationsRoute(contentPadding: PaddingValues) {
    val repository = remember { InMemoryNotificationRepository() }
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModelFactory(repository),
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

@Composable
fun NotificationsScreen(
    contentPadding: PaddingValues,
    uiState: NotificationsUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> LoadingContent(
            contentPadding = contentPadding,
            modifier = modifier,
        )
        uiState.errorMessage != null -> ErrorContent(
            contentPadding = contentPadding,
            errorMessage = uiState.errorMessage,
            onRetry = { onEvent(NotificationsUiEvent.RetryRefresh) },
            modifier = modifier,
        )
        else -> NotificationsContent(
            contentPadding = contentPadding,
            uiState = uiState,
            onEvent = onEvent,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingContent(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        StatePanel(
            title = "Loading notifications",
            body = "Checking inbox and delivery controls.",
            isLoading = true,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun ErrorContent(
    contentPadding: PaddingValues,
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        StatePanel(
            title = "Notifications unavailable",
            body = errorMessage,
            actionLabel = "Retry",
            onAction = onRetry,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Composable
private fun NotificationsContent(
    contentPadding: PaddingValues,
    uiState: NotificationsUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableStateOf(NotificationsTab.Inbox) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SectionHeader(
            title = "Notifications",
            subtitle = "Inbox and delivery settings.",
        )

        NotificationSegments(
            selectedTab = selectedTab,
            onSelectTab = { selectedTab = it },
        )

        when (selectedTab) {
            NotificationsTab.Inbox -> {
                if (uiState.isEmpty) {
                    EmptyContent(inAppMessagesEnabled = uiState.settings?.inAppMessagesEnabled != false)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        uiState.messages.forEach { message ->
                            MessageContent(
                                message = message,
                                onMarkRead = { onEvent(NotificationsUiEvent.MarkRead(message.id)) },
                                onArchive = { onEvent(NotificationsUiEvent.Archive(message.id)) },
                            )
                        }
                    }
                }
            }

            NotificationsTab.Settings -> {
                uiState.settings?.let { settings ->
                    SettingsContent(
                        settings = settings,
                        systemIntegration = uiState.systemIntegration,
                        onEvent = onEvent,
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
private fun NotificationSegments(
    selectedTab: NotificationsTab,
    onSelectTab: (NotificationsTab) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        NotificationsTab.entries.forEachIndexed { index, tab ->
            SegmentedButton(
                selected = selectedTab == tab,
                onClick = { onSelectTab(tab) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = NotificationsTab.entries.size,
                ),
            ) {
                Text(tab.label)
            }
        }
    }
}

@Composable
private fun SettingsContent(
    settings: NotificationSettings,
    systemIntegration: SystemNotificationIntegrationUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SettingRow(
            label = "In-app messages",
            checked = settings.inAppMessagesEnabled,
            onCheckedChange = { onEvent(NotificationsUiEvent.SetInAppMessagesEnabled(it)) },
        )
        SettingRow(
            label = "System notifications",
            checked = settings.systemNotificationsEnabled,
            onCheckedChange = { onEvent(NotificationsUiEvent.SetSystemNotificationsEnabled(it)) },
        )
        Text(
            text = "System notification integration",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = systemIntegration.statusLabel,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun SettingRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )
    }
}

@Composable
private fun EmptyContent(inAppMessagesEnabled: Boolean) {
    StatePanel(
        title = if (inAppMessagesEnabled) {
            "No active notifications"
        } else {
            "In-app messages are disabled"
        },
        body = if (inAppMessagesEnabled) {
            "New reminders and system messages will land here."
        } else {
            "Turn in-app messages back on in settings when you want inbox updates."
        },
    )
}

@Composable
private fun MessageContent(
    message: NotificationMessage,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit,
) {
    ActionCard(
        title = message.title,
        body = "${message.body}\n${message.category} / ${message.createdAtLabel} / ${message.status.label()}",
        actionLabel = if (message.status == NotificationStatus.Unread) "Mark read" else null,
        onAction = if (message.status == NotificationStatus.Unread) onMarkRead else null,
        secondaryLabel = "Archive",
        onSecondaryAction = onArchive,
    )
}

private fun NotificationStatus.label(): String = when (this) {
    NotificationStatus.Unread -> "Unread"
    NotificationStatus.Read -> "Read"
    NotificationStatus.Archived -> "Archived"
}

private enum class NotificationsTab(val label: String) {
    Inbox("Inbox"),
    Settings("Settings"),
}

private class NotificationsViewModelFactory(
    private val repository: NotificationRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            return NotificationsViewModel(repository) as T
        }
        error("Unknown ViewModel class: ${modelClass.name}")
    }
}
