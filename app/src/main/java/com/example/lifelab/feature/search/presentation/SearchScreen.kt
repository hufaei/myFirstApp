package com.example.lifelab.feature.search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.lifelab.R
import com.example.lifelab.core.ui.components.LifeLabPrimaryActionRow
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.core.ui.components.LifeLabSectionTitle
import com.example.lifelab.core.ui.components.LifeLabStateCard
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType

@Composable
fun SearchScreen(
    uiState: SearchUiState,
    contentPadding: PaddingValues,
    onQueryChanged: (String) -> Unit,
    onSubmitQuery: () -> Unit,
    onFilterSelected: (SearchFilter) -> Unit,
    onHotKeywordClick: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
    onRetryClick: () -> Unit,
    onResultClick: (String) -> Unit,
    onResultDetailDismiss: () -> Unit,
    onOpenResultDestination: (SearchResultType) -> Unit,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val headerBack = if (uiState.selectedResultDetail != null) {
        onResultDetailDismiss
    } else {
        onBack
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SearchInputSection(
                query = uiState.query,
                onQueryChanged = onQueryChanged,
                onSubmitQuery = onSubmitQuery,
                onBack = headerBack,
            )
        }

        item {
            SearchFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = onFilterSelected,
            )
        }

        uiState.selectedResultDetail?.let { detail ->
            item {
                SearchResultDetailCard(
                    detail = detail,
                    onDismiss = onResultDetailDismiss,
                    onOpenDestination = onOpenResultDestination,
                )
            }
        }

        when (val resultContent = uiState.resultContent) {
            SearchResultContent.Idle -> {
                item {
                    PreSearchContent(
                        hotKeywords = uiState.hotKeywords,
                        history = uiState.history,
                        onHotKeywordClick = onHotKeywordClick,
                        onHistoryClick = onHistoryClick,
                        onClearHistoryClick = onClearHistoryClick,
                    )
                }
            }

            SearchResultContent.Loading -> {
                item {
                    LoadingState()
                }
            }

            is SearchResultContent.Content -> {
                items(
                    items = resultContent.items,
                    key = SearchResultItem::id,
                ) { item ->
                    SearchResultRow(
                        item = item,
                        onClick = onResultClick,
                    )
                }
            }

            SearchResultContent.Empty -> {
                item {
                    EmptyState()
                }
            }

            is SearchResultContent.Error -> {
                item {
                    ErrorState(
                        message = resultContent.message,
                        onRetryClick = onRetryClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchInputSection(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSubmitQuery: () -> Unit,
    onBack: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LifeLabScreenHeader(
            title = stringResource(R.string.search_title),
            onBack = onBack,
        )
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(stringResource(R.string.search_keyword_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSubmitQuery() }),
            modifier = Modifier.fillMaxWidth(),
        )
        LifeLabPrimaryActionRow(
            primaryLabel = stringResource(R.string.search_action),
            onPrimaryClick = onSubmitQuery,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchFilterChips(
    selectedFilter: SearchFilter,
    onFilterSelected: (SearchFilter) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SearchFilter.entries.forEach { filter ->
            FilterChip(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.label()) },
            )
        }
    }
}

@Composable
private fun PreSearchContent(
    hotKeywords: List<String>,
    history: List<String>,
    onHotKeywordClick: (String) -> Unit,
    onHistoryClick: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        KeywordSection(
            title = stringResource(R.string.search_hot_keywords),
            keywords = hotKeywords,
            onKeywordClick = onHotKeywordClick,
        )
        HistorySection(
            history = history,
            onHistoryClick = onHistoryClick,
            onClearHistoryClick = onClearHistoryClick,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordSection(
    title: String,
    keywords: List<String>,
    onKeywordClick: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LifeLabSectionTitle(title = title)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            keywords.forEach { keyword ->
                AssistChip(
                    onClick = { onKeywordClick(keyword) },
                    label = { Text(keyword) },
                )
            }
        }
    }
}

@Composable
private fun HistorySection(
    history: List<String>,
    onHistoryClick: (String) -> Unit,
    onClearHistoryClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LifeLabSectionTitle(
            title = stringResource(R.string.search_history),
            actionLabel = stringResource(R.string.search_clear_history).takeIf { history.isNotEmpty() },
            onAction = onClearHistoryClick.takeIf { history.isNotEmpty() },
        )

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.search_no_recent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                history.forEach { query ->
                    Text(
                        text = query,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHistoryClick(query) }
                            .padding(vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultDetailCard(
    detail: SearchResultDetail,
    onDismiss: () -> Unit,
    onOpenDestination: (SearchResultType) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(14.dp),
        ) {
            Text(
                text = detail.type.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = detail.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (detail.summary.isNotBlank()) {
                Text(
                    text = detail.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            LifeLabPrimaryActionRow(
                primaryLabel = stringResource(R.string.search_detail_open_related),
                onPrimaryClick = { onOpenDestination(detail.type) },
                secondaryLabel = stringResource(R.string.common_dismiss),
                onSecondaryClick = onDismiss,
            )
        }
    }
}

@Composable
private fun SearchResultRow(
    item: SearchResultItem,
    onClick: (String) -> Unit,
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(item.id) },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Text(
                text = item.type.label(),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
            )
            if (item.summary.isNotBlank()) {
                Text(
                    text = item.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LoadingState() {
    LifeLabStateCard(title = stringResource(R.string.search_searching))
}

@Composable
private fun EmptyState() {
    LifeLabStateCard(
        title = stringResource(R.string.search_empty_title),
        body = stringResource(R.string.search_empty_body),
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
) {
    LifeLabStateCard(
        title = stringResource(R.string.search_error_title),
        body = message,
        actionLabel = stringResource(R.string.common_retry),
        onAction = onRetryClick,
    )
}

@Composable
private fun SearchFilter.label(): String =
    when (this) {
        SearchFilter.ALL -> stringResource(R.string.search_filter_all)
        SearchFilter.ARTICLES -> stringResource(R.string.search_filter_articles)
        SearchFilter.OFFERS -> stringResource(R.string.search_filter_offers)
        SearchFilter.TASKS -> stringResource(R.string.search_filter_tasks)
        SearchFilter.HABITS -> stringResource(R.string.search_filter_habits)
        SearchFilter.NOTIFICATIONS -> stringResource(R.string.search_filter_notifications)
    }

@Composable
private fun SearchResultType.label(): String =
    when (this) {
        SearchResultType.ARTICLE -> stringResource(R.string.search_type_article)
        SearchResultType.OFFER -> stringResource(R.string.search_type_offer)
        SearchResultType.TASK -> stringResource(R.string.search_type_task)
        SearchResultType.HABIT -> stringResource(R.string.search_type_habit)
        SearchResultType.NOTIFICATION -> stringResource(R.string.search_type_notification)
    }
