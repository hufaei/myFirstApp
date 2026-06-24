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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifelab.feature.home.domain.HomeFeedContent
import com.example.lifelab.feature.home.domain.HomeFeedItem
import com.example.lifelab.feature.home.domain.HomeRecommendationEntry

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onEvent: (HomeUiEvent) -> Unit,
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
            HomeContentSection(content = content)
        }
    }
}

@Composable
private fun HomeHeader(
    isActionEnabled: Boolean,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Home",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                text = "Today in LifeLab",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedButton(
            onClick = onRefresh,
            enabled = isActionEnabled,
        ) {
            Text(text = "Refresh")
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
                text = "Loading your home feed",
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
            text = "Refreshing feed",
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
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun HomeContentSection(content: HomeFeedContent) {
    RecommendedEntriesSection(entries = content.recommendedEntries)
    FeedSection(items = content.feedItems)
}

@Composable
private fun RecommendedEntriesSection(entries: List<HomeRecommendationEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Recommended",
            style = MaterialTheme.typography.titleLarge,
        )

        if (entries.isEmpty()) {
            EmptySection(message = "No recommendations yet")
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
                    Button(onClick = {}) {
                        Text(text = entry.actionLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedSection(items: List<HomeFeedItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Feed",
            style = MaterialTheme.typography.titleLarge,
        )

        if (items.isEmpty() || items.all { it == HomeFeedItem.EmptyState }) {
            EmptySection(message = "Your feed will appear here as LifeLab learns from your activity")
            return@Column
        }

        items.filterNot { it == HomeFeedItem.EmptyState }.forEach { item ->
            FeedCard(item = item)
        }
    }
}

@Composable
private fun FeedCard(item: HomeFeedItem) {
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

private fun feedItemTitle(item: HomeFeedItem): String =
    when (item) {
        is HomeFeedItem.TaskSummary -> "Task summary"
        is HomeFeedItem.HabitInsight -> "Habit insight"
        is HomeFeedItem.DiscoveryTeaser -> item.teaser.title
        HomeFeedItem.EmptyState -> "Home update"
    }

private fun feedItemDescription(item: HomeFeedItem): String =
    when (item) {
        is HomeFeedItem.TaskSummary -> {
            "${item.summary.openTaskCount} open tasks | ${item.summary.dueTodayCount} due today"
        }
        is HomeFeedItem.HabitInsight -> {
            "${item.insight.checkedInTodayCount}/${item.insight.totalHabitCount} checked in | " +
                "${item.insight.bestStreakCount}-day best streak"
        }
        is HomeFeedItem.DiscoveryTeaser -> item.teaser.description
        HomeFeedItem.EmptyState -> "Your feed will appear here as LifeLab learns from your activity"
    }
