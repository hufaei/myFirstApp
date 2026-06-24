package com.example.lifelab.app.workbench

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifelab.feature.habits.presentation.HabitsPane
import com.example.lifelab.feature.habits.presentation.HabitsViewModel
import com.example.lifelab.feature.tasks.presentation.TaskListViewModel
import com.example.lifelab.feature.tasks.presentation.TasksPane

@Composable
fun WorkbenchRoute(
    contentPadding: PaddingValues,
    taskViewModel: TaskListViewModel = viewModel(),
    habitsViewModel: HabitsViewModel = viewModel(),
) {
    val tasksState by taskViewModel.uiState.collectAsStateWithLifecycle()
    val habitsState by habitsViewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(WorkbenchTab.Tasks) }

    WorkbenchScreen(
        selectedTab = selectedTab,
        summary = buildWorkbenchSummary(
            tasksState = tasksState,
            habitsState = habitsState,
        ),
        contentPadding = contentPadding,
        onSelectTab = { selectedTab = it },
        tasksContent = {
            TasksPane(
                state = tasksState,
                showHeader = false,
                onSelectFilter = taskViewModel::selectFilter,
                onOpenDetail = taskViewModel::openDetail,
                onStartCreate = taskViewModel::startCreate,
                onStartEdit = taskViewModel::startEdit,
                onUpdateTitle = taskViewModel::updateTitle,
                onUpdateDescription = taskViewModel::updateDescription,
                onUpdatePriority = taskViewModel::updatePriority,
                onUpdateTags = taskViewModel::updateTags,
                onUpdateDueLabel = taskViewModel::updateDueLabel,
                onSaveEditor = taskViewModel::saveEditor,
                onComplete = taskViewModel::completeSelectedTask,
                onRestore = taskViewModel::restoreSelectedTask,
                onBackToList = taskViewModel::backToList,
                onClearMessage = taskViewModel::clearMessage,
            )
        },
        habitsContent = {
            HabitsPane(
                state = habitsState,
                showHeader = false,
                onCheckIn = habitsViewModel::checkIn,
                onReminderEnabledChange = habitsViewModel::setReminderEnabled,
                onReminderTimeChange = habitsViewModel::updateReminderTime,
                onClearMessage = habitsViewModel::clearMessage,
            )
        },
    )
}
