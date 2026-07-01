package com.example.lifelab.feature.search.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchRepository
import com.example.lifelab.feature.search.domain.SearchResultItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: SearchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    private var latestSearchRequestId = 0L

    init {
        viewModelScope.launch {
            repository.history.collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
        viewModelScope.launch {
            repository.hotKeywords.collect { hotKeywords ->
                _uiState.update { it.copy(hotKeywords = hotKeywords) }
            }
        }
    }

    fun onQueryChanged(query: String) {
        _uiState.update {
            it.copy(query = query)
        }
    }

    fun submitCurrentQuery() {
        submitQuery(uiState.value.query)
    }

    fun submitQuery(query: String) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) {
            return
        }

        _uiState.update {
            it.copy(
                query = trimmedQuery,
                lastSubmittedQuery = trimmedQuery,
            )
        }
        search(trimmedQuery, uiState.value.selectedFilter, shouldRecordHistory = true)
    }

    fun selectFilter(filter: SearchFilter) {
        val lastSubmittedQuery = uiState.value.lastSubmittedQuery
        _uiState.update {
            it.copy(selectedFilter = filter)
        }

        if (!lastSubmittedQuery.isNullOrBlank()) {
            search(lastSubmittedQuery, filter, shouldRecordHistory = false)
        }
    }

    fun selectHotKeyword(keyword: String) {
        submitQuery(keyword)
    }

    fun selectHistoryQuery(query: String) {
        submitQuery(query)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun retry() {
        val lastSubmittedQuery = uiState.value.lastSubmittedQuery ?: return
        search(lastSubmittedQuery, uiState.value.selectedFilter, shouldRecordHistory = false)
    }

    private fun search(
        query: String,
        filter: SearchFilter,
        shouldRecordHistory: Boolean,
    ) {
        val searchRequestId = ++latestSearchRequestId
        viewModelScope.launch {
            if (shouldRecordHistory) {
                repository.recordHistory(query)
            }

            _uiState.update {
                it.copy(
                    resultContent = SearchResultContent.Loading,
                )
            }

            when (val result = repository.search(query, filter)) {
                is AppResult.Success -> {
                    if (searchRequestId != latestSearchRequestId) {
                        return@launch
                    }
                    _uiState.update {
                        it.copy(resultContent = result.value.toResultContent())
                    }
                }

                is AppResult.Failure -> {
                    if (searchRequestId != latestSearchRequestId) {
                        return@launch
                    }
                    _uiState.update {
                        it.copy(resultContent = SearchResultContent.Error(result.error.message))
                    }
                }
            }
        }
    }

    private fun List<SearchResultItem>.toResultContent(): SearchResultContent {
        return if (isEmpty()) {
            SearchResultContent.Empty
        } else {
            SearchResultContent.Content(this)
        }
    }
}
