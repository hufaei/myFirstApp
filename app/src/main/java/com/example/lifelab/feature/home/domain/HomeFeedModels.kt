package com.example.lifelab.feature.home.domain

data class HomeFeedSeed(
    val recommendedEntries: List<HomeRecommendationEntry> = emptyList(),
    val taskSummary: HomeTaskSummary? = null,
    val habitInsight: HomeHabitInsight? = null,
    val discoveryTeaser: HomeDiscoveryTeaser? = null,
)

data class HomeFeedContent(
    val recommendedEntries: List<HomeRecommendationEntry>,
    val feedItems: List<HomeFeedItem>,
)

data class HomeRecommendationEntry(
    val id: String,
    val title: String,
    val subtitle: String,
    val actionLabel: String,
    val route: String,
)

data class HomeTaskSummary(
    val openTaskCount: Int,
    val dueTodayCount: Int,
)

data class HomeHabitInsight(
    val checkedInTodayCount: Int,
    val totalHabitCount: Int,
    val bestStreakCount: Int,
)

data class HomeDiscoveryTeaser(
    val title: String,
    val description: String,
)

sealed interface HomeFeedItem {
    val id: String

    data class TaskSummary(
        val summary: HomeTaskSummary,
    ) : HomeFeedItem {
        override val id: String = "tasks-summary"
    }

    data class HabitInsight(
        val insight: HomeHabitInsight,
    ) : HomeFeedItem {
        override val id: String = "habit-insight"
    }

    data class DiscoveryTeaser(
        val teaser: HomeDiscoveryTeaser,
    ) : HomeFeedItem {
        override val id: String = "discovery-teaser"
    }

    object EmptyState : HomeFeedItem {
        override val id: String = "empty-state"
    }
}
