package com.example.lifelab.feature.discover.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DiscoverRoute(
    contentPadding: PaddingValues,
    viewModel: DiscoverViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DiscoverScreen(
        uiState = uiState,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
    )
}
