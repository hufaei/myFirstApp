package com.example.lifelab.feature.habits.domain.model

import java.time.LocalTime

data class HabitReminder(
    val enabled: Boolean,
    val time: LocalTime?,
    val priority: HabitReminderPriority = HabitReminderPriority.Normal,
    val alarmClockEnabled: Boolean = false,
)

enum class HabitReminderPriority(
    val sortWeight: Int,
) {
    High(sortWeight = 0),
    Normal(sortWeight = 1),
    Low(sortWeight = 2),
}
