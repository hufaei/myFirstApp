package com.example.lifelab.feature.tasks.domain

import java.time.Instant

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueAt: Instant?,
    val tags: List<String>,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class TaskStatus {
    Active,
    Completed,
}

enum class TaskPriority {
    Low,
    Medium,
    High,
}

data class TaskDraft(
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.Medium,
    val dueAt: Instant? = null,
    val tags: List<String> = emptyList(),
)

data class TaskChanges(
    val title: String? = null,
    val description: String? = null,
    val status: TaskStatus? = null,
    val priority: TaskPriority? = null,
    val dueAt: Instant? = null,
    val shouldUpdateDueAt: Boolean = false,
    val tags: List<String>? = null,
)
