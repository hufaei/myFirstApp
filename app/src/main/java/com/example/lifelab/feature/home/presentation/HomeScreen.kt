package com.example.lifelab.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.lifelab.R
import com.example.lifelab.feature.home.domain.HomeFeedContent
import com.example.lifelab.feature.home.domain.HomeFeedItem
import com.example.lifelab.feature.home.domain.HomeRecommendationEntry

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenRoute: (String) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenDiscover: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HomeHeader(
            isActionEnabled = !uiState.isLoading && !uiState.isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.Refresh) },
            onOpenSearch = onOpenSearch,
            onOpenNotifications = onOpenNotifications,
        )
        HomeQuickActions(
            onOpenDiscover = onOpenDiscover,
        )

        if (uiState.isLoading && uiState.content == null) {
            LoadingSection()
        }

        if (uiState.isRefreshing) {
            RefreshingSection()
        }

        uiState.errorMessage?.let { message ->
            ErrorSection(
                message = message,
                onRetry = { onEvent(HomeUiEvent.Retry) },
            )
        }

        uiState.content?.let { content ->
            HomeContentSection(
                content = content,
                onOpenRoute = onOpenRoute,
                onOpenDiscover = onOpenDiscover,
            )
        }
    }
}

@Composable
private fun HomeHeader(
    isActionEnabled: Boolean,
    onRefresh: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenNotifications: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(R.string.home_title),
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onOpenSearch) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = stringResource(R.string.home_action_search),
                )
            }
            IconButton(onClick = onOpenNotifications) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = stringResource(R.string.home_action_notifications),
                )
            }
            IconButton(
                onClick = onRefresh,
                enabled = isActionEnabled,
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.common_refresh),
                )
            }
        }
    }
}

@Composable
private fun HomeQuickActions(
    onOpenDiscover: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = stringResource(R.string.home_quick_actions),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = stringResource(R.string.home_quick_actions_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(onClick = onOpenDiscover) {
                Icon(
                    imageVector = Icons.Filled.Explore,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.home_action_discover))
            }
        }
    }
}

@Composable
private fun LoadingSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.home_loading),
                style = MaterialTheme.typography.titleMedium,
            )
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun RefreshingSection() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.home_refreshing),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ErrorSection(
    message: String,
    onRetry: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
            )
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.common_retry))
            }
        }
    }
}

@Composable
private fun HomeContentSection(
    content: HomeFeedContent,
    onOpenRoute: (String) -> Unit,
    onOpenDiscover: () -> Unit,
) {
    RecommendedEntriesSection(
        entries = content.recommendedEntries,
        onOpenRoute = onOpenRoute,
    )
    FeedSection(
        items = content.feedItems,
        onOpenDiscover = onOpenDiscover,
    )
}

@Composable
private fun RecommendedEntriesSection(
    entries: List<HomeRecommendationEntry>,
    onOpenRoute: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.home_recommended),
            style = MaterialTheme.typography.titleLarge,
        )

        if (entries.isEmpty()) {
            EmptySection(message = stringResource(R.string.home_no_recommendations))
            return@Column
        }

        entries.forEach { entry ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Button(onClick = { onOpenRoute(entry.route) }) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = entry.actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedSection(
    items: List<HomeFeedItem>,
    onOpenDiscover: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.home_feed),
            style = MaterialTheme.typography.titleLarge,
        )

        if (items.isEmpty() || items.all { it == HomeFeedItem.EmptyState }) {
            EmptySection(message = stringResource(R.string.home_feed_empty))
            return@Column
        }

        items.filterNot { it == HomeFeedItem.EmptyState }.forEach { item ->
            FeedCard(
                item = item,
                onOpenDiscover = onOpenDiscover,
            )
        }
    }
}

@Composable
private fun FeedCard(
    item: HomeFeedItem,
    onOpenDiscover: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = feedItemTitle(item),
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = feedItemDescription(item),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (item is HomeFeedItem.DiscoveryTeaser) {
                FilledTonalButton(onClick = onOpenDiscover) {
                    Icon(
                        imageVector = Icons.Filled.Explore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = stringResource(R.string.home_action_discover))
                }
            }
        }
    }
}

@Composable
private fun EmptySection(message: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun feedItemTitle(item: HomeFeedItem): String =
    when (item) {
        is HomeFeedItem.TaskSummary -> stringResource(R.string.home_task_summary)
        is HomeFeedItem.HabitInsight -> stringResource(R.string.home_habit_insight)
        is HomeFeedItem.DiscoveryTeaser -> item.teaser.title
        HomeFeedItem.EmptyState -> stringResource(R.string.home_update)
    }

@Composable
private fun feedItemDescription(item: HomeFeedItem): String =
    when (item) {
        is HomeFeedItem.TaskSummary -> {
            stringResource(
                R.string.home_task_summary_detail,
                item.summary.openTaskCount,
                item.summary.dueTodayCount,
            )
        }
        is HomeFeedItem.HabitInsight -> {
            stringResource(
                R.string.home_habit_insight_detail,
                item.insight.checkedInTodayCount,
                item.insight.totalHabitCount,
                item.insight.bestStreakCount,
            )
        }
        is HomeFeedItem.DiscoveryTeaser -> item.teaser.description
        HomeFeedItem.EmptyState -> stringResource(R.string.home_feed_empty)
    }
