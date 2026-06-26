package com.example.lifelab.feature.habits.data

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RoomHabitRepositoryTest {

    @Test
    fun saveHabitPersistsAndEmitsMappedHabit() = runTest {
        val dao = FakeHabitDao()
        val repository = RoomHabitRepository(dao)
        val checkInDate = LocalDate.of(2026, 6, 24)

        val saved = repository.saveHabit(
            Habit(
                id = "hydrate",
                name = "Drink water",
                frequency = HabitFrequency.Daily,
                streakCount = 1,
                lastCheckInDate = checkInDate,
                reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
                checkInDates = setOf(checkInDate),
            ),
        )

        assertEquals(saved, dao.getHabit("hydrate")?.toDomain())
        assertEquals(listOf(saved), repository.observeHabits().first())
    }

    @Test
    fun updateReminderReturnsNullWhenHabitIsMissing() = runTest {
        val repository = RoomHabitRepository(FakeHabitDao())

        val updated = repository.updateReminder(
            habitId = "missing",
            reminder = HabitReminder(enabled = true, time = LocalTime.NOON),
        )

        assertEquals(null, updated)
    }

    private class FakeHabitDao : HabitDao {
        private val entities = MutableStateFlow(emptyList<HabitEntity>())

        override fun observeHabits(): Flow<List<HabitEntity>> = entities

        override suspend fun getHabit(id: String): HabitEntity? =
            entities.value.firstOrNull { it.id == id }

        override suspend fun upsertHabit(habit: HabitEntity) {
            entities.value = entities.value.filterNot { it.id == habit.id } + habit
        }

        override suspend fun countHabits(): Int = entities.value.size
    }
}
