package com.example.lifelab.feature.home.presentation

import com.example.lifelab.feature.home.domain.HomeFeedContent

data class HomeUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val content: HomeFeedContent? = null,
    val errorMessage: String? = null,
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
    data object Retry : HomeUiEvent
}
