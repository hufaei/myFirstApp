package com.example.lifelab.feature.tasks.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifelab.core.ui.component.StatePanel
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TasksRoute(
    contentPadding: PaddingValues,
    viewModel: TaskListViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksPane(
        state = state,
        contentPadding = contentPadding,
        showHeader = true,
        onSelectFilter = viewModel::selectFilter,
        onOpenDetail = viewModel::openDetail,
        onStartCreate = viewModel::startCreate,
        onStartEdit = viewModel::startEdit,
        onUpdateTitle = viewModel::updateTitle,
        onUpdateDescription = viewModel::updateDescription,
        onUpdatePriority = viewModel::updatePriority,
        onUpdateTags = viewModel::updateTags,
        onUpdateDueLabel = viewModel::updateDueLabel,
        onSaveEditor = viewModel::saveEditor,
        onComplete = viewModel::completeSelectedTask,
        onRestore = viewModel::restoreSelectedTask,
        onBackToList = viewModel::backToList,
        onClearMessage = viewModel::clearMessage,
    )
}

@Composable
fun TasksPane(
    state: TasksUiState,
    onSelectFilter: (TaskFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    onStartCreate: () -> Unit,
    onStartEdit: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdatePriority: (TaskPriority) -> Unit,
    onUpdateTags: (String) -> Unit,
    onUpdateDueLabel: (String) -> Unit,
    onSaveEditor: () -> Unit,
    onComplete: () -> Unit,
    onRestore: () -> Unit,
    onBackToList: () -> Unit,
    onClearMessage: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    showHeader: Boolean = true,
) {
    val panePadding = if (showHeader) 20.dp else 0.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(panePadding),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (showHeader) {
            TasksHeader(
                mode = state.mode,
                onBackToList = onBackToList,
            )
        }

        state.message?.let { message ->
            MessageBanner(
                message = message,
                onDismiss = onClearMessage,
            )
        }

        when (state.mode) {
            TaskScreenMode.List -> TaskListContent(
                state = state,
                onSelectFilter = onSelectFilter,
                onOpenDetail = onOpenDetail,
                onStartCreate = onStartCreate,
            )

            TaskScreenMode.Detail -> TaskDetailContent(
                task = state.selectedTask,
                onStartEdit = onStartEdit,
                onComplete = onComplete,
                onRestore = onRestore,
            )

            TaskScreenMode.Editor -> TaskEditorContent(
                editorState = state.editorState,
                onUpdateTitle = onUpdateTitle,
                onUpdateDescription = onUpdateDescription,
                onUpdatePriority = onUpdatePriority,
                onUpdateTags = onUpdateTags,
                onUpdateDueLabel = onUpdateDueLabel,
                onSaveEditor = onSaveEditor,
            )
        }
    }
}

@Composable
private fun TasksHeader(
    mode: TaskScreenMode,
    onBackToList: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column {
            Text(
                text = "Tasks",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = mode.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        if (mode != TaskScreenMode.List) {
            TextButton(onClick = onBackToList) {
                Text("Back")
            }
        }
    }
}

@Composable
private fun MessageBanner(
    message: String,
    onDismiss: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onDismiss) {
                Text("Dismiss")
            }
        }
    }
}

@Composable
private fun TaskListContent(
    state: TasksUiState,
    onSelectFilter: (TaskFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    onStartCreate: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        TaskFilterRow(
            selectedFilter = state.selectedFilter,
            onSelectFilter = onSelectFilter,
        )
        Button(onClick = onStartCreate) {
            Text("New task")
        }
    }

    if (state.isLoading) {
        StatePanel(
            title = "Loading tasks",
            body = "Preparing your current work list.",
            isLoading = true,
        )
        return
    }

    if (state.filteredTasks.isEmpty()) {
        TaskEmptyState(filter = state.selectedFilter)
        return
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(
            items = state.filteredTasks,
            key = { it.id },
        ) { task ->
            TaskRow(
                task = task,
                onClick = { onOpenDetail(task.id) },
            )
        }
    }
}

