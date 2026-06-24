package com.example.lifelab.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.tasks.data.InMemoryTaskRepository
import com.example.lifelab.feature.tasks.domain.CompleteTaskUseCase
import com.example.lifelab.feature.tasks.domain.CreateTaskUseCase
import com.example.lifelab.feature.tasks.domain.GetTaskDetailUseCase
import com.example.lifelab.feature.tasks.domain.ObserveTasksUseCase
import com.example.lifelab.feature.tasks.domain.RestoreTaskUseCase
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskChanges
import com.example.lifelab.feature.tasks.domain.TaskDraft
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.UpdateTaskUseCase
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskListViewModel(
    private val observeTasks: ObserveTasksUseCase,
    private val getTaskDetail: GetTaskDetailUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val restoreTask: RestoreTaskUseCase,
) : ViewModel() {

    constructor() : this(InMemoryTaskRepository())

    private constructor(repository: InMemoryTaskRepository) : this(
        observeTasks = ObserveTasksUseCase(repository),
        getTaskDetail = GetTaskDetailUseCase(repository),
        createTask = CreateTaskUseCase(repository),
        updateTask = UpdateTaskUseCase(repository),
        completeTask = CompleteTaskUseCase(repository),
        restoreTask = RestoreTaskUseCase(repository),
    )

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasks().collect { tasks ->
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        tasks = tasks,
                        selectedTask = state.selectedTask?.let { selected ->
                            tasks.firstOrNull { it.id == selected.id }
                        },
                    )
                }
            }
        }
    }

    fun selectFilter(filter: TaskFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
    }

    fun openDetail(taskId: String) {
        viewModelScope.launch {
            when (val result = getTaskDetail(taskId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedTask = result.value,
                            mode = TaskScreenMode.Detail,
                            message = null,
                        )
                    }
                }

                is AppResult.Failure -> showError(result.error.message)
            }
        }
    }

    fun startCreate() {
        _uiState.update {
            it.copy(
                selectedTask = null,
                mode = TaskScreenMode.Editor,
                editorState = TaskEditorState(),
                message = null,
            )
        }
    }

    fun startEdit() {
        val task = _uiState.value.selectedTask ?: return
        _uiState.update {
            it.copy(
                mode = TaskScreenMode.Editor,
                editorState = task.toEditorState(),
                message = null,
            )
        }
    }

    fun updateTitle(title: String) {
        updateEditor { it.copy(title = title) }
    }

    fun updateDescription(description: String) {
        updateEditor { it.copy(description = description) }
    }

    fun updatePriority(priority: TaskPriority) {
        updateEditor { it.copy(priority = priority) }
    }

    fun updateTags(tags: String) {
        updateEditor { it.copy(tags = tags) }
    }

    fun updateDueLabel(dueLabel: String) {
        updateEditor { it.copy(dueLabel = dueLabel) }
    }

    fun saveEditor() {
        val editor = _uiState.value.editorState
        viewModelScope.launch {
            val result = editor.editingTaskId?.let { taskId ->
                updateTask(
                    taskId,
                    TaskChanges(
                        title = editor.title,
                        description = editor.description,
                        priority = editor.priority,
                        dueAt = editor.dueLabel.toDueAt(),
                        shouldUpdateDueAt = true,
                        tags = editor.tagList(),
                    ),
                )
            } ?: createTask(
                TaskDraft(
                    title = editor.title,
                    description = editor.description,
                    priority = editor.priority,
                    dueAt = editor.dueLabel.toDueAt(),
                    tags = editor.tagList(),
                ),
            )

            when (result) {
                is AppResult.Success -> {
                    val isEditing = editor.editingTaskId != null
                    _uiState.update {
                        it.copy(
                            selectedTask = if (isEditing) result.value else null,
                            mode = if (isEditing) TaskScreenMode.Detail else TaskScreenMode.List,
                            editorState = TaskEditorState(),
                            message = if (isEditing) "Task updated" else "Task created",
                        )
                    }
                }

                is AppResult.Failure -> showError(result.error.message)
            }
        }
    }

    fun completeSelectedTask() {
        changeSelectedTaskStatus(completeTask::invoke, "Task completed")
    }

    fun completeOpenedTask() {
        completeSelectedTask()
    }

    fun restoreSelectedTask() {
        changeSelectedTaskStatus(restoreTask::invoke, "Task restored")
    }

    fun restoreOpenedTask() {
        restoreSelectedTask()
    }

    fun backToList() {
        _uiState.update {
            it.copy(
                mode = TaskScreenMode.List,
                selectedTask = null,
                editorState = TaskEditorState(),
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun changeSelectedTaskStatus(
        action: suspend (String) -> AppResult<Task>,
        successMessage: String,
    ) {
        val taskId = _uiState.value.selectedTask?.id ?: return
        viewModelScope.launch {
            when (val result = action(taskId)) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            selectedTask = result.value,
                            message = successMessage,
                        )
                    }
                }

                is AppResult.Failure -> showError(result.error.message)
            }
        }
    }

    private fun updateEditor(transform: (TaskEditorState) -> TaskEditorState) {
        _uiState.update { it.copy(editorState = transform(it.editorState)) }
    }

    private fun showError(message: String) {
        _uiState.update { it.copy(message = message) }
    }
}

private fun Task.toEditorState(): TaskEditorState {
    return TaskEditorState(
        editingTaskId = id,
        title = title,
        description = description,
        priority = priority,
        tags = tags.joinToString(", "),
        dueLabel = dueAt?.atZone(ZoneId.systemDefault())?.toLocalDate()?.toString().orEmpty(),
    )
}

private fun TaskEditorState.tagList(): List<String> {
    return tags.split(",")
        .map { it.trim() }
        .filter { it.isNotBlank() }
}

private fun String.toDueAt(): Instant? {
    val normalized = trim()
    if (normalized.isBlank()) {
        return null
    }
    return runCatching {
        LocalDate.parse(normalized)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
    }.getOrNull()
}
