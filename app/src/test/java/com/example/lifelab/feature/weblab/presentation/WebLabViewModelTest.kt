package com.example.lifelab.feature.weblab.presentation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class WebLabViewModelTest {

    @Test
    fun startsAtLifeLabLabUrlAndLoadingState() {
        val viewModel = WebLabViewModel()

        val state = viewModel.uiState.value
        assertEquals("https://hufaei.github.io/", state.url)
        assertTrue(state.isLoading)
        assertFalse(state.canGoBack)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun pageCallbacksUpdateLoadingErrorAndBackState() {
        val viewModel = WebLabViewModel()

        viewModel.onEvent(WebLabUiEvent.PageStarted("https://hufaei.github.io/"))
        viewModel.onEvent(WebLabUiEvent.BackStateChanged(canGoBack = true))
        viewModel.onEvent(WebLabUiEvent.PageFinished("https://hufaei.github.io/experiments"))

        val state = viewModel.uiState.value
        assertEquals("https://hufaei.github.io/experiments", state.url)
        assertFalse(state.isLoading)
        assertTrue(state.canGoBack)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun pageErrorExposesRetryableErrorAndRefreshClearsIt() {
        val viewModel = WebLabViewModel()

        viewModel.onEvent(WebLabUiEvent.PageFailed("Network unavailable"))

        val failedState = viewModel.uiState.value
        assertFalse(failedState.isLoading)
        assertEquals("Network unavailable", failedState.errorMessage)
        assertFalse(failedState.refreshRequested)

        viewModel.onEvent(WebLabUiEvent.RefreshRequested)

        val refreshingState = viewModel.uiState.value
        assertTrue(refreshingState.isLoading)
        assertEquals(null, refreshingState.errorMessage)
        assertTrue(refreshingState.refreshRequested)
    }

    @Test
    fun refreshConsumedAndBackHandledAreOneShotStateChanges() {
        val viewModel = WebLabViewModel()
        viewModel.onEvent(WebLabUiEvent.RefreshRequested)

        viewModel.onEvent(WebLabUiEvent.RefreshConsumed)

        assertFalse(viewModel.uiState.value.refreshRequested)

        viewModel.onEvent(WebLabUiEvent.BackStateChanged(canGoBack = true))
        viewModel.onEvent(WebLabUiEvent.WebBackRequested)

        assertTrue(viewModel.uiState.value.webBackRequested)

        viewModel.onEvent(WebLabUiEvent.WebBackConsumed(canGoBack = false))

        assertFalse(viewModel.uiState.value.webBackRequested)
        assertFalse(viewModel.uiState.value.canGoBack)
    }
}
