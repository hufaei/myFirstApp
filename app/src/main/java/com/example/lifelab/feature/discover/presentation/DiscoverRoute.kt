package com.example.lifelab.feature.discover.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DiscoverRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit = {},
    viewModel: DiscoverViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DiscoverScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}
