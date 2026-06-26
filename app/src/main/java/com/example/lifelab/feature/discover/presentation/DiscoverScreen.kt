package com.example.lifelab.feature.discover.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.lifelab.R
import com.example.lifelab.feature.discover.domain.DiscoverCategory
import com.example.lifelab.feature.discover.domain.DiscoverContent

@Composable
fun DiscoverScreen(
    uiState: DiscoverUiState,
    contentPadding: PaddingValues,
    onEvent: (DiscoverUiEvent) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.common_back),
                )
            }
            Text(
                text = stringResource(R.string.discover_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
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
                label = { Text(category.label()) },
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
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.discover_loading),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.discover_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = stringResource(R.string.discover_empty_body),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.discover_error_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(12.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.common_retry))
        }
    }
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
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = content.typeLabel(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = content.tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = content.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = content.summary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = content.detailLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private val discoverCategoryOptions = listOf(
    DiscoverCategory.All,
    DiscoverCategory.Articles,
    DiscoverCategory.Courses,
    DiscoverCategory.Offers,
    DiscoverCategory.Membership,
)

@Composable
private fun DiscoverCategory.label(): String =
    when (this) {
        DiscoverCategory.All -> stringResource(R.string.discover_category_all)
        DiscoverCategory.Articles -> stringResource(R.string.discover_category_articles)
        DiscoverCategory.Courses -> stringResource(R.string.discover_category_courses)
        DiscoverCategory.Offers -> stringResource(R.string.discover_category_offers)
        DiscoverCategory.Membership -> stringResource(R.string.discover_category_membership)
    }

@Composable
private fun DiscoverContent.typeLabel(): String =
    when (this) {
        is DiscoverContent.Article -> stringResource(R.string.discover_type_article)
        is DiscoverContent.Course -> stringResource(R.string.discover_type_course)
        is DiscoverContent.Offer.Product -> stringResource(R.string.discover_type_product)
        is DiscoverContent.Offer.Membership -> stringResource(R.string.discover_type_membership)
    }

@Composable
private fun DiscoverContent.detailLabel(): String =
    when (this) {
        is DiscoverContent.Article -> stringResource(R.string.discover_detail_by_author, author)
        is DiscoverContent.Course -> stringResource(R.string.discover_detail_course, instructor, duration)
        is DiscoverContent.Offer.Product -> priceLabel
        is DiscoverContent.Offer.Membership -> priceLabel
    }
