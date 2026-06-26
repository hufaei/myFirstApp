package com.example.lifelab.feature.discover.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.domain.DiscoverContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.test.runTest

class RoomDiscoverRepositoryTest {

    @Test
    fun getContentMapsStoredRowsInSortOrder() = runTest {
        val repository = RoomDiscoverRepository(
            object : DiscoverDao {
                override suspend fun getContent(): List<DiscoverContentEntity> = listOf(
                    DiscoverContentEntity(
                        id = "course-focus",
                        type = DiscoverContentType.Course.name,
                        title = "Focus Reset",
                        summary = "Protect deep work.",
                        tag = "Productivity",
                        author = null,
                        instructor = "Maya Chen",
                        duration = "42 min",
                        priceLabel = null,
                        sortOrder = 0,
                    ),
                    DiscoverContentEntity(
                        id = "membership-plus",
                        type = DiscoverContentType.Membership.name,
                        title = "LifeLab Plus",
                        summary = "Monthly labs.",
                        tag = "Membership",
                        author = null,
                        instructor = null,
                        duration = null,
                        priceLabel = "${'$'}9/month",
                        sortOrder = 1,
                    ),
                )

                override suspend fun insertContent(content: List<DiscoverContentEntity>) = Unit

                override suspend fun countContent(): Int = 2
            },
        )

        val content = assertIs<AppResult.Success<List<DiscoverContent>>>(repository.getContent()).value

        assertEquals("Focus Reset", content[0].title)
        assertIs<DiscoverContent.Course>(content[0])
        assertIs<DiscoverContent.Offer.Membership>(content[1])
    }
}
