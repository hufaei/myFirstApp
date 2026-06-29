package com.example.lifelab.feature.search.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.discover.data.DiscoverContentEntity
import com.example.lifelab.feature.discover.data.DiscoverContentType
import com.example.lifelab.feature.discover.data.DiscoverDao
import com.example.lifelab.feature.habits.data.HabitDao
import com.example.lifelab.feature.habits.data.HabitEntity
import com.example.lifelab.feature.notifications.data.NotificationDao
import com.example.lifelab.feature.notifications.data.NotificationMessageEntity
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchHistoryPolicy
import com.example.lifelab.feature.search.domain.SearchRepository
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import com.example.lifelab.feature.tasks.data.TaskDao
import com.example.lifelab.feature.tasks.data.TaskEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class RoomSearchRepository(
    private val searchDao: SearchDao,
    private val taskDao: TaskDao? = null,
    private val habitDao: HabitDao? = null,
    private val notificationDao: NotificationDao? = null,
    private val discoverDao: DiscoverDao? = null,
    private val seedLocalData: (suspend () -> Unit)? = null,
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

        seedLocalData?.invoke()
        val results = searchLocalData(trimmedQuery, filter).ifEmpty {
            searchDao.search(
                query = trimmedQuery,
                type = filter.toResultType()?.name,
            ).map(SearchResultEntity::toDomain)
        }
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
            SearchFilter.NOTIFICATIONS -> SearchResultType.NOTIFICATION
        }

    private suspend fun searchLocalData(
        query: String,
        filter: SearchFilter,
    ): List<SearchResultItem> {
        val normalizedQuery = query.normalizedSearchText()
        return buildList {
            if (filter == SearchFilter.ALL || filter == SearchFilter.TASKS) {
                taskDao?.getTasks()?.map(TaskEntity::toSearchResult)?.filterMatches(normalizedQuery)?.let(::addAll)
            }
            if (filter == SearchFilter.ALL || filter == SearchFilter.HABITS) {
                habitDao?.getHabits()?.map(HabitEntity::toSearchResult)?.filterMatches(normalizedQuery)?.let(::addAll)
            }
            if (filter == SearchFilter.ALL || filter == SearchFilter.NOTIFICATIONS) {
                notificationDao?.getMessages()
                    ?.map(NotificationMessageEntity::toSearchResult)
                    ?.filterMatches(normalizedQuery)
                    ?.let(::addAll)
            }
            if (filter == SearchFilter.ALL || filter == SearchFilter.ARTICLES || filter == SearchFilter.OFFERS) {
                discoverDao?.getContent()
                    ?.mapNotNull { entity -> entity.toSearchResultOrNull(filter) }
                    ?.filterMatches(normalizedQuery)
                    ?.let(::addAll)
            }
        }.sortedWith(
            compareByDescending<SearchResultItem> { item -> item.title.normalizedSearchText().startsWith(normalizedQuery) }
                .thenBy { item -> item.type.sortWeight }
                .thenBy { item -> item.title },
        )
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

private fun TaskEntity.toSearchResult(): SearchResultItem =
    SearchResultItem(
        id = id,
        title = title,
        summary = listOf(description, tags.replace("\u001F", " "))
            .filter { value -> value.isNotBlank() }
            .joinToString(" · "),
        type = SearchResultType.TASK,
    )

private fun HabitEntity.toSearchResult(): SearchResultItem =
    SearchResultItem(
        id = id,
        title = name,
        summary = if (reminderEnabled) {
            reminderTimeSecondOfDay.toTimeLabel()
        } else {
            ""
        },
        type = SearchResultType.HABIT,
    )

private fun NotificationMessageEntity.toSearchResult(): SearchResultItem =
    SearchResultItem(
        id = id,
        title = title,
        summary = "$category · $body",
        type = SearchResultType.NOTIFICATION,
    )

private fun DiscoverContentEntity.toSearchResultOrNull(filter: SearchFilter): SearchResultItem? {
    val contentType = DiscoverContentType.valueOf(type)
    val resultType = when (contentType) {
        DiscoverContentType.Article,
        DiscoverContentType.Course -> SearchResultType.ARTICLE
        DiscoverContentType.Product,
        DiscoverContentType.Membership -> SearchResultType.OFFER
    }
    if (filter == SearchFilter.ARTICLES && resultType != SearchResultType.ARTICLE) return null
    if (filter == SearchFilter.OFFERS && resultType != SearchResultType.OFFER) return null
    return SearchResultItem(
        id = id,
        title = title,
        summary = listOf(summary, tag, author, instructor, duration, priceLabel)
            .filterNotNull()
            .filter { value -> value.isNotBlank() }
            .joinToString(" · "),
        type = resultType,
    )
}

private fun List<SearchResultItem>.filterMatches(normalizedQuery: String): List<SearchResultItem> =
    filter { item ->
        item.title.normalizedSearchText().contains(normalizedQuery) ||
            item.summary.normalizedSearchText().contains(normalizedQuery)
    }

private fun String.normalizedSearchText(): String =
    lowercase().filterNot(Char::isWhitespace)

private fun Int?.toTimeLabel(): String =
    this?.let { secondOfDay ->
        val hour = secondOfDay / 3600
        val minute = secondOfDay % 3600 / 60
        "%02d:%02d".format(hour, minute)
    }.orEmpty()

private val SearchResultType.sortWeight: Int
    get() = when (this) {
        SearchResultType.TASK -> 0
        SearchResultType.HABIT -> 1
        SearchResultType.NOTIFICATION -> 2
        SearchResultType.ARTICLE -> 3
        SearchResultType.OFFER -> 4
    }
