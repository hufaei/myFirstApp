package com.example.lifelab.feature.discover.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lifelab.R
import com.example.lifelab.core.ui.components.LifeLabPrimaryActionRow
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.core.ui.components.LifeLabStateCard
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
    val selectedDetail = uiState.selectedContentDetail
    val headerBack = if (selectedDetail != null) {
        { onEvent(DiscoverUiEvent.DetailDismissed) }
    } else {
        onBack
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        LifeLabScreenHeader(
            title = stringResource(R.string.discover_title),
            onBack = headerBack,
        )
        if (selectedDetail == null) {
            CategoryFilters(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    onEvent(DiscoverUiEvent.CategorySelected(category))
                },
            )
            DiscoverContentState(
                listState = uiState.listState,
                onRetry = { onEvent(DiscoverUiEvent.RetrySelected) },
                onContentSelected = { contentId ->
                    onEvent(DiscoverUiEvent.ContentSelected(contentId))
                },
                modifier = Modifier.weight(1f),
            )
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                DiscoverContentDetailCard(
                    detail = selectedDetail,
                    onBackToList = { onEvent(DiscoverUiEvent.DetailDismissed) },
                )
            }
        }
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
    onContentSelected: (String) -> Unit,
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
            onContentSelected = onContentSelected,
            modifier = modifier,
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    LifeLabStateCard(
        title = stringResource(R.string.discover_loading),
        modifier = modifier,
    )
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    LifeLabStateCard(
        title = stringResource(R.string.discover_empty_title),
        body = stringResource(R.string.discover_empty_body),
        modifier = modifier,
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LifeLabStateCard(
        title = stringResource(R.string.discover_error_title),
        body = message,
        actionLabel = stringResource(R.string.common_retry),
        onAction = onRetry,
        modifier = modifier,
    )
}

@Composable
private fun ContentList(
    items: List<DiscoverContent>,
    onContentSelected: (String) -> Unit,
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
            DiscoverContentCard(
                content = content,
                onContentSelected = onContentSelected,
            )
        }
    }
}

@Composable
private fun DiscoverContentCard(
    content: DiscoverContent,
    onContentSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onContentSelected(content.id) },
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = content.typeLabel(),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = content.tag,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = content.detailLabel(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun DiscoverContentDetailCard(
    detail: DiscoverContentDetail,
    onBackToList: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = detail.kind.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = detail.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(
                    R.string.discover_detail_category,
                    detail.category.label(),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (detail.metadata.isNotBlank()) {
                Text(
                    text = detail.metadata,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Text(
                text = detail.tag,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            LifeLabPrimaryActionRow(
                primaryLabel = stringResource(R.string.discover_detail_back_to_list),
                onPrimaryClick = onBackToList,
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

@Composable
private fun DiscoverContentKind.label(): String =
    when (this) {
        DiscoverContentKind.Article -> stringResource(R.string.discover_type_article)
        DiscoverContentKind.Course -> stringResource(R.string.discover_type_course)
        DiscoverContentKind.Product -> stringResource(R.string.discover_type_product)
        DiscoverContentKind.Membership -> stringResource(R.string.discover_type_membership)
    }
