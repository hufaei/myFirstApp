package com.example.lifelab.feature.search.presentation

import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchResultItem

data class SearchUiState(
    val query: String = "",
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val history: List<String> = emptyList(),
    val hotKeywords: List<String> = emptyList(),
    val lastSubmittedQuery: String? = null,
    val resultContent: SearchResultContent = SearchResultContent.Idle,
)

sealed interface SearchResultContent {
    data object Idle : SearchResultContent
    data object Loading : SearchResultContent
    data class Content(val items: List<SearchResultItem>) : SearchResultContent
    data object Empty : SearchResultContent
    data class Error(val message: String) : SearchResultContent
}
