package com.example.lifelab.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.home.data.SeedHomeFeedRepository
import com.example.lifelab.feature.home.domain.BuildHomeFeedUseCase
import com.example.lifelab.feature.home.domain.HomeFeedContent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val buildHomeFeed: BuildHomeFeedUseCase = BuildHomeFeedUseCase(
        repository = SeedHomeFeedRepository(),
    ),
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHomeContent(mode = LoadMode.Initial)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh -> loadHomeContent(mode = LoadMode.Refresh)
            HomeUiEvent.Retry -> loadHomeContent(mode = LoadMode.Retry)
        }
    }

    private fun loadHomeContent(mode: LoadMode) {
        _uiState.update { state ->
            when (mode) {
                LoadMode.Initial -> state.copy(isLoading = true, errorMessage = null)
                LoadMode.Refresh -> state.copy(isRefreshing = true, errorMessage = null)
                LoadMode.Retry -> state.copy(
                    isLoading = state.content == null,
                    isRefreshing = state.content != null,
                    errorMessage = null,
                )
            }
        }

        viewModelScope.launch {
            when (val result = buildHomeFeed()) {
                is AppResult.Success -> showContent(result.value)
                is AppResult.Failure -> showError(result.error.message)
            }
        }
    }

    private fun showContent(content: HomeFeedContent) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                content = content,
                errorMessage = null,
            )
        }
    }

    private fun showError(message: String) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isRefreshing = false,
                errorMessage = message,
            )
        }
    }

    private enum class LoadMode {
        Initial,
        Refresh,
        Retry,
    }
}
