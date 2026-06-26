package com.example.lifelab.feature.tasks.presentation

import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoOwnerType
import com.example.lifelab.core.media.PhotoRecord
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
    val taskPhotos: Map<String, List<PhotoRecord>> = emptyMap(),
    val editorPhotos: List<PhotoRecord> = emptyList(),
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

    fun photosForTask(taskId: String): List<PhotoRecord> =
        taskPhotos[taskId].orEmpty().take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER)
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

fun taskPhotoOwner(taskId: String): PhotoOwner =
    PhotoOwner(type = PhotoOwnerType.Task, id = taskId)
