package com.example.lifelab.feature.discover.domain

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class LoadDiscoverContentUseCaseTest {
    @Test
    fun `all category returns mixed content in repository order`() = runTest {
        val content = discoverContentFixture()
        val useCase = LoadDiscoverContentUseCase(SuccessDiscoverRepository(content))

        val result = useCase(DiscoverCategory.All)

        assertEquals(AppResult.Success(content), result)
    }

    @Test
    fun `article category returns only articles`() = runTest {
        val useCase = LoadDiscoverContentUseCase(SuccessDiscoverRepository(discoverContentFixture()))

        val result = useCase(DiscoverCategory.Articles)

        assertEquals(
            listOf("article-1"),
            (result as AppResult.Success).value.map { it.id },
        )
    }

    @Test
    fun `course category returns only courses`() = runTest {
        val useCase = LoadDiscoverContentUseCase(SuccessDiscoverRepository(discoverContentFixture()))

        val result = useCase(DiscoverCategory.Courses)

        assertEquals(
            listOf("course-1"),
            (result as AppResult.Success).value.map { it.id },
        )
    }

    @Test
    fun `offers category returns products and memberships`() = runTest {
        val useCase = LoadDiscoverContentUseCase(SuccessDiscoverRepository(discoverContentFixture()))

        val result = useCase(DiscoverCategory.Offers)

        assertEquals(
            listOf("product-1", "membership-1"),
            (result as AppResult.Success).value.map { it.id },
        )
    }

    @Test
    fun `membership category returns only memberships`() = runTest {
        val useCase = LoadDiscoverContentUseCase(SuccessDiscoverRepository(discoverContentFixture()))

        val result = useCase(DiscoverCategory.Membership)

        assertEquals(
            listOf("membership-1"),
            (result as AppResult.Success).value.map { it.id },
        )
    }

    @Test
    fun `repository failure is returned unchanged`() = runTest {
        val error = AppError.Unknown("Discover content unavailable")
        val useCase = LoadDiscoverContentUseCase(FailureDiscoverRepository(error))

        val result = useCase(DiscoverCategory.All)

        assertEquals(AppResult.Failure(error), result)
        assertSame(error, (result as AppResult.Failure).error)
    }

    private fun discoverContentFixture(): List<DiscoverContent> =
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
            DiscoverContent.Offer.Membership(
                id = "membership-1",
                title = "LifeLab 会员",
                summary = "每月实验室、模板和专家场。",
                priceLabel = "¥9/月",
                tag = "会员",
            ),
        )

    private class SuccessDiscoverRepository(
        private val content: List<DiscoverContent>,
    ) : DiscoverRepository {
        override suspend fun getContent(): AppResult<List<DiscoverContent>> = AppResult.Success(content)
    }

    private class FailureDiscoverRepository(
        private val error: AppError,
    ) : DiscoverRepository {
        override suspend fun getContent(): AppResult<List<DiscoverContent>> = AppResult.Failure(error)
    }
}
