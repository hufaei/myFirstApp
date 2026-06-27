package com.example.lifelab.feature.tasks.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.datastore.AppPreferencesRepository
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoRecordRepository
import com.example.lifelab.core.media.PhotoSource
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
import com.example.lifelab.feature.tasks.domain.TaskRepository
import com.example.lifelab.feature.tasks.domain.UpdateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskListViewModel(
    private val observeTasks: ObserveTasksUseCase,
    private val getTaskDetail: GetTaskDetailUseCase,
    private val createTask: CreateTaskUseCase,
    private val updateTask: UpdateTaskUseCase,
    private val completeTask: CompleteTaskUseCase,
    private val restoreTask: RestoreTaskUseCase,
    private val photoRecordRepository: PhotoRecordRepository? = null,
    private val appPreferencesRepository: AppPreferencesRepository? = null,
    private val nowMillis: () -> Long = { System.currentTimeMillis() },
) : ViewModel() {

    @Inject
    constructor(
        repository: TaskRepository,
        photoRecordRepository: PhotoRecordRepository,
        appPreferencesRepository: AppPreferencesRepository,
    ) : this(
        observeTasks = ObserveTasksUseCase(repository),
        getTaskDetail = GetTaskDetailUseCase(repository),
        createTask = CreateTaskUseCase(repository),
        updateTask = UpdateTaskUseCase(repository),
        completeTask = CompleteTaskUseCase(repository),
        restoreTask = RestoreTaskUseCase(repository),
        photoRecordRepository = photoRecordRepository,
        appPreferencesRepository = appPreferencesRepository,
    )

    constructor() : this(InMemoryTaskRepository(), { System.currentTimeMillis() })

    constructor(
        nowMillis: () -> Long,
    ) : this(InMemoryTaskRepository(), nowMillis)

    private constructor(
        repository: InMemoryTaskRepository,
        nowMillis: () -> Long,
    ) : this(
        observeTasks = ObserveTasksUseCase(repository),
        getTaskDetail = GetTaskDetailUseCase(repository),
        createTask = CreateTaskUseCase(repository),
        updateTask = UpdateTaskUseCase(repository),
        completeTask = CompleteTaskUseCase(repository),
        restoreTask = RestoreTaskUseCase(repository),
        nowMillis = nowMillis,
    )

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()
    private val photoPolicy = PhotoAttachmentPolicy()

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
        appPreferencesRepository?.let { repository ->
            viewModelScope.launch {
                repository.appPreferences.collect { preferences ->
                    _uiState.update { state ->
                        state.copy(selectedFilter = preferences.defaultTaskFilterName.toTaskFilter())
                    }
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
                    val photos = loadTaskPhotos(taskId)
                    _uiState.update {
                        it.copy(
                            selectedTask = result.value,
                            taskPhotos = if (photos.isEmpty()) {
                                it.taskPhotos
                            } else {
                                it.taskPhotos + (taskId to photos)
                            },
                            mode = TaskScreenMode.Detail,
                            message = null,
                        )
                    }
                }

                is AppResult.Failure -> showError(result.error)
            }
        }
    }

    fun startCreate() {
        _uiState.update {
            it.copy(
                selectedTask = null,
                mode = TaskScreenMode.Editor,
                editorState = TaskEditorState(),
                editorPhotos = emptyList(),
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
                editorPhotos = it.photosForTask(task.id),
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

    fun attachEditorPhotos(
        localUris: List<String>,
        source: PhotoSource,
    ) {
        val editor = _uiState.value.editorState
        val owner = taskPhotoOwner(editor.editingTaskId ?: DraftTaskPhotoOwnerId)
        _uiState.update { state ->
            state.copy(
                editorPhotos = state.editorPhotos.attachPhotos(
                    owner = owner,
                    localUris = localUris,
                    source = source,
                ),
            )
        }
    }

    fun attachSelectedTaskPhotos(
        localUris: List<String>,
        source: PhotoSource,
    ) {
        val taskId = _uiState.value.selectedTask?.id ?: return
        _uiState.update { state ->
            state.copy(
                taskPhotos = state.taskPhotos.attachPhotos(
                    owner = taskPhotoOwner(taskId),
                    localUris = localUris,
                    source = source,
                ),
            )
        }
        persistPhotos(
            owner = taskPhotoOwner(taskId),
            localUris = localUris,
            source = source,
        )
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
                    val savedPhotos = persistEditorPhotos(
                        taskId = result.value.id,
                        photos = _uiState.value.editorPhotos,
                    )
                    _uiState.update {
                        val visiblePhotos = savedPhotos.ifEmpty {
                            it.editorPhotos.reassignTo(taskPhotoOwner(result.value.id))
                        }
                        it.copy(
                            selectedTask = if (isEditing) result.value else null,
                            mode = if (isEditing) TaskScreenMode.Detail else TaskScreenMode.List,
                            editorState = TaskEditorState(),
                            editorPhotos = emptyList(),
                            taskPhotos = if (visiblePhotos.isEmpty()) {
                                it.taskPhotos
                            } else {
                                it.taskPhotos + (result.value.id to visiblePhotos)
                            },
                            message = if (isEditing) TaskUiMessage.Updated else TaskUiMessage.Created,
                        )
                    }
                }

                is AppResult.Failure -> showError(result.error)
            }
        }
    }

    fun completeSelectedTask() {
        changeSelectedTaskStatus(completeTask::invoke, TaskUiMessage.Completed)
    }

    fun completeOpenedTask() {
        completeSelectedTask()
    }

    fun restoreSelectedTask() {
        changeSelectedTaskStatus(restoreTask::invoke, TaskUiMessage.Restored)
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
                editorPhotos = emptyList(),
            )
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun changeSelectedTaskStatus(
        action: suspend (String) -> AppResult<Task>,
        successMessage: TaskUiMessage,
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

                is AppResult.Failure -> showError(result.error)
            }
        }
    }

    private fun updateEditor(transform: (TaskEditorState) -> TaskEditorState) {
        _uiState.update { it.copy(editorState = transform(it.editorState)) }
    }

    private fun showError(error: AppError) {
        val message = when (error) {
            is AppError.Network,
            is AppError.Storage,
            is AppError.Unknown,
            is AppError.Validation -> TaskUiMessage.Error
        }
        _uiState.update { it.copy(message = message) }
    }

    private suspend fun loadTaskPhotos(taskId: String): List<PhotoRecord> =
        photoRecordRepository
            ?.observePhotoRecords(taskPhotoOwner(taskId))
            ?.first()
            .orEmpty()

    private suspend fun persistEditorPhotos(
        taskId: String,
        photos: List<PhotoRecord>,
    ): List<PhotoRecord> {
        val repository = photoRecordRepository ?: return emptyList()
        val owner = taskPhotoOwner(taskId)
        val existingPhotos = repository.observePhotoRecords(owner).first()
        val existingPhotoUris = existingPhotos.map(PhotoRecord::localUri).toSet()
        val newPhotos = photos.filterNot { photo -> photo.localUri in existingPhotoUris }
        val persisted = newPhotos.mapNotNull { photo ->
            when (
                val result = repository.addPhotoRecord(
                    owner = owner,
                    localUri = photo.localUri,
                    source = photo.source,
                    createdAtMillis = photo.createdAtMillis,
                )
            ) {
                is AppResult.Success -> result.value
                else -> null
            }
        }
        return (existingPhotos + persisted).take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER)
    }

    private fun persistPhotos(
        owner: PhotoOwner,
        localUris: List<String>,
        source: PhotoSource,
    ) {
        val repository = photoRecordRepository ?: return
        viewModelScope.launch {
            localUris.filter { it.isNotBlank() }.forEach { localUri ->
                repository.addPhotoRecord(
                    owner = owner,
                    localUri = localUri,
                    source = source,
                    createdAtMillis = nowMillis(),
                )
            }
        }
    }

    private fun List<PhotoRecord>.attachPhotos(
        owner: PhotoOwner,
        localUris: List<String>,
        source: PhotoSource,
    ): List<PhotoRecord> {
        val trimmedUris = photoPolicy.trimToAvailableSlots(
            owner = owner,
            existingRecords = this,
            candidates = localUris.filter { it.isNotBlank() },
        )
        if (trimmedUris.isEmpty()) {
            return this
        }
        val nextOrder = size
        return this + trimmedUris.mapIndexed { index, localUri ->
            val createdAtMillis = nowMillis()
            PhotoRecord(
                id = "photo-${owner.type.storageSegment}-${owner.id}-$createdAtMillis-${nextOrder + index}",
                owner = owner,
                localUri = localUri,
                source = source,
                sortOrder = nextOrder + index,
                createdAtMillis = createdAtMillis,
            )
        }
    }

    private fun Map<String, List<PhotoRecord>>.attachPhotos(
        owner: PhotoOwner,
        localUris: List<String>,
        source: PhotoSource,
    ): Map<String, List<PhotoRecord>> {
        val updatedPhotos = this[owner.id].orEmpty().attachPhotos(
            owner = owner,
            localUris = localUris,
            source = source,
        )
        return this + (owner.id to updatedPhotos)
    }

    private fun List<PhotoRecord>.reassignTo(owner: PhotoOwner): List<PhotoRecord> =
        take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER).mapIndexed { index, photo ->
            photo.copy(
                id = "photo-${owner.type.storageSegment}-${owner.id}-${photo.createdAtMillis}-$index",
                owner = owner,
                sortOrder = index,
            )
        }

    private companion object {
        const val DraftTaskPhotoOwnerId = "draft-task-editor"
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

private fun String.toTaskFilter(): TaskFilter =
    enumValues<TaskFilter>().firstOrNull { it.name == this } ?: TaskFilter.All
