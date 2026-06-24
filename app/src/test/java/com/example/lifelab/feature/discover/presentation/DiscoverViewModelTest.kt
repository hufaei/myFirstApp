package com.example.lifelab.feature.discover.presentation

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.discover.domain.DiscoverCategory
import com.example.lifelab.feature.discover.domain.DiscoverContent
import com.example.lifelab.feature.discover.domain.DiscoverRepository
import com.example.lifelab.feature.discover.domain.LoadDiscoverContentUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.Rule

class DiscoverViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load success enters content state`() {
        val viewModel = DiscoverViewModel(useCaseWith(discoverContentFixture()))

        val state = viewModel.uiState.value

        assertEquals(DiscoverCategory.All, state.selectedCategory)
        assertEquals(
            discoverContentFixture().map { it.id },
            (state.listState as DiscoverListState.Content).items.map { it.id },
        )
    }

    @Test
    fun `selecting membership with no membership content enters empty state`() {
        val viewModel = DiscoverViewModel(
            useCaseWith(discoverContentFixtureWithoutMembership()),
        )

        viewModel.onEvent(DiscoverUiEvent.CategorySelected(DiscoverCategory.Membership))

        val state = viewModel.uiState.value
        assertEquals(DiscoverCategory.Membership, state.selectedCategory)
        assertIs<DiscoverListState.Empty>(state.listState)
    }

    @Test
    fun `repository failure enters error state`() {
        val error = AppError.Unknown("Discover content unavailable")
        val viewModel = DiscoverViewModel(LoadDiscoverContentUseCase(FakeDiscoverRepository(error)))

        val state = viewModel.uiState.value

        assertEquals(DiscoverCategory.All, state.selectedCategory)
        assertEquals(error.message, (state.listState as DiscoverListState.Error).message)
    }

    @Test
    fun `retry event after failure can recover content state`() {
        val repository = FakeDiscoverRepository(AppError.Network("Temporary outage"))
        val viewModel = DiscoverViewModel(LoadDiscoverContentUseCase(repository))

        repository.result = AppResult.Success(discoverContentFixture())
        viewModel.onEvent(DiscoverUiEvent.RetrySelected)

        assertEquals(
            discoverContentFixture().map { it.id },
            (viewModel.uiState.value.listState as DiscoverListState.Content).items.map { it.id },
        )
    }

    @Test
    fun `category selection event updates selected category and filters content`() {
        val viewModel = DiscoverViewModel(useCaseWith(discoverContentFixture()))

        viewModel.onEvent(DiscoverUiEvent.CategorySelected(DiscoverCategory.Courses))

        val state = viewModel.uiState.value
        assertEquals(DiscoverCategory.Courses, state.selectedCategory)
        assertEquals(
            listOf("course-1"),
            (state.listState as DiscoverListState.Content).items.map { it.id },
        )
    }

    private fun useCaseWith(content: List<DiscoverContent>): LoadDiscoverContentUseCase =
        LoadDiscoverContentUseCase(FakeDiscoverRepository(content))

    private fun discoverContentFixture(): List<DiscoverContent> =
        discoverContentFixtureWithoutMembership() + DiscoverContent.Offer.Membership(
            id = "membership-1",
            title = "LifeLab Plus",
            summary = "Monthly labs, templates, and expert sessions.",
            priceLabel = "${'$'}9/month",
            tag = "Membership",
        )

    private fun discoverContentFixtureWithoutMembership(): List<DiscoverContent> =
        listOf(
            DiscoverContent.Article(
                id = "article-1",
                title = "Design a better morning",
                summary = "A short guide to making daily experiments stick.",
                author = "LifeLab Editorial",
                tag = "Focus",
            ),
            DiscoverContent.Course(
                id = "course-1",
                title = "Deep Work Sprint",
                summary = "A compact course for protecting attention.",
                instructor = "Maya Chen",
                duration = "42 min",
                tag = "Productivity",
            ),
            DiscoverContent.Offer.Product(
                id = "product-1",
                title = "Habit Journal",
                summary = "A guided notebook for tracking growth experiments.",
                priceLabel = "${'$'}18",
                tag = "Tools",
            ),
        )

    private class FakeDiscoverRepository : DiscoverRepository {
        var result: AppResult<List<DiscoverContent>>

        constructor(content: List<DiscoverContent>) {
            result = AppResult.Success(content)
        }

        constructor(error: AppError) {
            result = AppResult.Failure(error)
        }

        override fun getContent(): AppResult<List<DiscoverContent>> = result
    }
}
