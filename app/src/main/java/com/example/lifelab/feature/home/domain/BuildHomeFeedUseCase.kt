package com.example.lifelab.feature.home.domain

import com.example.lifelab.core.common.AppResult

class BuildHomeFeedUseCase(
    private val repository: HomeFeedRepository,
) {
    suspend operator fun invoke(): AppResult<HomeFeedContent> =
        when (val result = repository.loadHomeFeedSeed()) {
            is AppResult.Failure -> result
            is AppResult.Success -> AppResult.Success(buildContent(result.value))
        }

    private fun buildContent(seed: HomeFeedSeed): HomeFeedContent {
        val feedItems = buildList {
            seed.taskSummary?.let { add(HomeFeedItem.TaskSummary(summary = it)) }
            seed.habitInsight?.let { add(HomeFeedItem.HabitInsight(insight = it)) }
            seed.discoveryTeaser?.let { add(HomeFeedItem.DiscoveryTeaser(teaser = it)) }
        }

        return HomeFeedContent(
            recommendedEntries = seed.recommendedEntries,
            feedItems = feedItems.ifEmpty {
                if (seed.recommendedEntries.isEmpty()) {
                    listOf(HomeFeedItem.EmptyState)
                } else {
                    emptyList()
                }
            },
        )
    }
}
