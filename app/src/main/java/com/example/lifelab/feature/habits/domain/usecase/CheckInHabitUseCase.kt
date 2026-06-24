package com.example.lifelab.feature.habits.domain.usecase

import com.example.lifelab.feature.habits.domain.model.HabitCheckInResult
import com.example.lifelab.feature.habits.domain.repository.HabitRepository
import java.time.LocalDate

class CheckInHabitUseCase(
    private val repository: HabitRepository,
) {
    suspend operator fun invoke(
        habitId: String,
        checkInDate: LocalDate = LocalDate.now(),
    ): HabitCheckInResult {
        val habit = repository.getHabit(habitId) ?: return HabitCheckInResult.HabitMissing

        if (checkInDate in habit.checkInDates) {
            return HabitCheckInResult.AlreadyCheckedIn(habit)
        }

        val updatedCheckIns = habit.checkInDates + checkInDate
        val updatedHabit = habit.copy(
            checkInDates = updatedCheckIns,
            lastCheckInDate = updatedCheckIns.maxOrNull(),
            streakCount = updatedCheckIns.streakEndingAt(checkInDate),
        )

        return HabitCheckInResult.CheckedIn(repository.saveHabit(updatedHabit))
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
}
