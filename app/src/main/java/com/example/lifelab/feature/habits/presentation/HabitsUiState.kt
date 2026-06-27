package com.example.lifelab.feature.habits.presentation

import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoOwnerType
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitStats
import java.time.LocalDate

enum class HabitsStatus {
    Loading,
    Content,
    Empty,
    Error,
}

data class HabitsUiState(
    val status: HabitsStatus = HabitsStatus.Loading,
    val habits: List<Habit> = emptyList(),
    val stats: HabitStats = HabitStats(
        totalHabits = 0,
        checkedInToday = 0,
        activeReminders = 0,
        longestStreak = 0,
    ),
    val habitPhotos: Map<String, List<PhotoRecord>> = emptyMap(),
    val today: LocalDate = LocalDate.now(),
    val message: HabitUiMessage? = null,
    val errorMessage: HabitUiMessage? = null,
) {
    fun photosForHabit(habitId: String): List<PhotoRecord> =
        habitPhotos[habitId].orEmpty().take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER)
}

sealed interface HabitUiMessage {
    data class CheckedIn(val habitName: String) : HabitUiMessage
    data class AlreadyCheckedIn(val habitName: String) : HabitUiMessage
    data class ReminderUpdated(val habitName: String) : HabitUiMessage
    data object Missing : HabitUiMessage
    data object LoadError : HabitUiMessage
}

fun habitPhotoOwner(habitId: String): PhotoOwner =
    PhotoOwner(type = PhotoOwnerType.Habit, id = habitId)
