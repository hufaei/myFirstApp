package com.example.lifelab.feature.tasks.presentation

import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus

data class TasksUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val selectedFilter: TaskFilter = TaskFilter.All,
    val selectedTask: Task? = null,
    val mode: TaskScreenMode = TaskScreenMode.List,
    val editorState: TaskEditorState = TaskEditorState(),
    val message: String? = null,
) {
    val filteredTasks: List<Task>
        get() = tasks.filter { task ->
            when (selectedFilter) {
                TaskFilter.All -> true
                TaskFilter.Active -> task.status == TaskStatus.Active
                TaskFilter.Completed -> task.status == TaskStatus.Completed
            }
        }
}

enum class TaskFilter {
    All,
    Active,
    Completed,
}

enum class TaskScreenMode {
    List,
    Detail,
    Editor,
}

data class TaskEditorState(
    val editingTaskId: String? = null,
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.Medium,
    val tags: String = "",
    val dueLabel: String = "",
)
