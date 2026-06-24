package com.example.lifelab.feature.habits.domain.model

sealed interface HabitCheckInResult {
    data class CheckedIn(val habit: Habit) : HabitCheckInResult

    data class AlreadyCheckedIn(val habit: Habit) : HabitCheckInResult

    data object HabitMissing : HabitCheckInResult
}
