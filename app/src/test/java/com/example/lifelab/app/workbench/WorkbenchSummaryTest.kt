package com.example.lifelab.app.workbench

import com.example.lifelab.feature.habits.domain.model.HabitStats
import com.example.lifelab.feature.habits.presentation.HabitsUiState
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
import com.example.lifelab.feature.tasks.presentation.TasksUiState
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class WorkbenchSummaryTest {

    @Test
    fun summaryUsesTaskAndHabitState() {
        val summary = buildWorkbenchSummary(
            tasksState = TasksUiState(
                isLoading = false,
                tasks = listOf(
                    task(id = "active", status = TaskStatus.Active),
                    task(id = "done", status = TaskStatus.Completed),
                ),
            ),
            habitsState = HabitsUiState(
                stats = HabitStats(
                    totalHabits = 3,
                    checkedInToday = 1,
                    activeReminders = 2,
                    longestStreak = 6,
                ),
            ),
        )

        assertEquals("1 active", summary.taskLabel)
        assertEquals("1 checked in", summary.habitLabel)
        assertEquals("6 day streak", summary.streakLabel)
    }

    private fun task(
        id: String,
        status: TaskStatus,
    ): Task {
        val now = Instant.parse("2026-06-24T00:00:00Z")
        return Task(
            id = id,
            title = id,
            description = "",
            status = status,
            priority = TaskPriority.Medium,
            dueAt = null,
            tags = emptyList(),
            createdAt = now,
            updatedAt = now,
        )
    }
}
