package com.example.lifelab.feature.tasks.data

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskChanges
import com.example.lifelab.feature.tasks.domain.TaskDraft
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskRepository
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomTaskRepository(
    private val taskDao: TaskDao,
    private val now: () -> Instant = Instant::now,
    private val idGenerator: () -> String = { UUID.randomUUID().toString() },
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeTasks()
            .onStart { seedIfEmpty() }
            .map { tasks -> tasks.map(TaskEntity::toDomain) }

    override suspend fun getTask(id: String): AppResult<Task> =
        taskDao.getTask(id)?.toDomain()?.let { task -> AppResult.Success(task) }
            ?: missingTaskFailure(id)

    override suspend fun createTask(draft: TaskDraft): AppResult<Task> {
        val normalizedTitle = draft.title.trim()
        if (normalizedTitle.isBlank()) {
            return validationFailure()
        }

        val createdAt = now()
        val task = Task(
            id = idGenerator(),
            title = normalizedTitle,
            description = draft.description.trim(),
            status = TaskStatus.Active,
            priority = draft.priority,
            dueAt = draft.dueAt,
            tags = normalizeTags(draft.tags),
            createdAt = createdAt,
            updatedAt = createdAt,
        )
        taskDao.upsertTask(task.toEntity())
        return AppResult.Success(task)
    }

    override suspend fun updateTask(
        id: String,
        changes: TaskChanges,
    ): AppResult<Task> {
        val current = taskDao.getTask(id)?.toDomain()
            ?: return missingTaskFailure(id)
        val nextTitle = changes.title?.trim() ?: current.title
        if (nextTitle.isBlank()) {
            return validationFailure()
        }

        val updated = current.copy(
            title = nextTitle,
            description = changes.description?.trim() ?: current.description,
            status = changes.status ?: current.status,
            priority = changes.priority ?: current.priority,
            dueAt = if (changes.shouldUpdateDueAt) changes.dueAt else current.dueAt,
            tags = changes.tags?.let(::normalizeTags) ?: current.tags,
            updatedAt = now(),
        )
        taskDao.upsertTask(updated.toEntity())
        return AppResult.Success(updated)
    }

    override suspend fun completeTask(id: String): AppResult<Task> =
        updateTask(id, TaskChanges(status = TaskStatus.Completed))

    override suspend fun restoreTask(id: String): AppResult<Task> =
        updateTask(id, TaskChanges(status = TaskStatus.Active))

    suspend fun seedIfEmpty() {
        if (taskDao.countTasks() == 0) {
            seedTasks().forEach { taskDao.upsertTask(it.toEntity()) }
        }
    }

    private fun normalizeTags(tags: List<String>): List<String> =
        tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

    private fun validationFailure(): AppResult<Nothing> =
        AppResult.Failure(AppError.Validation(message = "Task title is required"))

    private fun missingTaskFailure(id: String): AppResult<Nothing> =
        AppResult.Failure(AppError.Storage(message = "Task not found: $id"))

    private companion object {
        private val baseTime: Instant = Instant.parse("2026-06-24T00:00:00Z")

        fun seedTasks(): List<Task> =
            listOf(
                Task(
                    id = "task-1",
                    title = "Plan the weekly reset",
                    description = "Pick the top three LifeLab priorities for the week.",
                    status = TaskStatus.Active,
                    priority = TaskPriority.High,
                    dueAt = baseTime.plusSeconds(86_400),
                    tags = listOf("planning", "home"),
                    createdAt = baseTime,
                    updatedAt = baseTime,
                ),
                Task(
                    id = "task-2",
                    title = "Book health checkup",
                    description = "Confirm the appointment window and required documents.",
                    status = TaskStatus.Active,
                    priority = TaskPriority.Medium,
                    dueAt = baseTime.plusSeconds(259_200),
                    tags = listOf("health"),
                    createdAt = baseTime.plusSeconds(1),
                    updatedAt = baseTime.plusSeconds(1),
                ),
                Task(
                    id = "task-3",
                    title = "Archive completed receipts",
                    description = "Move scanned receipts into the finance folder.",
                    status = TaskStatus.Completed,
                    priority = TaskPriority.Low,
                    dueAt = null,
                    tags = listOf("finance", "admin"),
                    createdAt = baseTime.plusSeconds(2),
                    updatedAt = baseTime.plusSeconds(3),
                ),
            )
    }
}
