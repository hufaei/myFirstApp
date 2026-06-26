package com.example.lifelab.feature.tasks.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.Instant

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val status: String,
    val priority: String,
    @ColumnInfo(name = "due_at_epoch_millis") val dueAtEpochMillis: Long?,
    val tags: String,
    @ColumnInfo(name = "created_at_epoch_millis") val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_millis") val updatedAtEpochMillis: Long,
)

fun TaskEntity.toDomain(): Task =
    Task(
        id = id,
        title = title,
        description = description,
        status = TaskStatus.valueOf(status),
        priority = TaskPriority.valueOf(priority),
        dueAt = dueAtEpochMillis?.let(Instant::ofEpochMilli),
        tags = decodeTags(tags),
        createdAt = Instant.ofEpochMilli(createdAtEpochMillis),
        updatedAt = Instant.ofEpochMilli(updatedAtEpochMillis),
    )

fun Task.toEntity(): TaskEntity =
    TaskEntity(
        id = id,
        title = title,
        description = description,
        status = status.name,
        priority = priority.name,
        dueAtEpochMillis = dueAt?.toEpochMilli(),
        tags = encodeTags(tags),
        createdAtEpochMillis = createdAt.toEpochMilli(),
        updatedAtEpochMillis = updatedAt.toEpochMilli(),
    )

private const val TAG_SEPARATOR = "\u001F"

private fun encodeTags(tags: List<String>): String =
    tags.joinToString(TAG_SEPARATOR)

private fun decodeTags(encoded: String): List<String> =
    if (encoded.isBlank()) {
        emptyList()
    } else {
        encoded.split(TAG_SEPARATOR)
    }
