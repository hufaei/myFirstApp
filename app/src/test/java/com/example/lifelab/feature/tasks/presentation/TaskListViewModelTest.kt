package com.example.lifelab.feature.tasks.presentation

import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.InMemoryAppPreferencesRepository
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoOwner
import com.example.lifelab.core.media.PhotoOwnerType
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoRecordRepository
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.feature.tasks.data.InMemoryTaskRepository
import com.example.lifelab.feature.tasks.domain.CompleteTaskUseCase
import com.example.lifelab.feature.tasks.domain.CreateTaskUseCase
import com.example.lifelab.feature.tasks.domain.GetTaskDetailUseCase
import com.example.lifelab.feature.tasks.domain.ObserveTasksUseCase
import com.example.lifelab.feature.tasks.domain.RestoreTaskUseCase
import com.example.lifelab.feature.tasks.domain.TaskStatus
import com.example.lifelab.feature.tasks.domain.UpdateTaskUseCase
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialLoadExposesSeededTasksAndAllFilter() = runTest {
        val viewModel = TaskListViewModel()

        val state = viewModel.uiState.value

        assertEquals(TaskFilter.All, state.selectedFilter)
        assertEquals(TaskScreenMode.List, state.mode)
        assertEquals(3, state.tasks.size)
        assertEquals(state.tasks, state.filteredTasks)
        assertTrue(state.tasks.any { it.title == "规划每周复盘" })
    }

    @Test
    fun switchingToCompletedFilterShowsCompletedTasks() = runTest {
        val viewModel = TaskListViewModel()

        viewModel.selectFilter(TaskFilter.Completed)

        val state = viewModel.uiState.value
        assertEquals(TaskFilter.Completed, state.selectedFilter)
        assertTrue(state.filteredTasks.isNotEmpty())
        assertTrue(state.filteredTasks.all { it.status == TaskStatus.Completed })
    }

    @Test
    fun creatingTaskFromEditorAddsActiveTaskAndReturnsToListWithSuccessMessage() = runTest {
        var clock = 1_000L
        val viewModel = TaskListViewModel(nowMillis = { clock++ })

        viewModel.startCreate()
        viewModel.updateTitle("  Draft quarterly review  ")
        viewModel.updateDescription("Write the first pass")
        viewModel.updatePriority(com.example.lifelab.feature.tasks.domain.TaskPriority.High)
        viewModel.updateTags("work, review")
        viewModel.updateDueLabel("2026-06-30")
        viewModel.attachEditorPhotos(
            localUris = listOf("content://task/1", "content://task/2", "content://task/3", "content://task/4"),
            source = PhotoSource.Picker,
        )
        viewModel.saveEditor()

        val state = viewModel.uiState.value
        val created = assertNotNull(state.tasks.firstOrNull { it.title == "Draft quarterly review" })
        assertEquals(TaskStatus.Active, created.status)
        assertEquals(listOf("work", "review"), created.tags)
        assertEquals(
            "2026-06-30",
            created.dueAt?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ISO_LOCAL_DATE),
        )
        assertEquals(TaskScreenMode.List, state.mode)
        assertEquals(TaskUiMessage.Created, state.message)
        val photos = state.photosForTask(created.id)
        assertEquals(3, photos.size)
        assertEquals(listOf("content://task/1", "content://task/2", "content://task/3"), photos.map { it.localUri })
        assertEquals(PhotoOwnerType.Task, photos.first().owner.type)
        assertEquals(created.id, photos.first().owner.id)
    }

    @Test
    fun editingSelectedTaskUpdatesTitleDescriptionAndDetailListState() = runTest {
        val viewModel = TaskListViewModel()
        val taskId = viewModel.uiState.value.tasks.first().id

        viewModel.openDetail(taskId)
        viewModel.startEdit()
        viewModel.updateTitle("Updated weekly reset")
        viewModel.updateDescription("Updated description")
        viewModel.updateDueLabel("")
        viewModel.saveEditor()

        val detailState = viewModel.uiState.value
        assertEquals(TaskScreenMode.Detail, detailState.mode)
        assertEquals("Updated weekly reset", detailState.selectedTask?.title)
        assertEquals("Updated description", detailState.selectedTask?.description)
        assertEquals(null, detailState.selectedTask?.dueAt)

        viewModel.backToList()

        val listState = viewModel.uiState.value
        assertEquals(TaskScreenMode.List, listState.mode)
        assertEquals("Updated weekly reset", listState.tasks.first { it.id == taskId }.title)
        assertEquals(TaskUiMessage.Updated, listState.message)
    }

    @Test
    fun completingSelectedActiveTaskChangesItToCompleted() = runTest {
        val viewModel = TaskListViewModel()
        val activeTaskId = viewModel.uiState.value.tasks.first { it.status == TaskStatus.Active }.id

        viewModel.openDetail(activeTaskId)
        viewModel.completeSelectedTask()

        val state = viewModel.uiState.value
        assertEquals(TaskStatus.Completed, state.selectedTask?.status)
        assertEquals(TaskStatus.Completed, state.tasks.first { it.id == activeTaskId }.status)
        assertEquals(TaskUiMessage.Completed, state.message)
    }

    @Test
    fun restoringSelectedCompletedTaskChangesItToActive() = runTest {
        val viewModel = TaskListViewModel()
        val completedTaskId = viewModel.uiState.value.tasks.first { it.status == TaskStatus.Completed }.id

        viewModel.openDetail(completedTaskId)
        viewModel.restoreSelectedTask()

        val state = viewModel.uiState.value
        assertEquals(TaskStatus.Active, state.selectedTask?.status)
        assertEquals(TaskStatus.Active, state.tasks.first { it.id == completedTaskId }.status)
        assertEquals(TaskUiMessage.Restored, state.message)
    }

    @Test
    fun appPreferencesSelectDefaultTaskFilter() = runTest {
        val preferencesRepository = InMemoryAppPreferencesRepository(
            AppPreferences(defaultTaskFilterName = "Completed"),
        )
        val viewModel = testViewModel(appPreferencesRepository = preferencesRepository)
        advanceUntilIdle()

        assertEquals(TaskFilter.Completed, viewModel.uiState.value.selectedFilter)

        preferencesRepository.updateDefaultTaskFilterName("Active")
        advanceUntilIdle()

        assertEquals(TaskFilter.Active, viewModel.uiState.value.selectedFilter)
    }

    @Test
    fun editingTaskWithExistingPhotosOnlyPersistsNewPhotos() = runTest {
        val photoRepository = FakePhotoRecordRepository()
        val viewModel = testViewModel(photoRecordRepository = photoRepository)
        advanceUntilIdle()
        val taskId = viewModel.uiState.value.tasks.first().id

        viewModel.openDetail(taskId)
        advanceUntilIdle()
        viewModel.attachSelectedTaskPhotos(listOf("content://task/existing"), PhotoSource.Picker)
        advanceUntilIdle()
        viewModel.startEdit()
        viewModel.attachEditorPhotos(listOf("content://task/new"), PhotoSource.Picker)
        viewModel.saveEditor()
        advanceUntilIdle()

        assertEquals(
            listOf("content://task/existing", "content://task/new"),
            photoRepository.recordsFor(taskPhotoOwner(taskId)).map { it.localUri },
        )
    }

    private fun testViewModel(
        photoRecordRepository: PhotoRecordRepository? = null,
        appPreferencesRepository: InMemoryAppPreferencesRepository? = null,
    ): TaskListViewModel {
        val repository = InMemoryTaskRepository()
        return TaskListViewModel(
            observeTasks = ObserveTasksUseCase(repository),
            getTaskDetail = GetTaskDetailUseCase(repository),
            createTask = CreateTaskUseCase(repository),
            updateTask = UpdateTaskUseCase(repository),
            completeTask = CompleteTaskUseCase(repository),
            restoreTask = RestoreTaskUseCase(repository),
            photoRecordRepository = photoRecordRepository,
            appPreferencesRepository = appPreferencesRepository,
        )
    }
}

