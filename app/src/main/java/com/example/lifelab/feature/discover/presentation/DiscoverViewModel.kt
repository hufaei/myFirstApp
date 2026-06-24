package com.example.lifelab.feature.discover.presentation

import androidx.lifecycle.ViewModel
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.data.InMemoryDiscoverRepository
import com.example.lifelab.feature.discover.domain.DiscoverCategory
import com.example.lifelab.feature.discover.domain.LoadDiscoverContentUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DiscoverViewModel(
    private val loadDiscoverContent: LoadDiscoverContentUseCase = LoadDiscoverContentUseCase(
        InMemoryDiscoverRepository(),
    ),
) : ViewModel() {
    private val _uiState = MutableStateFlow(DiscoverUiState())
    val uiState: StateFlow<DiscoverUiState> = _uiState.asStateFlow()

    init {
        loadContent(DiscoverCategory.All)
    }

    fun onEvent(event: DiscoverUiEvent) {
        when (event) {
            is DiscoverUiEvent.CategorySelected -> onCategorySelected(event.category)
            DiscoverUiEvent.RetrySelected -> retry()
        }
    }

    private fun onCategorySelected(category: DiscoverCategory) {
        loadContent(category)
    }

    private fun retry() {
        loadContent(_uiState.value.selectedCategory)
    }

    private fun loadContent(category: DiscoverCategory) {
        _uiState.update {
            it.copy(
                selectedCategory = category,
                listState = DiscoverListState.Loading,
            )
        }

        val listState = when (val result = loadDiscoverContent(category)) {
            is AppResult.Failure -> DiscoverListState.Error(result.error.message)
            is AppResult.Success -> {
                if (result.value.isEmpty()) {
                    DiscoverListState.Empty
                } else {
                    DiscoverListState.Content(result.value)
                }
            }
        }

        _uiState.update { it.copy(listState = listState) }
    }
}
