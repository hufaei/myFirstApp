package com.example.lifelab.feature.tasks.data

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskChanges
import com.example.lifelab.feature.tasks.domain.TaskDraft
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RoomTaskRepositoryTest {

    @Test
    fun createTaskPersistsNormalizedDraftThroughDao() = runTest {
        val dao = FakeTaskDao()
        val repository = RoomTaskRepository(
            taskDao = dao,
            now = { Instant.parse("2026-06-24T10:15:30Z") },
            idGenerator = { "generated-task" },
        )

        val result = repository.createTask(
            TaskDraft(
                title = "  Weekly reset  ",
                description = "  Pick priorities  ",
                priority = TaskPriority.High,
                tags = listOf(" home ", "", "home", "health"),
            ),
        )

        val created = assertIs<AppResult.Success<Task>>(result).value
        assertEquals("generated-task", created.id)
        assertEquals("Weekly reset", created.title)
        assertEquals("Pick priorities", created.description)
        assertEquals(listOf("home", "health"), created.tags)
        assertEquals(created, dao.getTask("generated-task")?.toDomain())
    }

    @Test
    fun updateMissingTaskReturnsStorageFailure() = runTest {
        val repository = RoomTaskRepository(taskDao = FakeTaskDao())

        val result = repository.updateTask(
            id = "missing",
            changes = TaskChanges(title = "Updated"),
        )

        val failure = assertIs<AppResult.Failure>(result)
        assertIs<AppError.Storage>(failure.error)
    }

    private class FakeTaskDao : TaskDao {
        private val entities = MutableStateFlow(emptyList<TaskEntity>())

        override fun observeTasks(): Flow<List<TaskEntity>> = entities

        override suspend fun getTasks(): List<TaskEntity> = entities.value

        override suspend fun getTask(id: String): TaskEntity? =
            entities.value.firstOrNull { it.id == id }

        override suspend fun upsertTask(task: TaskEntity) {
            entities.value = entities.value.filterNot { it.id == task.id } + task
        }

        override suspend fun countTasks(): Int = entities.value.size
    }
}
