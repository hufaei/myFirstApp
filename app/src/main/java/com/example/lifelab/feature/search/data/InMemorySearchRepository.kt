package com.example.lifelab.feature.search.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchHistoryPolicy
import com.example.lifelab.feature.search.domain.SearchRepository
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class InMemorySearchRepository : SearchRepository {
    private val _history = MutableStateFlow(emptyList<String>())
    override val history: Flow<List<String>> = _history

    private val _hotKeywords = MutableStateFlow(
        listOf(
            "focus",
            "weekly plan",
            "habit streak",
            "learning offer",
        ),
    )
    override val hotKeywords: Flow<List<String>> = _hotKeywords

    override suspend fun search(
        query: String,
        filter: SearchFilter,
    ): AppResult<List<SearchResultItem>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return AppResult.Success(emptyList())
        }

        val resultType = filter.toResultType()
        val matchingResults = seedResults.filter { item ->
            item.matches(trimmedQuery) && (resultType == null || item.type == resultType)
        }

        return AppResult.Success(matchingResults)
    }

    override suspend fun recordHistory(query: String) {
        _history.value = SearchHistoryPolicy.submit(
            query = query,
            currentHistory = _history.value,
        )
    }

    override suspend fun clearHistory() {
        _history.value = emptyList()
    }

    private fun SearchFilter.toResultType(): SearchResultType? =
        when (this) {
            SearchFilter.ALL -> null
            SearchFilter.ARTICLES -> SearchResultType.ARTICLE
            SearchFilter.OFFERS -> SearchResultType.OFFER
            SearchFilter.TASKS -> SearchResultType.TASK
            SearchFilter.HABITS -> SearchResultType.HABIT
        }

    private fun SearchResultItem.matches(query: String): Boolean =
        title.contains(query, ignoreCase = true) ||
            summary.contains(query, ignoreCase = true)

    private companion object {
        val seedResults = listOf(
            SearchResultItem(
                id = "article-deep-focus",
                title = "Deep Focus Reset",
                summary = "Article about protecting attention during high-value study blocks.",
                type = SearchResultType.ARTICLE,
            ),
            SearchResultItem(
                id = "offer-learning-sprint",
                title = "Learning Sprint Offer",
                summary = "A guided content pack for planning a focused seven-day skill sprint.",
                type = SearchResultType.OFFER,
            ),
            SearchResultItem(
                id = "task-weekly-plan",
                title = "Weekly Planning Task",
                summary = "Turn goals into a clear plan with review checkpoints.",
                type = SearchResultType.TASK,
            ),
            SearchResultItem(
                id = "habit-evening-review",
                title = "Evening Review Habit",
                summary = "Build a habit streak by reflecting on focus, tasks, and energy.",
                type = SearchResultType.HABIT,
            ),
        )
    }
}
