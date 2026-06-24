package com.example.lifelab.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.lifelab.core.ui.component.ActionCard
import com.example.lifelab.core.ui.component.PulseCard
import com.example.lifelab.core.ui.component.PulseMetric
import com.example.lifelab.core.ui.component.SectionHeader
import com.example.lifelab.core.ui.component.StatePanel
import com.example.lifelab.feature.home.domain.HomeDiscoveryTeaser
import com.example.lifelab.feature.home.domain.HomeFeedContent
import com.example.lifelab.feature.home.domain.HomeFeedItem
import com.example.lifelab.feature.home.domain.HomeHabitInsight
import com.example.lifelab.feature.home.domain.HomeRecommendationEntry
import com.example.lifelab.feature.home.domain.HomeTaskSummary

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
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.isLoading && uiState.content == null) {
            StatePanel(
                title = "Preparing today",
                body = "LifeLab is collecting your tasks, habits, and discovery signals.",
                isLoading = true,
            )
        }

        if (uiState.isRefreshing) {
            StatePanel(
                title = "Refreshing today",
                body = "Updating the pulse without moving your place.",
                isLoading = true,
            )
        }

        uiState.errorMessage?.let { message ->
            StatePanel(
                title = "Today could not update",
                body = message,
                actionLabel = "Try again",
                onAction = { onEvent(HomeUiEvent.Retry) },
            )
        }

        uiState.content?.let { content ->
            TodayContent(
                content = content,
                onRefresh = { onEvent(HomeUiEvent.Refresh) },
            )
        }
    }
}

@Composable
private fun TodayContent(
    content: HomeFeedContent,
    onRefresh: () -> Unit,
) {
    val taskSummary = content.taskSummary()
    val habitInsight = content.habitInsight()
    val discoveryTeaser = content.discoveryTeaser()

    PulseCard(
        title = "Today focus",
        subtitle = "A compact read on what needs attention before anything else.",
        metrics = listOf(
            PulseMetric(
                label = "Open tasks",
                value = (taskSummary?.openTaskCount ?: 0).toString(),
                helper = "${taskSummary?.dueTodayCount ?: 0} due today",
            ),
            PulseMetric(
                label = "Habits",
                value = "${habitInsight?.checkedInTodayCount ?: 0}/${habitInsight?.totalHabitCount ?: 0}",
                helper = "${habitInsight?.bestStreakCount ?: 0} day best",
            ),
            PulseMetric(
                label = "Next idea",
                value = if (discoveryTeaser == null) "0" else "1",
                helper = "ready to review",
            ),
        ),
    )

    SectionHeader(
        title = "Today focus",
        subtitle = "Start with the most concrete next step.",
        actionLabel = "Refresh",
        onAction = onRefresh,
    )

    ActionCard(
        title = taskFocusTitle(taskSummary),
        body = taskFocusBody(taskSummary),
        actionLabel = "Open workbench",
        onAction = {},
    )

    habitInsight?.let {
        ActionCard(
            title = "Keep the streak visible",
            body = "${it.checkedInTodayCount} of ${it.totalHabitCount} habits checked in. Best streak is ${it.bestStreakCount} days.",
            actionLabel = "Review habits",
            onAction = {},
        )
    }

    SectionHeader(
        title = "For you",
        subtitle = "Useful after the plan is clear.",
    )

    RecommendedEntriesSection(entries = content.recommendedEntries)

    discoveryTeaser?.let {
        ActionCard(
            title = it.title,
            body = it.description,
            actionLabel = "Read next",
            onAction = {},
        )
    }
}

@Composable
private fun RecommendedEntriesSection(entries: List<HomeRecommendationEntry>) {
    if (entries.isEmpty()) {
        StatePanel(
            title = "No recommendations yet",
            body = "Use LifeLab for a little longer and this space will collect relevant next steps.",
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        entries.forEach { entry ->
            ActionCard(
                title = entry.title,
                body = entry.subtitle,
                actionLabel = entry.actionLabel,
                onAction = {},
            )
        }
    }
}

private fun taskFocusTitle(summary: HomeTaskSummary?): String =
    when {
        summary == null -> "Set one clear next action"
        summary.dueTodayCount > 0 -> "Clear today's due work"
        summary.openTaskCount > 0 -> "Choose the next open task"
        else -> "No task pressure today"
    }

private fun taskFocusBody(summary: HomeTaskSummary?): String =
    when {
        summary == null -> "Workbench is ready when you want to add structure."
        summary.dueTodayCount > 0 -> "${summary.dueTodayCount} due today across ${summary.openTaskCount} open tasks."
        summary.openTaskCount > 0 -> "${summary.openTaskCount} open tasks are waiting for prioritization."
        else -> "Use the space for a habit check-in or a discovery pass."
    }

private fun HomeFeedContent.taskSummary(): HomeTaskSummary? =
    feedItems.firstNotNullOfOrNull { item ->
        (item as? HomeFeedItem.TaskSummary)?.summary
    }

private fun HomeFeedContent.habitInsight(): HomeHabitInsight? =
    feedItems.firstNotNullOfOrNull { item ->
        (item as? HomeFeedItem.HabitInsight)?.insight
    }

private fun HomeFeedContent.discoveryTeaser(): HomeDiscoveryTeaser? =
    feedItems.firstNotNullOfOrNull { item ->
        (item as? HomeFeedItem.DiscoveryTeaser)?.teaser
    }
