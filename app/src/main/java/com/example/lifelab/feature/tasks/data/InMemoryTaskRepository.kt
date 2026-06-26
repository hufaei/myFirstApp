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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository : TaskRepository {
    private val baseTime = Instant.parse("2026-06-24T00:00:00Z")
    private var nextId = 4
    private var nextMutationSecond = 4L
    private val tasks = MutableStateFlow(seedTasks())

    override fun observeTasks(): Flow<List<Task>> = tasks.asStateFlow()

    override suspend fun getTask(id: String): AppResult<Task> {
        return tasks.value.firstOrNull { it.id == id }?.let { task ->
            AppResult.Success(task)
        }
            ?: missingTaskFailure(id)
    }

    override suspend fun createTask(draft: TaskDraft): AppResult<Task> {
        val normalizedTitle = draft.title.trim()
        if (normalizedTitle.isBlank()) {
            return validationFailure()
        }

        val now = nextMutationTime()
        val task = Task(
            id = "task-${nextId++}",
            title = normalizedTitle,
            description = draft.description.trim(),
            status = TaskStatus.Active,
            priority = draft.priority,
            dueAt = draft.dueAt,
            tags = normalizeTags(draft.tags),
            createdAt = now,
            updatedAt = now,
        )
        tasks.update { current -> current + task }
        return AppResult.Success(task)
    }

    override suspend fun updateTask(
        id: String,
        changes: TaskChanges,
    ): AppResult<Task> {
        val current = tasks.value.firstOrNull { it.id == id }
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
            updatedAt = nextMutationTime(),
        )
        replace(updated)
        return AppResult.Success(updated)
    }

    override suspend fun completeTask(id: String): AppResult<Task> {
        return updateTask(id, TaskChanges(status = TaskStatus.Completed))
    }

    override suspend fun restoreTask(id: String): AppResult<Task> {
        return updateTask(id, TaskChanges(status = TaskStatus.Active))
    }

    private fun replace(task: Task) {
        tasks.update { current ->
            current.map { existing ->
                if (existing.id == task.id) task else existing
            }
        }
    }

    private fun nextMutationTime(): Instant {
        val time = baseTime.plusSeconds(nextMutationSecond)
        nextMutationSecond += 1
        return time
    }

    private fun seedTasks(): List<Task> {
        return listOf(
            Task(
                id = "task-1",
                title = "规划每周复盘",
                description = "选出本周最重要的三个 LifeLab 优先事项。",
                status = TaskStatus.Active,
                priority = TaskPriority.High,
                dueAt = baseTime.plusSeconds(86_400),
                tags = listOf("计划", "居家"),
                createdAt = baseTime,
                updatedAt = baseTime,
            ),
            Task(
                id = "task-2",
                title = "预约健康检查",
                description = "确认可预约时间和所需材料。",
                status = TaskStatus.Active,
                priority = TaskPriority.Medium,
                dueAt = baseTime.plusSeconds(259_200),
                tags = listOf("健康"),
                createdAt = baseTime.plusSeconds(1),
                updatedAt = baseTime.plusSeconds(1),
            ),
            Task(
                id = "task-3",
                title = "归档已完成票据",
                description = "把扫描票据整理到财务文件夹。",
                status = TaskStatus.Completed,
                priority = TaskPriority.Low,
                dueAt = null,
                tags = listOf("财务", "整理"),
                createdAt = baseTime.plusSeconds(2),
                updatedAt = baseTime.plusSeconds(3),
            ),
        )
    }

    private fun normalizeTags(tags: List<String>): List<String> {
        return tags.map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun validationFailure(): AppResult.Failure {
        return AppResult.Failure(AppError.Validation(message = "任务标题不能为空"))
    }

    private fun missingTaskFailure(id: String): AppResult.Failure {
        return AppResult.Failure(AppError.Storage(message = "没有找到任务：$id"))
    }
}
