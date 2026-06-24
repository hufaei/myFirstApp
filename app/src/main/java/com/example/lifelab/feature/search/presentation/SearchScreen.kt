package com.example.lifelab.feature.search.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.lifelab.core.ui.component.ActionCard
import com.example.lifelab.core.ui.component.SectionHeader
import com.example.lifelab.core.ui.component.StatePanel
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
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            SearchInputSection(
                query = uiState.query,
                onQueryChanged = onQueryChanged,
                onSubmitQuery = onSubmitQuery,
            )
        }

        item {
            SearchFilterChips(
                selectedFilter = uiState.selectedFilter,
                onFilterSelected = onFilterSelected,
            )
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
                    SearchResultRow(item = item)
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
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(
            title = "Search",
            subtitle = "Find tasks, habits, articles, and offers.",
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                label = { Text("Keyword") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSubmitQuery() }),
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onSubmitQuery) {
                Text("Search")
            }
        }
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
                label = { Text(filter.label) },
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
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        KeywordSection(
            title = "Hot searches",
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
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
        )
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "History",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f),
            )
            if (history.isNotEmpty()) {
                TextButton(onClick = onClearHistoryClick) {
                    Text("Clear")
                }
            }
        }

        if (history.isEmpty()) {
            Text(
                text = "No recent searches",
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
private fun SearchResultRow(item: SearchResultItem) {
    ActionCard(
        title = item.title,
        body = "${item.type.label}\n${item.summary}",
        actionLabel = "Open",
        onAction = {},
    )
}

@Composable
private fun LoadingState() {
    StatePanel(
        title = "Searching",
        body = "Checking LifeLab content and workspace items.",
        isLoading = true,
    )
}

@Composable
private fun EmptyState() {
    StateMessage(
        title = "No results",
        body = "Try another keyword or filter.",
    )
}

@Composable
private fun ErrorState(
    message: String,
    onRetryClick: () -> Unit,
) {
    StatePanel(
        title = "Search failed",
        body = message,
        actionLabel = "Retry",
        onAction = onRetryClick,
    )
}

@Composable
private fun StateMessage(
    title: String,
    body: String,
) {
    StatePanel(
        title = title,
        body = body,
    )
}

private val SearchFilter.label: String
    get() = when (this) {
        SearchFilter.ALL -> "All"
        SearchFilter.ARTICLES -> "Articles"
        SearchFilter.OFFERS -> "Offers"
        SearchFilter.TASKS -> "Tasks"
        SearchFilter.HABITS -> "Habits"
    }

private val SearchResultType.label: String
    get() = when (this) {
        SearchResultType.ARTICLE -> "Article"
        SearchResultType.OFFER -> "Offer"
        SearchResultType.TASK -> "Task"
        SearchResultType.HABIT -> "Habit"
    }
