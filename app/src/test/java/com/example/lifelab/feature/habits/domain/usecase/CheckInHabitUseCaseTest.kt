package com.example.lifelab.feature.habits.domain.usecase

import com.example.lifelab.feature.habits.data.InMemoryHabitRepository
import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitCheckInResult
import com.example.lifelab.feature.habits.domain.model.HabitFrequency
import com.example.lifelab.feature.habits.domain.model.HabitReminder
import java.time.LocalDate
import java.time.LocalTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class CheckInHabitUseCaseTest {

    @Test
    fun firstCheckInSetsLastDateAndStartsStreakAtOne() = runTest {
        val today = LocalDate.of(2026, 6, 24)
        val repository = InMemoryHabitRepository(initialHabits = listOf(sampleHabit()))
        val useCase = CheckInHabitUseCase(repository)

        val result = useCase(habitId = "hydrate", checkInDate = today)

        val checkedIn = assertIs<HabitCheckInResult.CheckedIn>(result)
        assertEquals(today, checkedIn.habit.lastCheckInDate)
        assertEquals(1, checkedIn.habit.streakCount)
    }

    @Test
    fun duplicateCheckInOnSameDayReturnsAlreadyCheckedInAndLeavesStreakUnchanged() = runTest {
        val today = LocalDate.of(2026, 6, 24)
        val repository = InMemoryHabitRepository(initialHabits = listOf(sampleHabit()))
        val useCase = CheckInHabitUseCase(repository)

        useCase(habitId = "hydrate", checkInDate = today)
        val duplicate = useCase(habitId = "hydrate", checkInDate = today)

        val alreadyCheckedIn = assertIs<HabitCheckInResult.AlreadyCheckedIn>(duplicate)
        assertEquals(today, alreadyCheckedIn.habit.lastCheckInDate)
        assertEquals(1, alreadyCheckedIn.habit.streakCount)
        assertEquals(1, repository.observeHabits().first().single().checkInDates.size)
    }

    @Test
    fun checkInAfterYesterdayContinuesStreak() = runTest {
        val yesterday = LocalDate.of(2026, 6, 23)
        val today = LocalDate.of(2026, 6, 24)
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    streakCount = 1,
                    lastCheckInDate = yesterday,
                    checkInDates = setOf(yesterday),
                ),
            ),
        )
        val useCase = CheckInHabitUseCase(repository)

        val result = useCase(habitId = "hydrate", checkInDate = today)

        val checkedIn = assertIs<HabitCheckInResult.CheckedIn>(result)
        assertEquals(today, checkedIn.habit.lastCheckInDate)
        assertEquals(2, checkedIn.habit.streakCount)
    }

    @Test
    fun checkInAfterMissedDayRestartsStreakAtOne() = runTest {
        val twoDaysAgo = LocalDate.of(2026, 6, 22)
        val today = LocalDate.of(2026, 6, 24)
        val repository = InMemoryHabitRepository(
            initialHabits = listOf(
                sampleHabit(
                    streakCount = 3,
                    lastCheckInDate = twoDaysAgo,
                    checkInDates = setOf(
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 21),
                        twoDaysAgo,
                    ),
                ),
            ),
        )
        val useCase = CheckInHabitUseCase(repository)

        val result = useCase(habitId = "hydrate", checkInDate = today)

        val checkedIn = assertIs<HabitCheckInResult.CheckedIn>(result)
        assertEquals(today, checkedIn.habit.lastCheckInDate)
        assertEquals(1, checkedIn.habit.streakCount)
    }

    @Test
    fun missingHabitReturnsHabitMissing() = runTest {
        val repository = InMemoryHabitRepository(initialHabits = listOf(sampleHabit()))
        val useCase = CheckInHabitUseCase(repository)

        val result = useCase(habitId = "missing", checkInDate = LocalDate.of(2026, 6, 24))

        assertEquals(HabitCheckInResult.HabitMissing, result)
    }

    private fun sampleHabit(
        streakCount: Int = 0,
        lastCheckInDate: LocalDate? = null,
        checkInDates: Set<LocalDate> = emptySet(),
    ) = Habit(
        id = "hydrate",
        name = "Drink water",
        frequency = HabitFrequency.Daily,
        streakCount = streakCount,
        lastCheckInDate = lastCheckInDate,
        reminder = HabitReminder(enabled = true, time = LocalTime.of(9, 0)),
        checkInDates = checkInDates,
    )
}