private class FakePhotoRecordRepository : PhotoRecordRepository {
    private val records = MutableStateFlow(emptyList<PhotoRecord>())
    private val policy = PhotoAttachmentPolicy()

    override fun observePhotoRecords(owner: PhotoOwner): Flow<List<PhotoRecord>> =
        records.map { allRecords -> allRecords.filter { it.owner == owner } }

    override suspend fun addPhotoRecord(
        owner: PhotoOwner,
        localUri: String,
        source: PhotoSource,
        createdAtMillis: Long,
    ): AppResult<PhotoRecord> {
        val ownerRecords = recordsFor(owner)
        if (!policy.canAttach(owner, ownerRecords)) {
            return AppResult.Failure(AppError.Validation("Too many photos"))
        }
        val record = PhotoRecord(
            id = "persisted-${records.value.size}",
            owner = owner,
            localUri = localUri,
            source = source,
            sortOrder = ownerRecords.size,
            createdAtMillis = createdAtMillis,
        )
        records.value = records.value + record
        return AppResult.Success(record)
    }

    override suspend fun deletePhotoRecord(id: String) {
        records.value = records.value.filterNot { it.id == id }
    }

    override suspend fun deletePhotoRecordsForOwner(owner: PhotoOwner) {
        records.value = records.value.filterNot { it.owner == owner }
    }

    fun recordsFor(owner: PhotoOwner): List<PhotoRecord> =
        records.value.filter { it.owner == owner }
}
