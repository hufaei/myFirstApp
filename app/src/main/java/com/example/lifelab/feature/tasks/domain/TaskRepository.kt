package com.example.lifelab.feature.tasks.domain

import com.example.lifelab.core.common.AppResult
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>

    suspend fun getTask(id: String): AppResult<Task>

    suspend fun createTask(draft: TaskDraft): AppResult<Task>

    suspend fun updateTask(id: String, changes: TaskChanges): AppResult<Task>

    suspend fun completeTask(id: String): AppResult<Task>

    suspend fun restoreTask(id: String): AppResult<Task>
}
