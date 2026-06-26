package com.example.lifelab.feature.habits.data

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomHabitRepository(
    private val habitDao: HabitDao,
) : HabitRepository {

    override fun observeHabits(): Flow<List<Habit>> =
        habitDao.observeHabits()
            .onStart { seedIfEmpty() }
            .map { habits -> habits.map(HabitEntity::toDomain) }

    override suspend fun getHabit(habitId: String): Habit? =
        habitDao.getHabit(habitId)?.toDomain()

    override suspend fun saveHabit(habit: Habit): Habit {
        habitDao.upsertHabit(habit.toEntity())
        return habit
    }

    override suspend fun updateReminder(
        habitId: String,
        reminder: HabitReminder,
    ): Habit? {
        val habit = habitDao.getHabit(habitId)?.toDomain() ?: return null
        val updatedHabit = habit.copy(reminder = reminder)
        habitDao.upsertHabit(updatedHabit.toEntity())
        return updatedHabit
    }

    suspend fun seedIfEmpty() {
        if (habitDao.countHabits() == 0) {
            sampleHabits().forEach { habitDao.upsertHabit(it.toEntity()) }
        }
    }

    private companion object {
        fun sampleHabits(): List<Habit> =
            listOf(
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
