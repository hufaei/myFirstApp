package com.example.lifelab.feature.habits.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HabitsRoute(contentPadding: PaddingValues) {
    val viewModel: HabitsViewModel = viewModel()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    HabitsScreen(
        state = uiState.value,
        contentPadding = contentPadding,
        onCheckIn = viewModel::checkIn,
        onReminderEnabledChange = viewModel::setReminderEnabled,
        onReminderTimeChange = viewModel::updateReminderTime,
        onClearMessage = viewModel::clearMessage,
    )
}
