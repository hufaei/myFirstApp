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
                "专注",
                "每周计划",
                "习惯连续",
                "学习权益",
            ).mapIndexed { index, keyword ->
                HotKeywordEntity(keyword = keyword, position = index)
            }

        fun defaultSearchResults(): List<SearchResultEntity> =
            listOf(
                SearchResultItem(
                    id = "article-deep-focus",
                    title = "深度专注重启",
                    summary = "关于在高价值学习时段保护注意力的文章。",
                    type = SearchResultType.ARTICLE,
                ),
                SearchResultItem(
                    id = "offer-learning-sprint",
                    title = "学习冲刺权益",
                    summary = "一套引导式内容包，帮助你规划 7 天专注技能冲刺。",
                    type = SearchResultType.OFFER,
                ),
                SearchResultItem(
                    id = "task-weekly-plan",
                    title = "每周计划任务",
                    summary = "把目标拆成清晰计划，并设置复盘节点。",
                    type = SearchResultType.TASK,
                ),
                SearchResultItem(
                    id = "habit-evening-review",
                    title = "晚间复盘习惯",
                    summary = "通过回顾专注、任务和精力，建立连续习惯。",
                    type = SearchResultType.HABIT,
                ),
            ).mapIndexed { index, item -> item.toEntity(sortOrder = index) }
    }
}
