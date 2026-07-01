package com.example.lifelab.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.lifelab.R
import com.example.lifelab.core.ui.components.LifeLabScreenHeader
import com.example.lifelab.core.ui.components.LifeLabSectionTitle
import com.example.lifelab.core.ui.components.LifeLabStateCard
import com.example.lifelab.feature.home.domain.HomeFeedContent
import com.example.lifelab.feature.home.domain.HomeFeedItem
import com.example.lifelab.feature.home.domain.HomeHabitInsight
import com.example.lifelab.feature.home.domain.HomeRecommendationEntry
import com.example.lifelab.feature.home.domain.HomeTaskSummary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onEvent: (HomeUiEvent) -> Unit,
    onOpenRoute: (String) -> Unit = {},
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onCreateTask: () -> Unit = {},
    onOpenTasks: () -> Unit = {},
    onOpenHabits: () -> Unit = {},
    onOpenDiscover: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        HomeHeader(
            isActionEnabled = !uiState.isLoading && !uiState.isRefreshing,
            onRefresh = { onEvent(HomeUiEvent.Refresh) },
            onOpenSearch = onOpenSearch,
            onOpenNotifications = onOpenNotifications,
        )

        if (uiState.isRefreshing) {
            RefreshingSection()
        }

        uiState.errorMessage?.let { message ->
            LifeLabStateCard(
                title = message,
                actionLabel = stringResource(R.string.common_retry),
                onAction = { onEvent(HomeUiEvent.Retry) },
            )
        }

        if (uiState.isLoading && uiState.content == null) {
            LoadingSection()
        }

        uiState.content?.let { content ->
            HomeContentSection(
                content = content,
                onOpenRoute = onOpenRoute,
                onCreateTask = onCreateTask,
                onOpenTasks = onOpenTasks,
                onOpenHabits = onOpenHabits,
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
    val today = LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL))

    LifeLabScreenHeader(
        title = stringResource(R.string.home_title),
        subtitle = stringResource(R.string.home_today_subtitle, today),
        actions = {
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
        },
    )
}

