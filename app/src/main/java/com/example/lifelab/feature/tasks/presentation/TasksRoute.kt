package com.example.lifelab.feature.tasks.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.verticalScroll
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.lifelab.R
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.core.ui.components.LifeLabMessageBanner
import com.example.lifelab.core.ui.components.LifeLabPhotoStrip
import com.example.lifelab.core.ui.components.LifeLabPrimaryActionRow
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.core.ui.components.LifeLabSectionTitle
import com.example.lifelab.core.ui.components.LifeLabStateCard
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun TasksRoute(
    contentPadding: PaddingValues,
    viewModel: TaskListViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksScreen(
        state = state,
        contentPadding = contentPadding,
        onSelectFilter = viewModel::selectFilter,
        onOpenDetail = viewModel::openDetail,
        onStartCreate = viewModel::startCreate,
        onStartEdit = viewModel::startEdit,
        onUpdateTitle = viewModel::updateTitle,
        onUpdateDescription = viewModel::updateDescription,
        onUpdatePriority = viewModel::updatePriority,
        onUpdateTags = viewModel::updateTags,
        onUpdateDueLabel = viewModel::updateDueLabel,
        onAttachEditorPhotos = viewModel::attachEditorPhotos,
        onAttachSelectedTaskPhotos = viewModel::attachSelectedTaskPhotos,
        onSaveEditor = viewModel::saveEditor,
        onComplete = viewModel::completeSelectedTask,
        onRestore = viewModel::restoreSelectedTask,
        onBackToList = viewModel::backToList,
        onClearMessage = viewModel::clearMessage,
    )
}

