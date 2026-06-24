package com.example.lifelab.feature.search.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.lifelab.feature.search.data.InMemorySearchRepository

@Composable
fun SearchRoute(contentPadding: PaddingValues) {
    val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory)
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

private object SearchViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(InMemorySearchRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
