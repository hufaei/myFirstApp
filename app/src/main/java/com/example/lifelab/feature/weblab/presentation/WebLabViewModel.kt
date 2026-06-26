package com.example.lifelab.feature.weblab.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WebLabViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WebLabUiState())
    val uiState: StateFlow<WebLabUiState> = _uiState.asStateFlow()

    fun onEvent(event: WebLabUiEvent) {
        when (event) {
            is WebLabUiEvent.PageStarted -> _uiState.update {
                it.copy(
                    url = event.url,
                    isLoading = true,
                    errorMessage = null,
                )
            }
            is WebLabUiEvent.PageFinished -> _uiState.update {
                it.copy(
                    url = event.url,
                    isLoading = false,
                    errorMessage = null,
                )
            }
            is WebLabUiEvent.PageFailed -> _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = event.message,
                )
            }
            is WebLabUiEvent.BackStateChanged -> _uiState.update {
                it.copy(canGoBack = event.canGoBack)
            }
            WebLabUiEvent.RefreshRequested -> _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null,
                    refreshRequested = true,
                )
            }
            WebLabUiEvent.RefreshConsumed -> _uiState.update {
                it.copy(refreshRequested = false)
            }
            WebLabUiEvent.WebBackRequested -> _uiState.update {
                if (it.canGoBack) {
                    it.copy(webBackRequested = true)
                } else {
                    it
                }
            }
            is WebLabUiEvent.WebBackConsumed -> _uiState.update {
                it.copy(
                    canGoBack = event.canGoBack,
                    webBackRequested = false,
                )
            }
        }
    }
}
