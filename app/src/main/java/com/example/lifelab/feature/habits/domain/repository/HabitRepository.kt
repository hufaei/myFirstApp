package com.example.lifelab.feature.habits.domain.repository

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import kotlinx.coroutines.flow.Flow

interface HabitRepository {
    fun observeHabits(): Flow<List<Habit>>

    suspend fun getHabit(habitId: String): Habit?

    suspend fun saveHabit(habit: Habit): Habit

    suspend fun updateReminder(
        habitId: String,
        reminder: HabitReminder,
    ): Habit?
}
