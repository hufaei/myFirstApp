package com.example.lifelab.feature.home.domain

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BuildHomeFeedUseCaseTest {

    @Test
    fun buildsFeedFromRecommendedEntryAndSourceSignals() = kotlinx.coroutines.test.runTest {
        val useCase = BuildHomeFeedUseCase(
            repository = FakeHomeFeedRepository(
                result = AppResult.Success(
                    HomeFeedSeed(
                        recommendedEntries = listOf(
                            HomeRecommendationEntry(
                                id = "today-focus",
                                title = "Plan today's focus",
                                subtitle = "Turn priorities into a short action list.",
                                actionLabel = "Open Tasks",
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
                            title = "Energy management basics",
                            description = "A short read for planning sustainable work.",
                        ),
                    ),
                ),
            ),
        )

        val result = assertIs<AppResult.Success<HomeFeedContent>>(useCase())

        assertEquals("Plan today's focus", result.value.recommendedEntries.single().title)
        assertEquals(
            listOf(
                "tasks-summary",
                "habit-insight",
                "discovery-teaser",
            ),
            result.value.feedItems.map { it.id },
        )
    }

    @Test
    fun buildsEmptyPlaceholderWhenSeedHasNoDisplayableContent() = kotlinx.coroutines.test.runTest {
        val useCase = BuildHomeFeedUseCase(
            repository = FakeHomeFeedRepository(
                result = AppResult.Success(HomeFeedSeed()),
            ),
        )

        val result = assertIs<AppResult.Success<HomeFeedContent>>(useCase())

        assertEquals(emptyList(), result.value.recommendedEntries)
        assertEquals(HomeFeedItem.EmptyState, result.value.feedItems.single())
    }

    @Test
    fun propagatesRepositoryFailureWithoutReplacingItWithEmptyContent() = kotlinx.coroutines.test.runTest {
        val useCase = BuildHomeFeedUseCase(
            repository = FakeHomeFeedRepository(
                result = AppResult.Failure(AppError.Network(message = "Feed unavailable")),
            ),
        )

        val result = assertIs<AppResult.Failure>(useCase())

        assertEquals("Feed unavailable", result.error.message)
    }

    private class FakeHomeFeedRepository(
        private val result: AppResult<HomeFeedSeed>,
    ) : HomeFeedRepository {
        override suspend fun loadHomeFeedSeed(): AppResult<HomeFeedSeed> = result
    }
}
