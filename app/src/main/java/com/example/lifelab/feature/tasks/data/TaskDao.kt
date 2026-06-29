package com.example.lifelab.feature.tasks.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY created_at_epoch_millis ASC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY created_at_epoch_millis ASC")
    suspend fun getTasks(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTask(id: String): TaskEntity?

    @Upsert
    suspend fun upsertTask(task: TaskEntity)

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun countTasks(): Int
}
