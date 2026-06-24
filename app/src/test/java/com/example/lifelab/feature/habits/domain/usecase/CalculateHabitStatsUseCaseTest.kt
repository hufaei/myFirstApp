package com.example.lifelab.feature.habits.domain.usecase

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculateHabitStatsUseCaseTest {

    @Test
    fun calculatesSummaryCountsFromHabits() {
        val today = LocalDate.of(2026, 6, 24)
        val useCase = CalculateHabitStatsUseCase()

        val stats = useCase(
            habits = listOf(
                sampleHabit(
                    id = "hydrate",
                    streakCount = 4,
                    reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
                    checkInDates = setOf(today),
                ),
                sampleHabit(
                    id = "read",
                    streakCount = 2,
                    reminder = HabitReminder(enabled = false, time = null),
                    checkInDates = setOf(today.minusDays(1)),
                ),
            ),
            today = today,
        )

        assertEquals(2, stats.totalHabits)
        assertEquals(1, stats.checkedInToday)
        assertEquals(1, stats.activeReminders)
        assertEquals(4, stats.longestStreak)
    }

    private fun sampleHabit(
        id: String,
        streakCount: Int,
        reminder: HabitReminder,
        checkInDates: Set<LocalDate>,
    ) = Habit(
        id = id,
        name = id.replaceFirstChar { it.uppercase() },
        frequency = HabitFrequency.Daily,
        streakCount = streakCount,
        lastCheckInDate = checkInDates.maxOrNull(),
        reminder = reminder,
        checkInDates = checkInDates,
    )
}
