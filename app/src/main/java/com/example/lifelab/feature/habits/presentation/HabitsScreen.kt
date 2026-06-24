package com.example.lifelab.feature.habits.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifelab.core.ui.component.StatePanel
import com.example.lifelab.core.ui.component.SummaryMetric
import com.example.lifelab.feature.habits.domain.model.Habit
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HabitsScreen(
    state: HabitsUiState,
    contentPadding: PaddingValues,
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HabitsPane(
        state = state,
        contentPadding = contentPadding,
        showHeader = true,
        onCheckIn = onCheckIn,
        onReminderEnabledChange = onReminderEnabledChange,
        onReminderTimeChange = onReminderTimeChange,
        onClearMessage = onClearMessage,
        modifier = modifier,
    )
}

@Composable
fun HabitsPane(
    state: HabitsUiState,
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onClearMessage: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showHeader: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val panePadding = if (showHeader) PaddingValues(16.dp) else PaddingValues(0.dp)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = panePadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showHeader) {
            item {
                Text(
                    text = "Habits",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        state.message?.let { message ->
            item {
                MessageCard(
                    message = message,
                    onClearMessage = onClearMessage,
                )
            }
        }

        item {
            StatsCard(state = state)
        }

        when (state.status) {
            HabitsStatus.Loading -> item {
                StatePanel(
                    title = "Loading habits",
                    body = "Checking today's streaks and reminders.",
                    isLoading = true,
                )
            }

            HabitsStatus.Empty -> item {
                StatePanel(
                    title = "No habits yet",
                    body = "Add one repeatable action to make this workbench useful.",
                )
            }

            HabitsStatus.Error -> item {
                StatePanel(
                    title = "Habits could not load",
                    body = state.errorMessage ?: "Unable to load habits.",
                )
            }

            HabitsStatus.Content -> items(
                items = state.habits,
                key = { habit -> habit.id },
            ) { habit ->
                HabitCard(
                    habit = habit,
                    onCheckIn = onCheckIn,
                    onReminderEnabledChange = onReminderEnabledChange,
                    onReminderTimeChange = onReminderTimeChange,
                )
            }
        }
    }
}

@Composable
private fun StatsCard(state: HabitsUiState) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SummaryMetric(
                value = state.stats.totalHabits.toString(),
                label = "Habits",
                helper = "${state.stats.checkedInToday} checked in",
                modifier = Modifier.weight(1f),
            )
            SummaryMetric(
                value = state.stats.longestStreak.toString(),
                label = "Best streak",
                helper = "${state.stats.activeReminders} reminders",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
) {
    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm") }
    val reminderTime = habit.reminder.time

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = habit.name,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "Current streak: ${habit.streakCount}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(onClick = { onCheckIn(habit.id) }) {
                    Text("Check in")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Reminder",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = reminderTime?.format(timeFormatter) ?: "No time set",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(
                    checked = habit.reminder.enabled,
                    onCheckedChange = { enabled ->
                        onReminderEnabledChange(habit.id, enabled)
                    },
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = {
                        onReminderTimeChange(
                            habit.id,
                            reminderTime?.plusMinutes(30) ?: LocalTime.of(9, 0),
                        )
                    },
                ) {
                    Text(if (reminderTime == null) "Set 09:00" else "+30 min")
                }
            }
        }
    }
}

@Composable
private fun MessageCard(
    message: String,
    onClearMessage: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onClearMessage) {
                Text("Dismiss")
            }
        }
    }
}