@Composable
private fun TasksScreen(
    state: TasksUiState,
    contentPadding: PaddingValues,
    onSelectFilter: (TaskFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    onStartCreate: () -> Unit,
    onStartEdit: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdatePriority: (TaskPriority) -> Unit,
    onUpdateTags: (String) -> Unit,
    onUpdateDueLabel: (String) -> Unit,
    onAttachEditorPhotos: (List<String>, PhotoSource) -> Unit,
    onAttachSelectedTaskPhotos: (List<String>, PhotoSource) -> Unit,
    onSaveEditor: () -> Unit,
    onComplete: () -> Unit,
    onRestore: () -> Unit,
    onBackToList: () -> Unit,
    onClearMessage: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TasksHeader(
            mode = state.mode,
            onStartCreate = onStartCreate,
            onBackToList = onBackToList,
        )

        state.message?.let { message ->
            LifeLabMessageBanner(
                message = message,
                onDismiss = onClearMessage,
            )
        }

        when (state.mode) {
            TaskScreenMode.List -> TaskListContent(
                state = state,
                onSelectFilter = onSelectFilter,
                onOpenDetail = onOpenDetail,
                modifier = Modifier.weight(1f),
            )

            TaskScreenMode.Detail -> TaskDetailContent(
                task = state.selectedTask,
                photos = state.selectedTask?.let { state.photosForTask(it.id) }.orEmpty(),
                onAttachPhotos = onAttachSelectedTaskPhotos,
                onStartEdit = onStartEdit,
                onComplete = onComplete,
                onRestore = onRestore,
                modifier = Modifier.weight(1f),
            )

            TaskScreenMode.Editor -> TaskEditorContent(
                editorState = state.editorState,
                onUpdateTitle = onUpdateTitle,
                onUpdateDescription = onUpdateDescription,
                onUpdatePriority = onUpdatePriority,
                onUpdateTags = onUpdateTags,
                onUpdateDueLabel = onUpdateDueLabel,
                photos = state.editorPhotos,
                onAttachPhotos = onAttachEditorPhotos,
                onSaveEditor = onSaveEditor,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TasksHeader(
    mode: TaskScreenMode,
    onStartCreate: () -> Unit,
    onBackToList: () -> Unit,
) {
    LifeLabScreenHeader(
        title = stringResource(R.string.tasks_title),
        subtitle = mode.title,
        onBack = if (mode == TaskScreenMode.List) null else onBackToList,
        actions = {
            if (mode == TaskScreenMode.List) {
                Button(onClick = onStartCreate) {
                    IconSmallAdd()
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.tasks_new))
                }
            }
        },
    )
}

@Composable
private fun IconSmallAdd() {
    androidx.compose.material3.Icon(
        imageVector = Icons.Filled.Add,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun TaskListContent(
    state: TasksUiState,
    onSelectFilter: (TaskFilter) -> Unit,
    onOpenDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        LifeLabSectionTitle(title = stringResource(R.string.tasks_filter_section))
        TaskFilterRow(
            selectedFilter = state.selectedFilter,
            onSelectFilter = onSelectFilter,
        )

        when {
            state.isLoading -> {
                LifeLabStateCard(title = stringResource(R.string.tasks_loading))
            }

            state.filteredTasks.isEmpty() -> {
                TaskEmptyState(filter = state.selectedFilter)
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TaskFilterRow(
    selectedFilter: TaskFilter,
    onSelectFilter: (TaskFilter) -> Unit,
) {
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        TaskFilter.entries.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = selectedFilter == filter,
                onClick = { onSelectFilter(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = TaskFilter.entries.size,
                ),
            ) {
                Text(filter.label())
            }
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
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = task.status.label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = task.status.color(),
                )
            }
            Text(
                text = task.description.ifBlank { stringResource(R.string.tasks_no_description) },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TaskMetaRow(task = task)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TaskMetaRow(
    task: Task,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AssistChip(
            onClick = {},
            label = { Text(task.priority.label()) },
        )
        AssistChip(
            onClick = {},
            label = { Text(task.dueLabelOrNull() ?: stringResource(R.string.tasks_no_due_date)) },
        )
        if (task.tags.isNotEmpty()) {
            AssistChip(
                onClick = {},
                label = { Text(stringResource(R.string.tasks_tags_count, task.tags.size)) },
            )
        }
    }
}

@Composable
private fun TaskEmptyState(filter: TaskFilter) {
    LifeLabStateCard(
        title = stringResource(filter.emptyTitleRes()),
        body = stringResource(R.string.tasks_empty_body),
    )
}

@Composable
private fun TaskDetailContent(
    task: Task?,
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
    onStartEdit: () -> Unit,
    onComplete: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (task == null) {
        LifeLabStateCard(title = stringResource(R.string.tasks_select_detail))
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = task.description.ifBlank { stringResource(R.string.tasks_no_description) },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        TaskMetaRow(task = task)
        if (task.tags.isNotEmpty()) {
            Text(
                text = stringResource(R.string.tasks_tags_value, task.tags.joinToString(", ")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        LifeLabPhotoStrip(
            owner = taskPhotoOwner(task.id),
            photos = photos,
            onAttachPhotos = onAttachPhotos,
        )
        LifeLabPrimaryActionRow(
            primaryLabel = if (task.status == TaskStatus.Completed) {
                stringResource(R.string.tasks_restore)
            } else {
                stringResource(R.string.tasks_complete)
            },
            onPrimaryClick = if (task.status == TaskStatus.Completed) onRestore else onComplete,
            secondaryLabel = stringResource(R.string.tasks_edit),
            onSecondaryClick = onStartEdit,
            primaryIcon = Icons.Filled.CheckCircle,
            secondaryIcon = Icons.Filled.Edit,
        )
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
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
    onSaveEditor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = editorState.title,
            onValueChange = onUpdateTitle,
            label = { Text(stringResource(R.string.tasks_editor_title)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = editorState.description,
            onValueChange = onUpdateDescription,
            label = { Text(stringResource(R.string.tasks_editor_description)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
        )
        Text(
            text = stringResource(R.string.tasks_editor_priority),
            style = MaterialTheme.typography.labelLarge,
        )
        PrioritySelector(
            selectedPriority = editorState.priority,
            onUpdatePriority = onUpdatePriority,
        )
        OutlinedTextField(
            value = editorState.tags,
            onValueChange = onUpdateTags,
            label = { Text(stringResource(R.string.tasks_editor_tags)) },
            placeholder = { Text(stringResource(R.string.tasks_editor_tags_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = editorState.dueLabel,
            onValueChange = onUpdateDueLabel,
            label = { Text(stringResource(R.string.tasks_editor_due_date)) },
            placeholder = { Text(stringResource(R.string.tasks_editor_due_placeholder)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        LifeLabPhotoStrip(
            owner = taskPhotoOwner(editorState.editingTaskId ?: DraftTaskPhotoOwnerId),
            photos = photos,
            onAttachPhotos = onAttachPhotos,
        )
        Button(
            onClick = onSaveEditor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.tasks_save))
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
                Text(priority.label())
            }
        }
    }
}

@Composable
private fun TaskFilter.label(): String =
    stringResource(
        when (this) {
            TaskFilter.All -> R.string.tasks_filter_all
            TaskFilter.Active -> R.string.tasks_filter_active
            TaskFilter.Completed -> R.string.tasks_filter_completed
        },
    )

private fun TaskFilter.emptyTitleRes(): Int =
    when (this) {
        TaskFilter.All -> R.string.tasks_empty_all
        TaskFilter.Active -> R.string.tasks_empty_active
        TaskFilter.Completed -> R.string.tasks_empty_completed
    }

private val TaskScreenMode.title: String
    @Composable get() = stringResource(
        when (this) {
            TaskScreenMode.List -> R.string.tasks_mode_list
            TaskScreenMode.Detail -> R.string.tasks_mode_detail
            TaskScreenMode.Editor -> R.string.tasks_mode_editor
        },
    )

@Composable
private fun TaskPriority.label(): String =
    stringResource(
        when (this) {
            TaskPriority.Low -> R.string.tasks_priority_low
            TaskPriority.Medium -> R.string.tasks_priority_medium
            TaskPriority.High -> R.string.tasks_priority_high
        },
    )

@Composable
private fun TaskStatus.label(): String =
    stringResource(
        when (this) {
            TaskStatus.Active -> R.string.tasks_status_active
            TaskStatus.Completed -> R.string.tasks_status_completed
        },
    )

@Composable
private fun TaskStatus.color() = when (this) {
    TaskStatus.Active -> MaterialTheme.colorScheme.primary
    TaskStatus.Completed -> MaterialTheme.colorScheme.tertiary
}

private fun Task.dueLabelOrNull(): String? =
    dueAt?.atZone(ZoneId.systemDefault())
        ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))

private const val DraftTaskPhotoOwnerId = "draft-task-editor"
