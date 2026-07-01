package com.example.lifelab.feature.habits.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.lifelab.R
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.core.notifications.AndroidNotificationPermissionStatus
import com.example.lifelab.core.notifications.androidNotificationPermissionStatus
import com.example.lifelab.core.notifications.canPostNotifications
import com.example.lifelab.core.ui.components.LifeLabMessageBanner
import com.example.lifelab.core.ui.components.LifeLabPhotoStrip
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.core.ui.components.LifeLabStateCard
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitReminderPriority
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HabitsScreen(
    state: HabitsUiState,
    contentPadding: PaddingValues,
    onStartCreate: () -> Unit,
    onStartEdit: (String) -> Unit,
    onUpdateEditorName: (String) -> Unit,
    onEditorReminderEnabledChange: (Boolean) -> Unit,
    onIncreaseEditorReminderTime: () -> Unit,
    onEditorReminderPriorityChange: (HabitReminderPriority) -> Unit,
    onSaveEditor: () -> Unit,
    onDismissEditor: () -> Unit,
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onReminderPriorityChange: (String, HabitReminderPriority) -> Unit,
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var androidPermissionStatus by remember {
        mutableStateOf(context.androidNotificationPermissionStatus())
    }
    var pendingReminderHabitId by remember { mutableStateOf<String?>(null) }
    var pendingEditorReminderEnable by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        androidPermissionStatus = context.androidNotificationPermissionStatus()
        if (granted) {
            pendingReminderHabitId?.let { habitId ->
                onReminderEnabledChange(habitId, true)
            }
            if (pendingEditorReminderEnable) {
                onEditorReminderEnabledChange(true)
            }
        }
        pendingReminderHabitId = null
        pendingEditorReminderEnable = false
    }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        androidPermissionStatus = context.androidNotificationPermissionStatus()
    }
    val hasBlockedActiveReminder = state.habits.any { habit -> habit.reminder.enabled } &&
        !androidPermissionStatus.canPostNotifications

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            LifeLabScreenHeader(
                title = stringResource(R.string.habits_title),
                subtitle = stringResource(R.string.habits_subtitle),
                actions = {
                    FilledTonalButton(onClick = onStartCreate) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.habits_new))
                    }
                },
            )
        }

        state.message?.let { message ->
            item {
                LifeLabMessageBanner(
                    message = message.text(),
                    onDismiss = onClearMessage,
                )
            }
        }

        if (hasBlockedActiveReminder) {
            item {
                LifeLabStateCard(
                    title = stringResource(R.string.habits_reminder_permission_blocked_title),
                    body = stringResource(R.string.habits_reminder_permission_blocked_body),
                    actionLabel = stringResource(R.string.habits_reminder_permission_action),
                    onAction = {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }
        }

        state.editor?.let { editor ->
            item {
                HabitEditorCard(
                    editor = editor,
                    onUpdateName = onUpdateEditorName,
                    onReminderEnabledChange = onEditorReminderEnabledChange,
                    onIncreaseReminderTime = onIncreaseEditorReminderTime,
                    onReminderPriorityChange = onEditorReminderPriorityChange,
                    androidPermissionStatus = androidPermissionStatus,
                    onRequestNotificationPermission = {
                        pendingEditorReminderEnable = true
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                    onSave = onSaveEditor,
                    onDismiss = onDismissEditor,
                )
            }
        }

        item {
            StatsCard(state = state)
        }

        when (state.status) {
            HabitsStatus.Loading -> item {
                LifeLabStateCard(
                    title = stringResource(R.string.habits_loading),
                    body = stringResource(R.string.habits_loading_body),
                )
                CircularProgressIndicator(modifier = Modifier.padding(top = 12.dp))
            }

            HabitsStatus.Empty -> item {
                LifeLabStateCard(
                    title = stringResource(R.string.habits_empty),
                    body = stringResource(R.string.habits_empty_body),
                )
            }

            HabitsStatus.Error -> item {
                LifeLabStateCard(
                    title = state.errorMessage?.text() ?: stringResource(R.string.habits_load_error),
                )
            }

            HabitsStatus.Content -> items(
                items = state.habits,
                key = { habit -> habit.id },
            ) { habit ->
                HabitCard(
                    habit = habit,
                    photos = state.photosForHabit(habit.id),
                    isCheckedInToday = habit.isCheckedInOn(state.today),
                    onCheckIn = onCheckIn,
                    onStartEdit = onStartEdit,
                    onReminderEnabledChange = onReminderEnabledChange,
                    onReminderTimeChange = onReminderTimeChange,
                    onReminderPriorityChange = onReminderPriorityChange,
                    onAttachPhotos = onAttachPhotos,
                    androidPermissionStatus = androidPermissionStatus,
                    onRequestNotificationPermission = {
                        pendingReminderHabitId = habit.id
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    },
                )
            }
        }
    }
}

@Composable
private fun StatsCard(state: HabitsUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            StatText(
                label = stringResource(R.string.habits_stat_total),
                value = state.stats.totalHabits.toString(),
                modifier = Modifier.weight(1f),
            )
            StatText(
                label = stringResource(R.string.habits_stat_today),
                value = state.stats.checkedInToday.toString(),
                modifier = Modifier.weight(1f),
            )
            StatText(
                label = stringResource(R.string.habits_stat_reminders),
                value = state.stats.activeReminders.toString(),
                modifier = Modifier.weight(1f),
            )
            StatText(
                label = stringResource(R.string.habits_stat_best),
                value = state.stats.longestStreak.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatText(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.tertiary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    photos: List<PhotoRecord>,
    isCheckedInToday: Boolean,
    onCheckIn: (String) -> Unit,
    onStartEdit: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onReminderPriorityChange: (String, HabitReminderPriority) -> Unit,
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
    androidPermissionStatus: AndroidNotificationPermissionStatus,
    onRequestNotificationPermission: () -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val reminderTime = habit.reminder.time

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = stringResource(R.string.habits_current_streak, habit.streakCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = if (isCheckedInToday) {
                            stringResource(R.string.habits_checked_today)
                        } else {
                            stringResource(R.string.habits_not_checked_today)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isCheckedInToday) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
            }

            FilledTonalButton(
                onClick = { onCheckIn(habit.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCheckedInToday,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (isCheckedInToday) {
                        stringResource(R.string.habits_checked_today)
                    } else {
                        stringResource(R.string.habits_check_in)
                    },
                )
            }
            OutlinedButton(
                onClick = { onStartEdit(habit.id) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.habits_edit))
            }

            ReminderControls(
                reminderEnabled = habit.reminder.enabled,
                reminderTimeLabel = reminderTime?.format(timeFormatter)
                    ?: stringResource(R.string.habits_no_time),
                reminderTime = reminderTime,
                priority = habit.reminder.priority,
                reminderDeliveryBlocked = habit.reminder.enabled &&
                    !androidPermissionStatus.canPostNotifications,
                onReminderEnabledChange = { enabled ->
                    if (enabled && androidPermissionStatus == AndroidNotificationPermissionStatus.Blocked) {
                        onRequestNotificationPermission()
                    } else {
                        onReminderEnabledChange(habit.id, enabled)
                    }
                },
                onReminderTimeChange = {
                    onReminderTimeChange(
                        habit.id,
                        reminderTime?.plusMinutes(30) ?: LocalTime.of(9, 0),
                    )
                },
                onReminderPriorityChange = { priority ->
                    onReminderPriorityChange(habit.id, priority)
                },
            )

            LifeLabPhotoStrip(
                owner = habitPhotoOwner(habit.id),
                photos = photos,
                onAttachPhotos = { localUris, source ->
                    onAttachPhotos(habit.id, localUris, source)
                },
            )
        }
    }
}

@Composable
private fun HabitUiMessage.text(): String =
    when (this) {
        is HabitUiMessage.CheckedIn -> stringResource(R.string.habits_message_checked_in, habitName)
        is HabitUiMessage.AlreadyCheckedIn -> {
            stringResource(R.string.habits_message_already_checked_in, habitName)
        }
        is HabitUiMessage.ReminderUpdated -> {
            stringResource(R.string.habits_message_reminder_updated, habitName)
        }
        is HabitUiMessage.HabitSaved -> stringResource(R.string.habits_message_saved, habitName)
        HabitUiMessage.Missing -> stringResource(R.string.habits_message_missing)
        HabitUiMessage.LoadError -> stringResource(R.string.habits_load_error)
    }

private fun Habit.isCheckedInOn(date: java.time.LocalDate): Boolean =
    lastCheckInDate == date || date in checkInDates

@Composable
private fun HabitEditorCard(
    editor: HabitEditorState,
    onUpdateName: (String) -> Unit,
    onReminderEnabledChange: (Boolean) -> Unit,
    onIncreaseReminderTime: () -> Unit,
    onReminderPriorityChange: (HabitReminderPriority) -> Unit,
    androidPermissionStatus: AndroidNotificationPermissionStatus,
    onRequestNotificationPermission: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = when (editor.mode) {
                    HabitEditorMode.Create -> stringResource(R.string.habits_editor_create_title)
                    HabitEditorMode.Edit -> stringResource(R.string.habits_editor_edit_title)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = editor.name,
                onValueChange = onUpdateName,
                label = { Text(stringResource(R.string.habits_editor_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            ReminderControls(
                reminderEnabled = editor.reminderEnabled,
                reminderTimeLabel = editor.reminderTime.format(timeFormatter),
                reminderTime = editor.reminderTime,
                priority = editor.reminderPriority,
                reminderDeliveryBlocked = editor.reminderEnabled &&
                    !androidPermissionStatus.canPostNotifications,
                onReminderEnabledChange = { enabled ->
                    if (enabled && androidPermissionStatus == AndroidNotificationPermissionStatus.Blocked) {
                        onRequestNotificationPermission()
                    } else {
                        onReminderEnabledChange(enabled)
                    }
                },
                onReminderTimeChange = onIncreaseReminderTime,
                onReminderPriorityChange = onReminderPriorityChange,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                ) {
                    Text(stringResource(R.string.habits_editor_cancel))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    enabled = editor.canSave,
                ) {
                    Text(stringResource(R.string.habits_editor_save))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderControls(
    reminderEnabled: Boolean,
    reminderTimeLabel: String,
    reminderTime: LocalTime?,
    priority: HabitReminderPriority,
    reminderDeliveryBlocked: Boolean,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: () -> Unit,
    onReminderPriorityChange: (HabitReminderPriority) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = stringResource(R.string.habits_reminder),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = reminderTimeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = reminderEnabled,
                onCheckedChange = onReminderEnabledChange,
            )
        }
        if (reminderDeliveryBlocked) {
            Text(
                text = stringResource(R.string.habits_reminder_delivery_blocked),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        OutlinedButton(
            onClick = onReminderTimeChange,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                if (reminderTime == null) {
                    stringResource(R.string.habits_set_default_time)
                } else {
                    stringResource(R.string.habits_add_30_minutes)
                },
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            HabitReminderPriority.entries.forEach { option ->
                FilterChip(
                    selected = priority == option,
                    onClick = { onReminderPriorityChange(option) },
                    label = { Text(option.label()) },
                )
            }
        }
    }
}

@Composable
private fun HabitReminderPriority.label(): String =
    when (this) {
        HabitReminderPriority.High -> stringResource(R.string.habits_priority_high)
        HabitReminderPriority.Normal -> stringResource(R.string.habits_priority_normal)
        HabitReminderPriority.Low -> stringResource(R.string.habits_priority_low)
    }
