package com.example.lifelab.feature.habits.presentation

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.lifelab.R
import com.example.lifelab.core.media.PhotoAttachmentActions
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoFileStorage
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.core.media.copyPhotosToAppStorage
import com.example.lifelab.core.media.toLifeLabFileProviderUri
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
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
    onClearMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.habits_title),
                style = MaterialTheme.typography.headlineMedium,
            )
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
                CircularProgressIndicator()
            }

            HabitsStatus.Empty -> item {
                Text(
                    text = stringResource(R.string.habits_empty),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            HabitsStatus.Error -> item {
                Text(
                    text = state.errorMessage?.localizedHabitMessage() ?: stringResource(R.string.habits_load_error),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            HabitsStatus.Content -> items(
                items = state.habits,
                key = { habit -> habit.id },
            ) { habit ->
                HabitCard(
                    habit = habit,
                    photos = state.photosForHabit(habit.id),
                    onCheckIn = onCheckIn,
                    onReminderEnabledChange = onReminderEnabledChange,
                    onReminderTimeChange = onReminderTimeChange,
                    onAttachPhotos = onAttachPhotos,
                )
            }
        }
    }
}

@Composable
private fun StatsCard(state: HabitsUiState) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatText(label = stringResource(R.string.habits_stat_total), value = state.stats.totalHabits.toString())
            StatText(label = stringResource(R.string.habits_stat_today), value = state.stats.checkedInToday.toString())
            StatText(label = stringResource(R.string.habits_stat_reminders), value = state.stats.activeReminders.toString())
            StatText(label = stringResource(R.string.habits_stat_best), value = state.stats.longestStreak.toString())
        }
    }
}

@Composable
private fun StatText(
    label: String,
    value: String,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun HabitCard(
    habit: Habit,
    photos: List<PhotoRecord>,
    onCheckIn: (String) -> Unit,
    onReminderEnabledChange: (String, Boolean) -> Unit,
    onReminderTimeChange: (String, LocalTime) -> Unit,
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
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
                        text = stringResource(R.string.habits_current_streak, habit.streakCount),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Button(onClick = { onCheckIn(habit.id) }) {
                    Text(stringResource(R.string.habits_check_in))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.habits_reminder),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = reminderTime?.format(timeFormatter) ?: stringResource(R.string.habits_no_time),
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
                    Text(
                        if (reminderTime == null) {
                            stringResource(R.string.habits_set_default_time)
                        } else {
                            stringResource(R.string.habits_add_30_minutes)
                        },
                    )
                }
            }

            HabitPhotoSection(
                habitId = habit.id,
                photos = photos,
                onAttachPhotos = onAttachPhotos,
            )
        }
    }
}

@Composable
private fun HabitPhotoSection(
    habitId: String,
    photos: List<PhotoRecord>,
    onAttachPhotos: (String, List<String>, PhotoSource) -> Unit,
) {
    val owner = remember(habitId) { habitPhotoOwner(habitId) }
    val context = LocalContext.current
    val remainingSlots = PhotoAttachmentPolicy().remainingSlots(owner, photos)
    val cameraCaptureUri = rememberHabitCameraCaptureUri(
        habitId = habitId,
        remainingSlots = remainingSlots,
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.photo_section_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.photo_remaining_count, remainingSlots),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PhotoAttachmentActions(
                remainingSlots = remainingSlots,
                cameraCaptureUri = cameraCaptureUri,
                pickerLabel = stringResource(R.string.photo_add_from_album),
                cameraLabel = stringResource(R.string.photo_take_photo),
                onPickerPhotosSelected = { uris ->
                    val storedUris = context.copyPhotosToAppStorage(
                        owner = owner,
                        uris = uris.take(remainingSlots),
                        startSequence = photos.size,
                        createdAtMillis = System.currentTimeMillis(),
                    )
                    onAttachPhotos(habitId, storedUris.map { it.toString() }, PhotoSource.Picker)
                },
                onCameraPhotoCaptured = { uri ->
                    onAttachPhotos(habitId, listOf(uri.toString()), PhotoSource.Camera)
                },
            )
        }

        if (photos.isEmpty()) {
            Text(
                text = stringResource(R.string.photo_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER).forEach { photo ->
                    AsyncImage(
                        model = photo.localUri,
                        contentDescription = stringResource(R.string.photo_preview_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberHabitCameraCaptureUri(
    habitId: String,
    remainingSlots: Int,
): Uri? {
    val context = LocalContext.current
    return remember(habitId, remainingSlots) {
        if (remainingSlots <= 0) {
            null
        } else {
            PhotoFileStorage(
                filesDir = context.filesDir,
                cacheDir = context.cacheDir,
            ).createCameraCaptureFile(
                owner = habitPhotoOwner(habitId),
                createdAtMillis = System.currentTimeMillis(),
            ).toLifeLabFileProviderUri(context)
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
                text = message.localizedHabitMessage(),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onClearMessage) {
                Text(stringResource(R.string.common_dismiss))
            }
        }
    }
}

@Composable
private fun String.localizedHabitMessage(): String {
    Regex("^Checked in (.+)\\.$").matchEntire(this)?.let { match ->
        return stringResource(R.string.habits_message_checked_in, match.groupValues[1])
    }
    Regex("^(.+) is already checked in today\\.$").matchEntire(this)?.let { match ->
        return stringResource(R.string.habits_message_already_checked_in, match.groupValues[1])
    }
    Regex("^Reminder updated for (.+)\\.$").matchEntire(this)?.let { match ->
        return stringResource(R.string.habits_message_reminder_updated, match.groupValues[1])
    }
    return when (this) {
        "Habit was not found." -> stringResource(R.string.habits_message_missing)
        "Unable to load habits." -> stringResource(R.string.habits_load_error)
        else -> this
    }
}
