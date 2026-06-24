package com.example.lifelab.feature.tasks.presentation

import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
        assertTrue(state.tasks.any { it.title == "Plan the weekly reset" })
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
        val viewModel = TaskListViewModel()

        viewModel.startCreate()
        viewModel.updateTitle("  Draft quarterly review  ")
        viewModel.updateDescription("Write the first pass")
        viewModel.updatePriority(com.example.lifelab.feature.tasks.domain.TaskPriority.High)
        viewModel.updateTags("work, review")
        viewModel.updateDueLabel("2026-06-30")
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
        assertEquals("Task created", state.message)
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
        assertEquals("Task updated", listState.message)
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
    }
}
