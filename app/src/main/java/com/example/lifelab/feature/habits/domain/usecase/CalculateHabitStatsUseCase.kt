package com.example.lifelab.feature.habits.domain.usecase

import com.example.lifelab.feature.habits.domain.model.Habit
import com.example.lifelab.feature.habits.domain.model.HabitStats
import java.time.LocalDate

class CalculateHabitStatsUseCase {
    operator fun invoke(
        habits: List<Habit>,
        today: LocalDate = LocalDate.now(),
    ): HabitStats = HabitStats(
        totalHabits = habits.size,
        checkedInToday = habits.count { today in it.checkInDates },
        activeReminders = habits.count { it.reminder.enabled },
        longestStreak = habits.maxOfOrNull { it.streakCount } ?: 0,
    )
}
