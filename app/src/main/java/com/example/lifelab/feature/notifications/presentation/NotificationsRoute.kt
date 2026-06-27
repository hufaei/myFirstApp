package com.example.lifelab.feature.notifications.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lifelab.R
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.core.ui.components.LifeLabStateCard
import com.example.lifelab.feature.notifications.domain.NotificationMessage
import com.example.lifelab.feature.notifications.domain.NotificationSettings
import com.example.lifelab.feature.notifications.domain.NotificationStatus

@Composable
fun NotificationsRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    NotificationsScreen(
        contentPadding = contentPadding,
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
fun NotificationsScreen(
    contentPadding: PaddingValues,
    uiState: NotificationsUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> LoadingContent(
            contentPadding = contentPadding,
            onBack = onBack,
            modifier = modifier,
        )
        uiState.errorMessage != null -> ErrorContent(
            contentPadding = contentPadding,
            errorMessage = uiState.errorMessage,
            onRetry = { onEvent(NotificationsUiEvent.RetryRefresh) },
            onBack = onBack,
            modifier = modifier,
        )
        else -> NotificationsContent(
            contentPadding = contentPadding,
            uiState = uiState,
            onEvent = onEvent,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingContent(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NotificationsHeader(onBack = onBack)
        LifeLabStateCard(title = stringResource(R.string.notifications_loading))
    }
}

@Composable
private fun NotificationsHeader(
    onBack: () -> Unit,
) {
    LifeLabScreenHeader(
        title = stringResource(R.string.notifications_title),
        subtitle = stringResource(R.string.notifications_subtitle),
        onBack = onBack,
    )
}

@Composable
private fun ErrorContent(
    contentPadding: PaddingValues,
    errorMessage: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NotificationsHeader(onBack = onBack)
        LifeLabStateCard(
            title = stringResource(R.string.notifications_unavailable),
            body = errorMessage,
            actionLabel = stringResource(R.string.common_retry),
            onAction = onRetry,
        )
    }
}

@Composable
private fun NotificationsContent(
    contentPadding: PaddingValues,
    uiState: NotificationsUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        NotificationsHeader(onBack = onBack)

        uiState.settings?.let { settings ->
            SettingsContent(
                settings = settings,
                systemIntegration = uiState.systemIntegration,
                onEvent = onEvent,
            )
        }

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
}

@Composable
private fun SettingsContent(
    settings: NotificationSettings,
    systemIntegration: SystemNotificationIntegrationUiState,
    onEvent: (NotificationsUiEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.notifications_settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            SettingRow(
                label = stringResource(R.string.notifications_in_app_messages),
                checked = settings.inAppMessagesEnabled,
                onCheckedChange = { onEvent(NotificationsUiEvent.SetInAppMessagesEnabled(it)) },
            )
            SettingRow(
                label = stringResource(R.string.notifications_system_notifications),
                checked = settings.systemNotificationsEnabled,
                onCheckedChange = { onEvent(NotificationsUiEvent.SetSystemNotificationsEnabled(it)) },
            )
            Text(
                text = stringResource(R.string.notifications_system_integration),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(
                    if (systemIntegration.enabled) {
                        R.string.notifications_system_enabled
                    } else {
                        R.string.notifications_system_disabled
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
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
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
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
    LifeLabStateCard(
        title = if (inAppMessagesEnabled) {
            stringResource(R.string.notifications_empty_active)
        } else {
            stringResource(R.string.notifications_empty_disabled)
        },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageContent(
    message: NotificationMessage,
    onMarkRead: () -> Unit,
    onArchive: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = message.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = stringResource(R.string.notifications_category_value, message.category),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.notifications_created_value, message.createdAtLabel),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(R.string.notifications_status_value, message.status.label()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (message.status == NotificationStatus.Unread) {
                    TextButton(onClick = onMarkRead) {
                        Text(text = stringResource(R.string.notifications_mark_read))
                    }
                }
                TextButton(onClick = onArchive) {
                    Text(text = stringResource(R.string.notifications_archive))
                }
            }
        }
    }
}

@Composable
private fun NotificationStatus.label(): String = when (this) {
    NotificationStatus.Unread -> stringResource(R.string.notifications_status_unread)
    NotificationStatus.Read -> stringResource(R.string.notifications_status_read)
    NotificationStatus.Archived -> stringResource(R.string.notifications_status_archived)
}