@Composable
private fun LoadingSection() {
    LifeLabStateCard(
        title = stringResource(R.string.home_loading),
        body = stringResource(R.string.home_loading_body),
    )
    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun RefreshingSection() {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        Text(
            text = stringResource(R.string.home_refreshing),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HomeContentSection(
    content: HomeFeedContent,
    onOpenRoute: (String) -> Unit,
    onCreateTask: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenDiscover: () -> Unit,
) {
    val taskSummary = content.feedItems
        .filterIsInstance<HomeFeedItem.TaskSummary>()
        .firstOrNull()
        ?.summary
    val habitInsight = content.feedItems
        .filterIsInstance<HomeFeedItem.HabitInsight>()
        .firstOrNull()
        ?.insight

    DailyDashboardCards(
        taskSummary = taskSummary,
        habitInsight = habitInsight,
        onOpenTasks = onOpenTasks,
        onOpenHabits = onOpenHabits,
    )
    HomeQuickActions(
        onCreateTask = onCreateTask,
        onOpenTasks = onOpenTasks,
        onOpenHabits = onOpenHabits,
        onOpenDiscover = onOpenDiscover,
    )
    RecommendedEntriesSection(
        entries = content.recommendedEntries,
        onOpenRoute = onOpenRoute,
    )
    FeedSection(
        items = content.feedItems.filterIsInstance<HomeFeedItem.DiscoveryTeaser>(),
        onOpenDiscover = onOpenDiscover,
    )
}

@Composable
private fun DailyDashboardCards(
    taskSummary: HomeTaskSummary?,
    habitInsight: HomeHabitInsight?,
    onOpenTasks: () -> Unit,
    onOpenHabits: () -> Unit,
) {
    if (taskSummary == null && habitInsight == null) {
        LifeLabStateCard(
            title = stringResource(R.string.home_daily_empty_title),
            body = stringResource(R.string.home_daily_empty_body),
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LifeLabSectionTitle(title = stringResource(R.string.home_daily_section))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 560.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    taskSummary?.let {
                        TaskSummaryCard(
                            summary = it,
                            onOpenTasks = onOpenTasks,
                        )
                    }
                    habitInsight?.let {
                        HabitInsightCard(
                            insight = it,
                            onOpenHabits = onOpenHabits,
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    taskSummary?.let {
                        TaskSummaryCard(
                            summary = it,
                            onOpenTasks = onOpenTasks,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    habitInsight?.let {
                        HabitInsightCard(
                            insight = it,
                            onOpenHabits = onOpenHabits,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskSummaryCard(
    summary: HomeTaskSummary,
    onOpenTasks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardCard(
        modifier = modifier,
        title = stringResource(R.string.home_task_summary),
        metric = summary.openTaskCount.toString(),
        body = stringResource(
            R.string.home_task_summary_detail,
            summary.openTaskCount,
            summary.dueTodayCount,
        ),
        actionLabel = stringResource(R.string.home_action_tasks),
        onAction = onOpenTasks,
    )
}

@Composable
private fun HabitInsightCard(
    insight: HomeHabitInsight,
    onOpenHabits: () -> Unit,
    modifier: Modifier = Modifier,
) {
    DashboardCard(
        modifier = modifier,
        title = stringResource(R.string.home_habit_insight),
        metric = "${insight.checkedInTodayCount}/${insight.totalHabitCount}",
        body = stringResource(
            R.string.home_habit_insight_detail,
            insight.checkedInTodayCount,
            insight.totalHabitCount,
            insight.bestStreakCount,
        ),
        actionLabel = stringResource(R.string.home_action_habits),
        onAction = onOpenHabits,
    )
}

@Composable
private fun DashboardCard(
    title: String,
    metric: String,
    body: String,
    actionLabel: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = metric,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FilledTonalButton(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun HomeQuickActions(
    onCreateTask: () -> Unit,
    onOpenTasks: () -> Unit,
    onOpenHabits: () -> Unit,
    onOpenDiscover: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LifeLabSectionTitle(title = stringResource(R.string.home_quick_actions))
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 560.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionButton(
                        label = stringResource(R.string.home_action_create_task),
                        onClick = onCreateTask,
                        icon = { QuickActionIcon(Icons.Filled.Add) },
                    )
                    QuickActionButton(
                        label = stringResource(R.string.home_action_tasks),
                        onClick = onOpenTasks,
                        icon = { QuickActionIcon(Icons.Filled.TaskAlt) },
                    )
                    QuickActionButton(
                        label = stringResource(R.string.home_action_habits),
                        onClick = onOpenHabits,
                        icon = { QuickActionIcon(Icons.Filled.CheckCircle) },
                    )
                    QuickActionButton(
                        label = stringResource(R.string.home_action_discover),
                        onClick = onOpenDiscover,
                        icon = { QuickActionIcon(Icons.Filled.Explore) },
                    )
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    QuickActionButton(
                        label = stringResource(R.string.home_action_create_task),
                        onClick = onCreateTask,
                        modifier = Modifier.weight(1f),
                        icon = { QuickActionIcon(Icons.Filled.Add) },
                    )
                    QuickActionButton(
                        label = stringResource(R.string.home_action_tasks),
                        onClick = onOpenTasks,
                        modifier = Modifier.weight(1f),
                        icon = { QuickActionIcon(Icons.Filled.TaskAlt) },
                    )
                    QuickActionButton(
                        label = stringResource(R.string.home_action_habits),
                        onClick = onOpenHabits,
                        modifier = Modifier.weight(1f),
                        icon = { QuickActionIcon(Icons.Filled.CheckCircle) },
                    )
                    QuickActionButton(
                        label = stringResource(R.string.home_action_discover),
                        onClick = onOpenDiscover,
                        modifier = Modifier.weight(1f),
                        icon = { QuickActionIcon(Icons.Filled.Explore) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        icon()
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun QuickActionIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(
        imageVector = imageVector,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun RecommendedEntriesSection(
    entries: List<HomeRecommendationEntry>,
    onOpenRoute: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LifeLabSectionTitle(title = stringResource(R.string.home_recommended))

        if (entries.isEmpty()) {
            LifeLabStateCard(title = stringResource(R.string.home_no_recommendations))
            return@Column
        }

        entries.forEach { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = entry.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    FilledTonalButton(onClick = { onOpenRoute(entry.route) }) {
                        Icon(
                            imageVector = Icons.Filled.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = entry.actionLabel,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedSection(
    items: List<HomeFeedItem.DiscoveryTeaser>,
    onOpenDiscover: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LifeLabSectionTitle(title = stringResource(R.string.home_feed))

        if (items.isEmpty()) {
            LifeLabStateCard(title = stringResource(R.string.home_feed_empty))
            return@Column
        }

        items.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.teaser.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = item.teaser.description,
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
                        Text(
                            text = stringResource(R.string.home_action_discover),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}
