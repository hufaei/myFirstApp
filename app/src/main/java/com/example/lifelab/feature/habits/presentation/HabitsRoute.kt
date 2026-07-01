package com.example.lifelab.feature.habits.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun HabitsRoute(
    contentPadding: PaddingValues,
    viewModel: HabitsViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    HabitsScreen(
        state = uiState.value,
        contentPadding = contentPadding,
        onStartCreate = viewModel::startCreateHabit,
        onStartEdit = viewModel::startEditHabit,
        onUpdateEditorName = viewModel::updateEditorName,
        onEditorReminderEnabledChange = viewModel::setEditorReminderEnabled,
        onIncreaseEditorReminderTime = viewModel::increaseEditorReminderTime,
        onEditorReminderPriorityChange = viewModel::updateEditorReminderPriority,
        onSaveEditor = viewModel::saveEditor,
        onDismissEditor = viewModel::dismissEditor,
        onCheckIn = viewModel::checkIn,
        onReminderEnabledChange = viewModel::setReminderEnabled,
        onReminderTimeChange = viewModel::updateReminderTime,
        onReminderPriorityChange = viewModel::updateReminderPriority,
        onAttachPhotos = viewModel::attachHabitPhotos,
        onClearMessage = viewModel::clearMessage,
    )
}
