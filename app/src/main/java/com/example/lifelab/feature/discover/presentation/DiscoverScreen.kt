package com.example.lifelab.feature.discover.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifelab.core.ui.component.ActionCard
import com.example.lifelab.core.ui.component.SectionHeader
import com.example.lifelab.core.ui.component.StatePanel
import com.example.lifelab.feature.discover.domain.DiscoverCategory
import com.example.lifelab.feature.discover.domain.DiscoverContent

@Composable
fun DiscoverScreen(
    uiState: DiscoverUiState,
    contentPadding: PaddingValues,
    onEvent: (DiscoverUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SectionHeader(
            title = "Discover",
            subtitle = "Editorial picks for your next experiment.",
        )
        CategoryFilters(
            selectedCategory = uiState.selectedCategory,
            onCategorySelected = { category ->
                onEvent(DiscoverUiEvent.CategorySelected(category))
            },
        )
        DiscoverContentState(
            listState = uiState.listState,
            onRetry = { onEvent(DiscoverUiEvent.RetrySelected) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun CategoryFilters(
    selectedCategory: DiscoverCategory,
    onCategorySelected: (DiscoverCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(discoverCategoryOptions) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick = { onCategorySelected(category) },
                label = { Text(category.label) },
            )
        }
    }
}

@Composable
private fun DiscoverContentState(
    listState: DiscoverListState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (listState) {
        DiscoverListState.Loading -> LoadingState(modifier)
        DiscoverListState.Empty -> EmptyState(modifier)
        is DiscoverListState.Error -> ErrorState(
            message = listState.message,
            onRetry = onRetry,
            modifier = modifier,
        )
        is DiscoverListState.Content -> ContentList(
            items = listState.items,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    StatePanel(
        modifier = modifier.fillMaxWidth(),
        title = "Loading discovery",
        body = "Preparing a short list of useful ideas.",
        isLoading = true,
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    StatePanel(
        modifier = modifier.fillMaxWidth(),
        title = "No picks in this category",
        body = "Try another filter to keep exploring LifeLab ideas and offers.",
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StatePanel(
        modifier = modifier.fillMaxWidth(),
        title = "Discovery could not load",
        body = message,
        actionLabel = "Retry",
        onAction = onRetry,
    )
}

@Composable
private fun ContentList(
    items: List<DiscoverContent>,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(
            items = items,
            key = { content -> content.id },
        ) { content ->
            DiscoverContentCard(content)
        }
    }
}

@Composable
private fun DiscoverContentCard(
    content: DiscoverContent,
    modifier: Modifier = Modifier,
) {
    ActionCard(
        modifier = modifier,
        title = content.title,
        body = "${content.typeLabel} / ${content.tag}\n${content.summary}\n${content.detailLabel}",
        actionLabel = "Review",
        onAction = {},
    )
}

private val discoverCategoryOptions = listOf(
    DiscoverCategory.All,
    DiscoverCategory.Articles,
    DiscoverCategory.Courses,
    DiscoverCategory.Offers,
    DiscoverCategory.Membership,
)

private val DiscoverCategory.label: String
    get() = when (this) {
        DiscoverCategory.All -> "All"
        DiscoverCategory.Articles -> "Articles"
        DiscoverCategory.Courses -> "Courses"
        DiscoverCategory.Offers -> "Offers"
        DiscoverCategory.Membership -> "Membership"
    }

private val DiscoverContent.typeLabel: String
    get() = when (this) {
        is DiscoverContent.Article -> "Article"
        is DiscoverContent.Course -> "Course"
        is DiscoverContent.Offer.Product -> "Product"
        is DiscoverContent.Offer.Membership -> "Membership"
    }

private val DiscoverContent.detailLabel: String
    get() = when (this) {
        is DiscoverContent.Article -> "By $author"
        is DiscoverContent.Course -> "$instructor - $duration"
        is DiscoverContent.Offer.Product -> priceLabel
        is DiscoverContent.Offer.Membership -> priceLabel
    }
