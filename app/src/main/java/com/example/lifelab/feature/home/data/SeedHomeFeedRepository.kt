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
                        title = "规划今日重点",
                        subtitle = "把优先事项整理成一份简短行动清单。",
                        actionLabel = "打开任务",
                        route = "tasks",
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
                    title = "精力管理入门",
                    description = "一篇帮助你安排可持续工作节奏的短读。",
                ),
            ),
        )
}
