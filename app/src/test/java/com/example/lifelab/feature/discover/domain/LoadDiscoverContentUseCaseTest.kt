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
            DiscoverContent.Offer.Membership(
                id = "membership-1",
                title = "LifeLab Plus",
                summary = "Monthly labs, templates, and expert sessions.",
                priceLabel = "${'$'}9/month",
                tag = "Membership",
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
