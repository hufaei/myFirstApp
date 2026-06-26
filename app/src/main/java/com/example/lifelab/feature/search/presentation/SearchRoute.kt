package com.example.lifelab.feature.search.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SearchRoute(
    contentPadding: PaddingValues,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SearchScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onQueryChanged = viewModel::onQueryChanged,
        onSubmitQuery = viewModel::submitCurrentQuery,
        onFilterSelected = viewModel::selectFilter,
        onHotKeywordClick = viewModel::selectHotKeyword,
        onHistoryClick = viewModel::selectHistoryQuery,
        onClearHistoryClick = viewModel::clearHistory,
        onRetryClick = viewModel::retry,
    )
}
