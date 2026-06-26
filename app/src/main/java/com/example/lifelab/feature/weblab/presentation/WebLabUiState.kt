package com.example.lifelab.feature.weblab.presentation

data class WebLabUiState(
    val url: String = WebLabDefaults.START_URL,
    val isLoading: Boolean = true,
    val canGoBack: Boolean = false,
    val errorMessage: String? = null,
    val refreshRequested: Boolean = false,
    val webBackRequested: Boolean = false,
)

sealed interface WebLabUiEvent {
    data class PageStarted(val url: String) : WebLabUiEvent
    data class PageFinished(val url: String) : WebLabUiEvent
    data class PageFailed(val message: String) : WebLabUiEvent
    data class BackStateChanged(val canGoBack: Boolean) : WebLabUiEvent
    data object RefreshRequested : WebLabUiEvent
    data object RefreshConsumed : WebLabUiEvent
    data object WebBackRequested : WebLabUiEvent
    data class WebBackConsumed(val canGoBack: Boolean) : WebLabUiEvent
}

object WebLabDefaults {
    const val START_URL = "https://hufaei.github.io/"
    const val ALLOWED_HOST = "hufaei.github.io"
}
