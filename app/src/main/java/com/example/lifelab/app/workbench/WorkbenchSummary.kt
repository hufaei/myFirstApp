package com.example.lifelab.app.workbench

import com.example.lifelab.feature.habits.presentation.HabitsUiState
import com.example.lifelab.feature.tasks.domain.TaskStatus
import com.example.lifelab.feature.tasks.presentation.TasksUiState

data class WorkbenchSummary(
    val activeTasks: Int,
    val completedTasks: Int,
    val totalHabits: Int,
    val checkedInToday: Int,
    val activeReminders: Int,
    val longestStreak: Int,
) {
    val taskLabel: String = "$activeTasks active"
    val completedLabel: String = "$completedTasks complete"
    val habitLabel: String = "$checkedInToday checked in"
    val reminderLabel: String = "$activeReminders reminders"
    val streakLabel: String = "$longestStreak day streak"
}

fun buildWorkbenchSummary(
    tasksState: TasksUiState,
    habitsState: HabitsUiState,
): WorkbenchSummary {
    val activeTasks = tasksState.tasks.count { it.status == TaskStatus.Active }
    val completedTasks = tasksState.tasks.count { it.status == TaskStatus.Completed }

    return WorkbenchSummary(
        activeTasks = activeTasks,
        completedTasks = completedTasks,
        totalHabits = habitsState.stats.totalHabits,
        checkedInToday = habitsState.stats.checkedInToday,
        activeReminders = habitsState.stats.activeReminders,
        longestStreak = habitsState.stats.longestStreak,
    )
}
