package com.example.lifelab.feature.discover.presentation

import com.example.lifelab.feature.discover.domain.DiscoverCategory
import com.example.lifelab.feature.discover.domain.DiscoverContent

data class DiscoverUiState(
    val selectedCategory: DiscoverCategory = DiscoverCategory.All,
    val listState: DiscoverListState = DiscoverListState.Loading,
    val selectedContentDetail: DiscoverContentDetail? = null,
)

data class DiscoverContentDetail(
    val id: String,
    val title: String,
    val summary: String,
    val tag: String,
    val metadata: String,
    val kind: DiscoverContentKind,
    val category: DiscoverCategory,
)

enum class DiscoverContentKind {
    Article,
    Course,
    Product,
    Membership,
}

sealed interface DiscoverUiEvent {
    data class CategorySelected(val category: DiscoverCategory) : DiscoverUiEvent
    data class ContentSelected(val contentId: String) : DiscoverUiEvent
    data object DetailDismissed : DiscoverUiEvent
    data object RetrySelected : DiscoverUiEvent
}

sealed interface DiscoverListState {
    data object Loading : DiscoverListState
    data class Content(val items: List<DiscoverContent>) : DiscoverListState
    data object Empty : DiscoverListState
    data class Error(val message: String) : DiscoverListState
}
