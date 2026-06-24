package com.example.lifelab.feature.home.presentation

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.home.domain.BuildHomeFeedUseCase
import com.example.lifelab.feature.home.domain.HomeDiscoveryTeaser
import com.example.lifelab.feature.home.domain.HomeFeedRepository
import com.example.lifelab.feature.home.domain.HomeFeedSeed
import com.example.lifelab.feature.home.domain.HomeHabitInsight
import com.example.lifelab.feature.home.domain.HomeRecommendationEntry
import com.example.lifelab.feature.home.domain.HomeTaskSummary
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun loadsHomeContentWhenCreated() = runTest {
        val viewModel = HomeViewModel(
            buildHomeFeed = BuildHomeFeedUseCase(
                repository = QueueHomeFeedRepository(
                    AppResult.Success(displayableSeed()),
                ),
            ),
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Plan today's focus", state.content?.recommendedEntries?.single()?.title)
        assertEquals(3, state.content?.feedItems?.size)
    }

    @Test
    fun showsErrorStateWhenInitialLoadFails() = runTest {
        val viewModel = HomeViewModel(
            buildHomeFeed = BuildHomeFeedUseCase(
                repository = QueueHomeFeedRepository(
                    AppResult.Failure(AppError.Network(message = "Feed unavailable")),
                ),
            ),
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Feed unavailable", state.errorMessage)
        assertEquals(null, state.content)
    }

    @Test
    fun refreshKeepsExistingContentWhenRefreshFails() = runTest {
        val viewModel = HomeViewModel(
            buildHomeFeed = BuildHomeFeedUseCase(
                repository = QueueHomeFeedRepository(
                    AppResult.Success(displayableSeed()),
                    AppResult.Failure(AppError.Network(message = "Refresh failed")),
                ),
            ),
        )

        viewModel.onEvent(HomeUiEvent.Refresh)

        val state = viewModel.uiState.value
        assertFalse(state.isRefreshing)
        assertEquals("Refresh failed", state.errorMessage)
        assertEquals("Plan today's focus", state.content?.recommendedEntries?.single()?.title)
    }

    @Test
    fun retryReplacesErrorWithLoadedContent() = runTest {
        val viewModel = HomeViewModel(
            buildHomeFeed = BuildHomeFeedUseCase(
                repository = QueueHomeFeedRepository(
                    AppResult.Failure(AppError.Network(message = "Feed unavailable")),
                    AppResult.Success(displayableSeed()),
                ),
            ),
        )

        viewModel.onEvent(HomeUiEvent.Retry)

        val state = viewModel.uiState.value
        assertEquals(null, state.errorMessage)
        assertTrue(state.content?.feedItems?.isNotEmpty() == true)
    }

    private fun displayableSeed(): HomeFeedSeed =
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
            taskSummary = HomeTaskSummary(openTaskCount = 4, dueTodayCount = 2),
            habitInsight = HomeHabitInsight(
                checkedInTodayCount = 1,
                totalHabitCount = 3,
                bestStreakCount = 6,
            ),
            discoveryTeaser = HomeDiscoveryTeaser(
                title = "Energy management basics",
                description = "A short read for planning sustainable work.",
            ),
        )

    private class QueueHomeFeedRepository(
        vararg results: AppResult<HomeFeedSeed>,
    ) : HomeFeedRepository {
        private val queuedResults = ArrayDeque<AppResult<HomeFeedSeed>>().apply {
            addAll(results.toList())
        }

        override suspend fun loadHomeFeedSeed(): AppResult<HomeFeedSeed> =
            queuedResults.removeFirst()
    }
}
