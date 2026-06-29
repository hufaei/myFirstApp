package com.example.lifelab.feature.habits.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.model.HabitReminderPriority
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val frequency: String,
    @ColumnInfo(name = "streak_count") val streakCount: Int,
    @ColumnInfo(name = "last_check_in_date") val lastCheckInDate: String?,
    @ColumnInfo(name = "reminder_enabled") val reminderEnabled: Boolean,
    @ColumnInfo(name = "reminder_time_second_of_day") val reminderTimeSecondOfDay: Int?,
    @ColumnInfo(name = "reminder_priority") val reminderPriority: String,
    @ColumnInfo(name = "check_in_dates") val checkInDates: String,
)

fun HabitEntity.toDomain(): Habit =
    Habit(
        id = id,
        name = name,
        frequency = HabitFrequency.valueOf(frequency),
        streakCount = streakCount,
        lastCheckInDate = lastCheckInDate?.let(LocalDate::parse),
        reminder = HabitReminder(
            enabled = reminderEnabled,
            time = reminderTimeSecondOfDay?.let { LocalTime.ofSecondOfDay(it.toLong()) },
            priority = reminderPriority.toHabitReminderPriority(),
        ),
        checkInDates = decodeDates(checkInDates),
    )

fun Habit.toEntity(): HabitEntity =
    HabitEntity(
        id = id,
        name = name,
        frequency = frequency.name,
        streakCount = streakCount,
        lastCheckInDate = lastCheckInDate?.toString(),
        reminderEnabled = reminder.enabled,
        reminderTimeSecondOfDay = reminder.time?.toSecondOfDay(),
        reminderPriority = reminder.priority.name,
        checkInDates = encodeDates(checkInDates),
    )

private const val DATE_SEPARATOR = "\u001F"

private fun encodeDates(dates: Set<LocalDate>): String =
    dates.sorted().joinToString(DATE_SEPARATOR) { it.toString() }

private fun decodeDates(encoded: String): Set<LocalDate> =
    if (encoded.isBlank()) {
        emptySet()
    } else {
        encoded.split(DATE_SEPARATOR).map(LocalDate::parse).toSet()
    }

private fun String.toHabitReminderPriority(): HabitReminderPriority =
    enumValues<HabitReminderPriority>().firstOrNull { priority -> priority.name == this }
        ?: HabitReminderPriority.Normal
