package com.example.lifelab.feature.home.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.home.domain.HomeDiscoveryTeaser
import com.example.lifelab.feature.home.domain.HomeFeedRepository
import com.example.lifelab.feature.home.domain.HomeFeedSeed
import com.example.lifelab.feature.home.domain.HomeHabitInsight
import com.example.lifelab.feature.home.domain.HomeRecommendationEntry
import com.example.lifelab.feature.home.domain.HomeTaskSummary

class SeedHomeFeedRepository : HomeFeedRepository {
    override suspend fun loadHomeFeedSeed(): AppResult<HomeFeedSeed> =
        AppResult.Success(
            HomeFeedSeed(
                recommendedEntries = listOf(
                    HomeRecommendationEntry(
                        id = "today-focus",
                        title = "Plan today's focus",
                        subtitle = "Turn priorities into a short action list.",
                        actionLabel = "Open workbench",
                        route = "workbench",
                    ),
                ),
                taskSummary = HomeTaskSummary(
                    openTaskCount = 4,
                    dueTodayCount = 2,
                ),
                habitInsight = HomeHabitInsight(
                    checkedInTodayCount = 1,
                    totalHabitCount = 3,
                    bestStreakCount = 6,
                ),
                discoveryTeaser = HomeDiscoveryTeaser(
                    title = "Energy management basics",
                    description = "A short read for planning sustainable work.",
                ),
            ),
        )
}
