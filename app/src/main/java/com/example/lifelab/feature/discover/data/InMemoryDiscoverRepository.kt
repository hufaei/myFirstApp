package com.example.lifelab.feature.discover.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.domain.DiscoverContent
import com.example.lifelab.feature.discover.domain.DiscoverRepository

class InMemoryDiscoverRepository : DiscoverRepository {
    override fun getContent(): AppResult<List<DiscoverContent>> =
        AppResult.Success(seedContent)

    private companion object {
        val seedContent = listOf(
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
