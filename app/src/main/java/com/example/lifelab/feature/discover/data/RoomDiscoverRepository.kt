package com.example.lifelab.feature.discover.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.domain.DiscoverContent
import com.example.lifelab.feature.discover.domain.DiscoverRepository

class RoomDiscoverRepository(
    private val discoverDao: DiscoverDao,
) : DiscoverRepository {

    override suspend fun getContent(): AppResult<List<DiscoverContent>> {
        seedIfEmpty()
        return AppResult.Success(discoverDao.getContent().map(DiscoverContentEntity::toDomain))
    }

    suspend fun seedIfEmpty() {
        if (discoverDao.countContent() == 0) {
            discoverDao.insertContent(
                seedContent().mapIndexed { index, content ->
                    content.toEntity(sortOrder = index)
                },
            )
        }
    }

    private companion object {
        fun seedContent(): List<DiscoverContent> =
            listOf(
                DiscoverContent.Article(
                    id = "article-morning-systems",
                    title = "Design a better morning",
                    summary = "A practical guide to turning daily routines into measurable growth experiments.",
                    author = "LifeLab Editorial",
                    tag = "Focus",
                ),
                DiscoverContent.Course(
                    id = "course-focus-reset",
                    title = "Focus Reset Sprint",
                    summary = "A compact course for rebuilding attention blocks and protecting deep work time.",
                    instructor = "Maya Chen",
                    duration = "42 min",
                    tag = "Productivity",
                ),
                DiscoverContent.Offer.Product(
                    id = "product-habit-journal",
                    title = "Habit Experiment Journal",
                    summary = "A guided notebook for planning weekly experiments and reviewing progress.",
                    priceLabel = "${'$'}18",
                    tag = "Tools",
                ),
                DiscoverContent.Offer.Membership(
                    id = "membership-lifelab-plus",
                    title = "LifeLab Plus",
                    summary = "Monthly labs, expert sessions, and premium templates for personal systems.",
                    priceLabel = "${'$'}9/month",
                    tag = "Membership",
                ),
            )
    }
}