@Composable
private fun TaskFilterRow(
    selectedFilter: TaskFilter,
    onSelectFilter: (TaskFilter) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskFilter.entries.forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onSelectFilter(filter) },
                label = { Text(filter.label) },
            )
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = task.status.label,
                    style = MaterialTheme.typography.labelMedium,
                    color = task.status.color(),
                )
            }
            Text(
                text = task.description.ifBlank { "No description" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TaskMetaRow(task = task)
        }
    }
}

@Composable
private fun TaskMetaRow(task: Task) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = {},
            label = { Text(task.priority.label) },
        )
        AssistChip(
            onClick = {},
            label = { Text(task.dueLabel()) },
        )
    }
}

@Composable
private fun TaskEmptyState(filter: TaskFilter) {
    StatePanel(
        title = "No ${filter.label.lowercase()} tasks yet",
        body = "Create a task or switch filters to review other work.",
    )
}

@Composable
private fun TaskDetailContent(
    task: Task?,
    onStartEdit: () -> Unit,
    onComplete: () -> Unit,
    onRestore: () -> Unit,
) {
    if (task == null) {
        Text("Select a task to view its details.")
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = task.description.ifBlank { "No description" },
            style = MaterialTheme.typography.bodyLarge,
        )
        TaskMetaRow(task = task)
        if (task.tags.isNotEmpty()) {
            Text(
                text = "Tags: ${task.tags.joinToString(", ")}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onStartEdit) {
                Text("Edit")
            }
            if (task.status == TaskStatus.Completed) {
                OutlinedButton(onClick = onRestore) {
                    Text("Restore")
                }
            } else {
                OutlinedButton(onClick = onComplete) {
                    Text("Complete")
                }
            }
        }
    }
}

@Composable
private fun TaskEditorContent(
    editorState: TaskEditorState,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdatePriority: (TaskPriority) -> Unit,
    onUpdateTags: (String) -> Unit,
    onUpdateDueLabel: (String) -> Unit,
    onSaveEditor: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = editorState.title,
            onValueChange = onUpdateTitle,
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = editorState.description,
            onValueChange = onUpdateDescription,
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        Text(
            text = "Priority",
            style = MaterialTheme.typography.labelLarge,
        )
        PrioritySelector(
            selectedPriority = editorState.priority,
            onUpdatePriority = onUpdatePriority,
        )
        OutlinedTextField(
            value = editorState.tags,
            onValueChange = onUpdateTags,
            label = { Text("Tags") },
            placeholder = { Text("planning, health") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = editorState.dueLabel,
            onValueChange = onUpdateDueLabel,
            label = { Text("Due date") },
            placeholder = { Text("2026-06-30") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onSaveEditor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save task")
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun PrioritySelector(
    selectedPriority: TaskPriority,
    onUpdatePriority: (TaskPriority) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TaskPriority.entries.forEachIndexed { index, priority ->
            SegmentedButton(
                selected = selectedPriority == priority,
                onClick = { onUpdatePriority(priority) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = TaskPriority.entries.size,
                ),
            ) {
                Text(priority.label)
            }
        }
    }
}

private val TaskFilter.label: String
    get() = when (this) {
        TaskFilter.All -> "All"
        TaskFilter.Active -> "Active"
        TaskFilter.Completed -> "Completed"
    }

private val TaskScreenMode.title: String
    get() = when (this) {
        TaskScreenMode.List -> "Plan and filter current work"
        TaskScreenMode.Detail -> "Review task details"
        TaskScreenMode.Editor -> "Create or edit task"
    }

private val TaskPriority.label: String
    get() = when (this) {
        TaskPriority.Low -> "Low"
        TaskPriority.Medium -> "Medium"
        TaskPriority.High -> "High"
    }

private val TaskStatus.label: String
    get() = when (this) {
        TaskStatus.Active -> "Active"
        TaskStatus.Completed -> "Completed"
    }

@Composable
private fun TaskStatus.color() = when (this) {
    TaskStatus.Active -> MaterialTheme.colorScheme.primary
    TaskStatus.Completed -> MaterialTheme.colorScheme.tertiary
}

private fun Task.dueLabel(): String {
    return dueAt?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofPattern("MMM d"))
        ?: "No due date"
}
