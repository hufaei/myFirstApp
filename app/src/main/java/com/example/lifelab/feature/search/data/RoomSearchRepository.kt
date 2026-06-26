package com.example.lifelab.feature.search.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchHistoryPolicy
import com.example.lifelab.feature.search.domain.SearchRepository
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomSearchRepository(
    private val searchDao: SearchDao,
) : SearchRepository {
    override val history: Flow<List<String>> =
        searchDao.observeHistory()
            .onStart { seedIfEmpty() }
            .map { history ->
                history.sortedBy(SearchHistoryEntity::position).map(SearchHistoryEntity::query)
            }

    override val hotKeywords: Flow<List<String>> =
        searchDao.observeHotKeywords()
            .onStart { seedIfEmpty() }
            .map { keywords ->
                keywords.sortedBy(HotKeywordEntity::position).map(HotKeywordEntity::keyword)
            }

    override suspend fun search(
        query: String,
        filter: SearchFilter,
    ): AppResult<List<SearchResultItem>> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return AppResult.Success(emptyList())
        }

        val results = searchDao.search(
            query = trimmedQuery,
            type = filter.toResultType()?.name,
        ).map(SearchResultEntity::toDomain)
        return AppResult.Success(results)
    }

    override suspend fun recordHistory(query: String) {
        val updatedHistory = SearchHistoryPolicy.submit(
            query = query,
            currentHistory = searchDao.getHistoryQueries(),
        )
        searchDao.clearHistory()
        searchDao.insertHistory(
            updatedHistory.mapIndexed { index, item ->
                SearchHistoryEntity(query = item, position = index)
            },
        )
    }

    override suspend fun clearHistory() {
        searchDao.clearHistory()
    }

    suspend fun seedIfEmpty() {
        if (searchDao.countSearchResults() == 0) {
            searchDao.insertHotKeywords(defaultHotKeywords())
            searchDao.insertSearchResults(defaultSearchResults())
        }
    }

    private fun SearchFilter.toResultType(): SearchResultType? =
        when (this) {
            SearchFilter.ALL -> null
            SearchFilter.ARTICLES -> SearchResultType.ARTICLE
            SearchFilter.OFFERS -> SearchResultType.OFFER
            SearchFilter.TASKS -> SearchResultType.TASK
            SearchFilter.HABITS -> SearchResultType.HABIT
        }

    private companion object {
        fun defaultHotKeywords(): List<HotKeywordEntity> =
            listOf(
                "focus",
                "weekly plan",
                "habit streak",
                "learning offer",
            ).mapIndexed { index, keyword ->
                HotKeywordEntity(keyword = keyword, position = index)
            }

        fun defaultSearchResults(): List<SearchResultEntity> =
            listOf(
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
            ).mapIndexed { index, item -> item.toEntity(sortOrder = index) }
    }
}
