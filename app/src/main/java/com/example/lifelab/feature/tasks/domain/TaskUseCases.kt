package com.example.lifelab.feature.tasks.domain

class ObserveTasksUseCase(
    private val repository: TaskRepository,
) {
    operator fun invoke() = repository.observeTasks()
}

class GetTaskDetailUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: String) = repository.getTask(id)
}

class CreateTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(draft: TaskDraft) = repository.createTask(draft)
}

class UpdateTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(
        id: String,
        changes: TaskChanges,
    ) = repository.updateTask(id, changes)
}

class CompleteTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: String) = repository.completeTask(id)
}

class RestoreTaskUseCase(
    private val repository: TaskRepository,
) {
    suspend operator fun invoke(id: String) = repository.restoreTask(id)
}
