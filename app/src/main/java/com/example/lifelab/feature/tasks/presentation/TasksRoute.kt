package com.example.lifelab.feature.tasks.presentation

import android.net.Uri
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.lifelab.R
import com.example.lifelab.core.media.PhotoAttachmentActions
import com.example.lifelab.core.media.PhotoAttachmentPolicy
import com.example.lifelab.core.media.PhotoFileStorage
import com.example.lifelab.core.media.PhotoRecord
import com.example.lifelab.core.media.PhotoSource
import com.example.lifelab.core.media.copyPhotosToAppStorage
import com.example.lifelab.core.media.toLifeLabFileProviderUri
import com.example.lifelab.feature.tasks.domain.Task
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
            onBackToList = onBackToList,
        )

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
                photos = state.selectedTask?.let { state.photosForTask(it.id) }.orEmpty(),
                onAttachPhotos = onAttachSelectedTaskPhotos,
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
                photos = state.editorPhotos,
                onAttachPhotos = onAttachEditorPhotos,
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
                text = stringResource(R.string.tasks_title),
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
                Text(stringResource(R.string.common_back))
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
                text = message.localizedTaskMessage(),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_dismiss))
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
            Text(stringResource(R.string.tasks_new))
        }
    }

    if (state.isLoading) {
        Text(stringResource(R.string.tasks_loading))
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
                label = { Text(filter.label()) },
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
private fun TaskMetaRow(task: Task) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = {},
            label = { Text(task.priority.label()) },
        )
        AssistChip(
            onClick = {},
            label = { Text(task.dueLabelOrNull() ?: stringResource(R.string.tasks_no_due_date)) },
        )
    }
}

@Composable
private fun TaskEmptyState(filter: TaskFilter) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(filter.emptyTitleRes()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.tasks_empty_body),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun TaskDetailContent(
    task: Task?,
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
    onStartEdit: () -> Unit,
    onComplete: () -> Unit,
    onRestore: () -> Unit,
) {
    if (task == null) {
        Text(stringResource(R.string.tasks_select_detail))
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = task.description.ifBlank { stringResource(R.string.tasks_no_description) },
            style = MaterialTheme.typography.bodyLarge,
        )
        TaskMetaRow(task = task)
        if (task.tags.isNotEmpty()) {
            Text(
                text = stringResource(R.string.tasks_tags_value, task.tags.joinToString(", ")),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TaskPhotoSection(
            ownerId = task.id,
            photos = photos,
            onAttachPhotos = onAttachPhotos,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onStartEdit) {
                Text(stringResource(R.string.tasks_edit))
            }
            if (task.status == TaskStatus.Completed) {
                OutlinedButton(onClick = onRestore) {
                    Text(stringResource(R.string.tasks_restore))
                }
            } else {
                OutlinedButton(onClick = onComplete) {
                    Text(stringResource(R.string.tasks_complete))
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
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
    onSaveEditor: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        TaskPhotoSection(
            ownerId = editorState.editingTaskId ?: "draft-task-editor",
            photos = photos,
            onAttachPhotos = onAttachPhotos,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Button(
            onClick = onSaveEditor,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.tasks_save))
        }
    }
}

@Composable
private fun TaskPhotoSection(
    ownerId: String,
    photos: List<PhotoRecord>,
    onAttachPhotos: (List<String>, PhotoSource) -> Unit,
) {
    val owner = remember(ownerId) { taskPhotoOwner(ownerId) }
    val context = LocalContext.current
    val remainingSlots = PhotoAttachmentPolicy().remainingSlots(owner, photos)
    val cameraCaptureUri = rememberCameraCaptureUri(
        ownerId = ownerId,
        remainingSlots = remainingSlots,
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = stringResource(R.string.photo_section_title),
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = stringResource(R.string.photo_remaining_count, remainingSlots),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            PhotoAttachmentActions(
                remainingSlots = remainingSlots,
                cameraCaptureUri = cameraCaptureUri,
                pickerLabel = stringResource(R.string.photo_add_from_album),
                cameraLabel = stringResource(R.string.photo_take_photo),
                onPickerPhotosSelected = { uris ->
                    val storedUris = context.copyPhotosToAppStorage(
                        owner = owner,
                        uris = uris.take(remainingSlots),
                        startSequence = photos.size,
                        createdAtMillis = System.currentTimeMillis(),
                    )
                    onAttachPhotos(storedUris.map { it.toString() }, PhotoSource.Picker)
                },
                onCameraPhotoCaptured = { uri ->
                    onAttachPhotos(listOf(uri.toString()), PhotoSource.Camera)
                },
            )
        }

        if (photos.isEmpty()) {
            Text(
                text = stringResource(R.string.photo_empty_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                photos.take(PhotoAttachmentPolicy.MAX_PHOTOS_PER_OWNER).forEach { photo ->
                    AsyncImage(
                        model = photo.localUri,
                        contentDescription = stringResource(R.string.photo_preview_description),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberCameraCaptureUri(
    ownerId: String,
    remainingSlots: Int,
): Uri? {
    val context = LocalContext.current
    return remember(ownerId, remainingSlots) {
        if (remainingSlots <= 0) {
            null
        } else {
            PhotoFileStorage(
                filesDir = context.filesDir,
                cacheDir = context.cacheDir,
            ).createCameraCaptureFile(
                owner = taskPhotoOwner(ownerId),
                createdAtMillis = System.currentTimeMillis(),
            ).toLifeLabFileProviderUri(context)
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
        ?.format(DateTimeFormatter.ofPattern("MMM d"))

@Composable
private fun String.localizedTaskMessage(): String =
    when (this) {
        "Task created" -> stringResource(R.string.tasks_message_created)
        "Task updated" -> stringResource(R.string.tasks_message_updated)
        "Task completed" -> stringResource(R.string.tasks_message_completed)
        "Task restored" -> stringResource(R.string.tasks_message_restored)
        else -> this
    }
