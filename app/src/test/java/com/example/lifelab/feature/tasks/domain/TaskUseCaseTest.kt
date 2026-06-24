package com.example.lifelab.feature.tasks.domain

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.tasks.data.InMemoryTaskRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TaskUseCaseTest {

    @Test
    fun createRejectsBlankTitleWithValidationFailure() = runTest {
        val repository = InMemoryTaskRepository()
        val createTask = CreateTaskUseCase(repository)

        val result = createTask(TaskDraft(title = "   "))

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
    }

    @Test
    fun createStoresValidTaskAndEmitsIt() = runTest {
        val repository = InMemoryTaskRepository()
        val createTask = CreateTaskUseCase(repository)
        val observeTasks = ObserveTasksUseCase(repository)

        val result = createTask(
            TaskDraft(
                title = "Prepare weekly plan",
                description = "Review priorities before Monday",
                priority = TaskPriority.High,
                tags = listOf("planning", "work"),
            ),
        )

        val created = assertIs<AppResult.Success<Task>>(result).value
        val emittedTasks = observeTasks().first { tasks ->
            tasks.any { it.id == created.id }
        }

        val emitted = emittedTasks.first { it.id == created.id }
        assertEquals("Prepare weekly plan", emitted.title)
        assertEquals("Review priorities before Monday", emitted.description)
        assertEquals(TaskStatus.Active, emitted.status)
        assertEquals(TaskPriority.High, emitted.priority)
        assertEquals(listOf("planning", "work"), emitted.tags)
    }

    @Test
    fun updateMissingTaskReturnsStorageFailure() = runTest {
        val repository = InMemoryTaskRepository()
        val updateTask = UpdateTaskUseCase(repository)

        val result = updateTask(
            id = "missing-task",
            changes = TaskChanges(title = "Updated title"),
        )

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Storage>(failure.error)
    }

    @Test
    fun updateRejectsBlankTitleWithValidationFailure() = runTest {
        val repository = InMemoryTaskRepository()
        val createTask = CreateTaskUseCase(repository)
        val updateTask = UpdateTaskUseCase(repository)
        val created = assertIs<AppResult.Success<Task>>(
            createTask(TaskDraft(title = "Review weekly plan")),
        ).value

        val result = updateTask(
            id = created.id,
            changes = TaskChanges(title = "   "),
        )

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Validation>(failure.error)
    }

    @Test
    fun completeMissingTaskReturnsStorageFailure() = runTest {
        val repository = InMemoryTaskRepository()
        val completeTask = CompleteTaskUseCase(repository)

        val result = completeTask("missing-task")

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Storage>(failure.error)
    }

    @Test
    fun completeTransitionsActiveTaskToCompleted() = runTest {
        val repository = InMemoryTaskRepository()
        val createTask = CreateTaskUseCase(repository)
        val completeTask = CompleteTaskUseCase(repository)
        val getTaskDetail = GetTaskDetailUseCase(repository)
        val created = assertIs<AppResult.Success<Task>>(
            createTask(TaskDraft(title = "Submit expense report")),
        ).value

        val result = completeTask(created.id)

        val completed = assertIs<AppResult.Success<Task>>(result).value
        assertEquals(TaskStatus.Completed, completed.status)
        val stored = assertIs<AppResult.Success<Task>>(getTaskDetail(created.id)).value
        assertEquals(TaskStatus.Completed, stored.status)
    }

    @Test
    fun restoreTransitionsCompletedTaskToActive() = runTest {
        val repository = InMemoryTaskRepository()
        val createTask = CreateTaskUseCase(repository)
        val completeTask = CompleteTaskUseCase(repository)
        val restoreTask = RestoreTaskUseCase(repository)
        val created = assertIs<AppResult.Success<Task>>(
            createTask(TaskDraft(title = "Confirm travel booking")),
        ).value
        assertIs<AppResult.Success<Task>>(completeTask(created.id))

        val result = restoreTask(created.id)

        val restored = assertIs<AppResult.Success<Task>>(result).value
        assertEquals(TaskStatus.Active, restored.status)
        assertNotNull(restored.updatedAt)
        assertTrue(restored.updatedAt >= created.updatedAt)
    }
}
