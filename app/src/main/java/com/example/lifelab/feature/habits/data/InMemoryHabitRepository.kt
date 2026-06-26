package com.example.lifelab.feature.habits.data

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class InMemoryHabitRepository(
    initialHabits: List<Habit> = sampleHabits(),
) : HabitRepository {
    private val habits = MutableStateFlow(initialHabits.map { it.withDerivedStreak() })

    override fun observeHabits(): Flow<List<Habit>> = habits.asStateFlow()

    override suspend fun getHabit(habitId: String): Habit? =
        habits.value.firstOrNull { it.id == habitId }

    override suspend fun saveHabit(updatedHabit: Habit): Habit {
        val currentHabits = habits.value
        habits.value = currentHabits.map { existingHabit ->
            if (existingHabit.id == updatedHabit.id) updatedHabit else existingHabit
        }

        return updatedHabit
    }

    override suspend fun updateReminder(
        habitId: String,
        reminder: HabitReminder,
    ): Habit? {
        val currentHabits = habits.value
        val targetHabit = currentHabits.firstOrNull { it.id == habitId } ?: return null
        val updatedHabit = targetHabit.copy(reminder = reminder)

        habits.value = currentHabits.map { habit ->
            if (habit.id == habitId) updatedHabit else habit
        }

        return updatedHabit
    }

    private fun Habit.withDerivedStreak(): Habit {
        val latestCheckIn = checkInDates.maxOrNull()
        if (latestCheckIn == null) {
            return copy(streakCount = 0, lastCheckInDate = null)
        }

        return copy(
            streakCount = checkInDates.streakEndingAt(latestCheckIn),
            lastCheckInDate = latestCheckIn,
        )
    }

    private fun Set<LocalDate>.streakEndingAt(date: LocalDate): Int {
        var streak = 0
        var cursor = date

        while (cursor in this) {
            streak += 1
            cursor = cursor.minusDays(1)
        }

        return streak
    }

    private companion object {
        fun sampleHabits() = listOf(
        Habit(
            id = "hydrate",
            name = "喝水",
            frequency = HabitFrequency.Daily,
                streakCount = 0,
                lastCheckInDate = null,
                reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
            ),
        Habit(
            id = "walk",
            name = "晚间散步",
            frequency = HabitFrequency.Daily,
                streakCount = 0,
                lastCheckInDate = null,
                reminder = HabitReminder(enabled = false, time = null),
            ),
        )
    }
}
