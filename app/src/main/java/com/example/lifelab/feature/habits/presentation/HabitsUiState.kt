package com.example.lifelab.feature.habits.presentation

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitStats

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
    val message: String? = null,
    val errorMessage: String? = null,
)
