package com.example.lifelab.feature.discover.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.domain.DiscoverContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class InMemoryDiscoverRepositoryTest {
    @Test
    fun `seed content covers all discover content types`() = runTest {
        val repository = InMemoryDiscoverRepository()

        val result = repository.getContent()

        val content = (result as AppResult.Success).value
        assertTrue(content.any { it is DiscoverContent.Article })
        assertTrue(content.any { it is DiscoverContent.Course })
        assertTrue(content.any { it is DiscoverContent.Offer.Product })
        assertTrue(content.any { it is DiscoverContent.Offer.Membership })
    }

    @Test
    fun `seed content uses stable presentation-ready values`() = runTest {
        val repository = InMemoryDiscoverRepository()

        val result = repository.getContent()

        val content = (result as AppResult.Success).value
        assertEquals(
            listOf(
                "article-morning-systems",
                "course-focus-reset",
                "product-habit-journal",
                "membership-lifelab-plus",
            ),
            content.map { it.id },
        )
        assertTrue(content.all { it.title.isNotBlank() && it.summary.isNotBlank() && it.tag.isNotBlank() })
    }
}
