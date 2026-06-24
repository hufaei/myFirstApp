package com.example.lifelab.feature.habits.data

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class InMemoryHabitRepositoryTest {

    @Test
    fun updateReminderChangesOnlyTheTargetHabitReminder() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(id = "hydrate", reminder = HabitReminder(enabled = false, time = null)),
                sampleHabit(id = "read", reminder = HabitReminder(enabled = true, time = LocalTime.of(20, 30))),
            ),
        )
        val updatedReminder = HabitReminder(enabled = true, time = LocalTime.of(7, 45))

        val updatedHabit = repository.updateReminder(habitId = "hydrate", reminder = updatedReminder)

        assertNotNull(updatedHabit)
        assertEquals(updatedReminder, updatedHabit.reminder)
        val habits = repository.observeHabits().first()
        assertEquals(updatedReminder, habits.single { it.id == "hydrate" }.reminder)
        assertEquals(LocalTime.of(20, 30), habits.single { it.id == "read" }.reminder.time)
    }

    @Test
    fun saveHabitReplacesOnlyTheMatchingHabit() = runTest {
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(id = "hydrate", reminder = HabitReminder(enabled = false, time = null)),
                sampleHabit(id = "read", reminder = HabitReminder(enabled = true, time = LocalTime.of(20, 30))),
            ),
        )
        val checkedInDate = LocalDate.of(2026, 6, 24)

        repository.saveHabit(
            sampleHabit(
                id = "hydrate",
                reminder = HabitReminder(enabled = false, time = null),
            ).copy(
                streakCount = 1,
                lastCheckInDate = checkedInDate,
                checkInDates = setOf(checkedInDate),
            ),
        )

        val habits = repository.observeHabits().first()
        assertEquals(1, habits.single { it.id == "hydrate" }.streakCount)
        assertEquals(0, habits.single { it.id == "read" }.streakCount)
    }

    private fun sampleHabit(
        id: String,
        reminder: HabitReminder,
    ) = Habit(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        frequency = HabitFrequency.Daily,
        streakCount = 0,
        lastCheckInDate = null,
        reminder = reminder,
    )
}
