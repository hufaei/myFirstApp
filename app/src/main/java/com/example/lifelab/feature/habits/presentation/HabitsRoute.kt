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
        onCheckIn = viewModel::checkIn,
        onReminderEnabledChange = viewModel::setReminderEnabled,
        onReminderTimeChange = viewModel::updateReminderTime,
        onReminderPriorityChange = viewModel::updateReminderPriority,
        onAttachPhotos = viewModel::attachHabitPhotos,
        onClearMessage = viewModel::clearMessage,
    )
}
