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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load success enters content state`() = runTest {
        val viewModel = DiscoverViewModel(useCaseWith(discoverContentFixture()))
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(DiscoverCategory.All, state.selectedCategory)
        assertEquals(
            discoverContentFixture().map { it.id },
            (state.listState as DiscoverListState.Content).items.map { it.id },
        )
    }

    @Test
    fun `selecting membership with no membership content enters empty state`() = runTest {
        val viewModel = DiscoverViewModel(
            useCaseWith(discoverContentFixtureWithoutMembership()),
        )
        advanceUntilIdle()

        viewModel.onEvent(DiscoverUiEvent.CategorySelected(DiscoverCategory.Membership))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DiscoverCategory.Membership, state.selectedCategory)
        assertIs<DiscoverListState.Empty>(state.listState)
    }

    @Test
    fun `repository failure enters error state`() = runTest {
        val error = AppError.Unknown("Discover content unavailable")
        val viewModel = DiscoverViewModel(LoadDiscoverContentUseCase(FakeDiscoverRepository(error)))
        advanceUntilIdle()

        val state = viewModel.uiState.value

        assertEquals(DiscoverCategory.All, state.selectedCategory)
        assertEquals(error.message, (state.listState as DiscoverListState.Error).message)
    }

    @Test
    fun `retry event after failure can recover content state`() = runTest {
        val repository = FakeDiscoverRepository(AppError.Network("Temporary outage"))
        val viewModel = DiscoverViewModel(LoadDiscoverContentUseCase(repository))
        advanceUntilIdle()

        repository.result = AppResult.Success(discoverContentFixture())
        viewModel.onEvent(DiscoverUiEvent.RetrySelected)
        advanceUntilIdle()

        assertEquals(
            discoverContentFixture().map { it.id },
            (viewModel.uiState.value.listState as DiscoverListState.Content).items.map { it.id },
        )
    }

    @Test
    fun `category selection event updates selected category and filters content`() = runTest {
        val viewModel = DiscoverViewModel(useCaseWith(discoverContentFixture()))
        advanceUntilIdle()

        viewModel.onEvent(DiscoverUiEvent.CategorySelected(DiscoverCategory.Courses))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(DiscoverCategory.Courses, state.selectedCategory)
        assertEquals(
            listOf("course-1"),
            (state.listState as DiscoverListState.Content).items.map { it.id },
        )
    }

    @Test
    fun `content selection event opens local detail state`() = runTest {
        val viewModel = DiscoverViewModel(useCaseWith(discoverContentFixture()))
        advanceUntilIdle()

        viewModel.onEvent(DiscoverUiEvent.ContentSelected("course-1"))

        val detail = viewModel.uiState.value.selectedContentDetail
        assertEquals("course-1", detail?.id)
        assertEquals("深度工作冲刺", detail?.title)
        assertEquals("一门保护注意力的紧凑课程。", detail?.summary)
        assertEquals(DiscoverContentKind.Course, detail?.kind)
        assertEquals(DiscoverCategory.Courses, detail?.category)
    }

    private fun useCaseWith(content: List<DiscoverContent>): LoadDiscoverContentUseCase =
        LoadDiscoverContentUseCase(FakeDiscoverRepository(content))

    private fun discoverContentFixture(): List<DiscoverContent> =
        discoverContentFixtureWithoutMembership() + DiscoverContent.Offer.Membership(
            id = "membership-1",
            title = "LifeLab 会员",
            summary = "每月实验室、模板和专家场。",
            priceLabel = "¥9/月",
            tag = "会员",
        )

    private fun discoverContentFixtureWithoutMembership(): List<DiscoverContent> =
        listOf(
            DiscoverContent.Article(
                id = "article-1",
                title = "设计更好的早晨",
                summary = "把日常实验坚持下去的短指南。",
                author = "LifeLab 编辑部",
                tag = "专注",
            ),
            DiscoverContent.Course(
                id = "course-1",
                title = "深度工作冲刺",
                summary = "一门保护注意力的紧凑课程。",
                instructor = "陈明雅",
                duration = "42 分钟",
                tag = "效率",
            ),
            DiscoverContent.Offer.Product(
                id = "product-1",
                title = "习惯手账",
                summary = "一本用于记录成长实验的引导式笔记本。",
                priceLabel = "¥18",
                tag = "工具",
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

        override suspend fun getContent(): AppResult<List<DiscoverContent>> = result
    }
}
