package com.example.lifelab.feature.search.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RoomSearchRepositoryTest {

    @Test
    fun recordHistoryPersistsPolicyOrderedQueries() = runTest {
        val dao = FakeSearchDao()
        val repository = RoomSearchRepository(dao)

        repository.recordHistory(" focus ")
        repository.recordHistory("routine")
        repository.recordHistory("FOCUS")

        assertEquals(listOf("FOCUS", "routine"), repository.history.first())
    }

    @Test
    fun searchDelegatesTrimmedQueryAndFilterToDao() = runTest {
        val dao = FakeSearchDao(
            results = listOf(
                SearchResultEntity(
                    id = "task-weekly-plan",
                    title = "Weekly Planning Task",
                    summary = "Turn goals into a clear plan.",
                    type = SearchResultType.TASK.name,
                ),
            ),
        )
        val repository = RoomSearchRepository(dao)

        val result = repository.search(query = " plan ", filter = SearchFilter.TASKS)

        val items = assertIs<AppResult.Success<List<SearchResultItem>>>(result).value
        assertEquals(1, items.size)
        assertEquals("plan", dao.lastQuery)
        assertEquals(SearchResultType.TASK.name, dao.lastType)
    }

    private class FakeSearchDao(
        private val results: List<SearchResultEntity> = emptyList(),
    ) : SearchDao {
        private val historyEntities = MutableStateFlow(emptyList<SearchHistoryEntity>())
        private val hotKeywordEntities = MutableStateFlow(emptyList<HotKeywordEntity>())
        var lastQuery: String? = null
            private set
        var lastType: String? = null
            private set

        override fun observeHistory(): Flow<List<SearchHistoryEntity>> = historyEntities

        override suspend fun getHistoryQueries(): List<String> =
            historyEntities.value.sortedBy { it.position }.map { it.query }

        override suspend fun insertHistory(history: List<SearchHistoryEntity>) {
            historyEntities.value = history
        }

        override suspend fun clearHistory() {
            historyEntities.value = emptyList()
        }

        override fun observeHotKeywords(): Flow<List<HotKeywordEntity>> = hotKeywordEntities

        override suspend fun insertHotKeywords(keywords: List<HotKeywordEntity>) {
            hotKeywordEntities.value = keywords
        }

        override suspend fun insertSearchResults(results: List<SearchResultEntity>) = Unit

        override suspend fun search(query: String, type: String?): List<SearchResultEntity> {
            lastQuery = query
            lastType = type
            return results.filter { result ->
                result.title.contains(query, ignoreCase = true) &&
                    (type == null || result.type == type)
            }
        }

        override suspend fun countSearchResults(): Int = results.size
    }
}
