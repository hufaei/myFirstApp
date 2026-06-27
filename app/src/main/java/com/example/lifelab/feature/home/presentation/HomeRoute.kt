package com.example.lifelab.feature.home.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
    onOpenRoute: (String) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onCreateTask: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    onOpenDiscover: () -> Unit = {},
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onOpenRoute = onOpenRoute,
        onOpenSearch = onOpenSearch,
        onOpenNotifications = onOpenNotifications,
        onCreateTask = onCreateTask,
        onOpenTasks = onOpenTasks,
        onOpenHabits = onOpenHabits,
        onOpenDiscover = onOpenDiscover,
    )
}
