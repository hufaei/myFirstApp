package com.example.lifelab.feature.habits.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.lifelab.R
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoSource
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
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onReminderPriorityChange: (String, HabitReminderPriority) -> Unit,
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                    onReminderEnabledChange = onReminderEnabledChange,
                    onReminderTimeChange = onReminderTimeChange,
                    onReminderPriorityChange = onReminderPriorityChange,
                    onAttachPhotos = onAttachPhotos,
                )
            }
        }
    }
}

@Composable
private fun StatsCard(state: HabitsUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
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
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    photos: List<PhotoRecord>,
    isCheckedInToday: Boolean,
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onReminderPriorityChange: (String, HabitReminderPriority) -> Unit,
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val reminderTime = habit.reminder.time
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            onReminderEnabledChange(habit.id, true)
        }
    }

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

            Button(
                onClick = { onCheckIn(habit.id) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isCheckedInToday,
            ) {
                androidx.compose.material3.Icon(
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

            ReminderControls(
                reminderEnabled = habit.reminder.enabled,
                reminderTimeLabel = reminderTime?.format(timeFormatter)
                    ?: stringResource(R.string.habits_no_time),
                reminderTime = reminderTime,
                priority = habit.reminder.priority,
                onReminderEnabledChange = { enabled ->
                    if (enabled && context.needsNotificationPermission()) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
        HabitUiMessage.Missing -> stringResource(R.string.habits_message_missing)
        HabitUiMessage.LoadError -> stringResource(R.string.habits_load_error)
    }

private fun Habit.isCheckedInOn(date: java.time.LocalDate): Boolean =
    lastCheckInDate == date || date in checkInDates

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReminderControls(
    reminderEnabled: Boolean,
    reminderTimeLabel: String,
    reminderTime: LocalTime?,
    priority: HabitReminderPriority,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: () -> Unit,
    onReminderPriorityChange: (HabitReminderPriority) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
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
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderEnabledChange,
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
}

@Composable
private fun HabitReminderPriority.label(): String =
    when (this) {
        HabitReminderPriority.High -> stringResource(R.string.habits_priority_high)
        HabitReminderPriority.Normal -> stringResource(R.string.habits_priority_normal)
        HabitReminderPriority.Low -> stringResource(R.string.habits_priority_low)
    }

private fun android.content.Context.needsNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) != PackageManager.PERMISSION_GRANTED
