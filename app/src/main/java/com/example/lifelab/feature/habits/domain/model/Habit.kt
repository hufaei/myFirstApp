package com.example.lifelab.feature.habits.domain.model

import java.time.LocalDate

data class Habit(
    val id: String,
    val name: String,
    val frequency: HabitFrequency,
    val streakCount: Int,
    val lastCheckInDate: LocalDate?,
    val reminder: HabitReminder,
    val checkInDates: Set<LocalDate> = emptySet(),
)
