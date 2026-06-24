package com.example.lifelab.feature.habits.domain.model

data class HabitStats(
    val totalHabits: Int,
    val checkedInToday: Int,
    val activeReminders: Int,
    val longestStreak: Int,
)
