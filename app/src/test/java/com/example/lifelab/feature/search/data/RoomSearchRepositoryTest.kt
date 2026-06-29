package com.example.lifelab.feature.search.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import com.example.lifelab.feature.tasks.data.TaskDao
import com.example.lifelab.feature.tasks.data.TaskEntity
import com.example.lifelab.feature.tasks.domain.TaskPriority
import com.example.lifelab.feature.tasks.domain.TaskStatus
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

    @Test
    fun searchUsesRealLocalTasksBeforeSeededCache() = runTest {
        val dao = FakeSearchDao(
            results = listOf(
                SearchResultEntity(
                    id = "cached-plan",
                    title = "Cached plan",
                    summary = "Old cached result",
                    type = SearchResultType.TASK.name,
                ),
            ),
        )
        val taskDao = FakeTaskDao(
            tasks = listOf(
                TaskEntity(
                    id = "task-real",
                    title = "家庭复盘",
                    description = "整理本周任务和健康记录",
                    status = TaskStatus.Active.name,
                    priority = TaskPriority.High.name,
                    dueAtEpochMillis = null,
                    tags = "计划",
                    createdAtEpochMillis = 1L,
                    updatedAtEpochMillis = 1L,
                ),
            ),
        )
        val repository = RoomSearchRepository(
            searchDao = dao,
            taskDao = taskDao,
        )

        val result = repository.search(query = " 健康 ", filter = SearchFilter.TASKS)

        val items = assertIs<AppResult.Success<List<SearchResultItem>>>(result).value
        assertEquals(listOf("task-real"), items.map { it.id })
        assertEquals(null, dao.lastQuery)
    }

    @Test
    fun searchSeedsLocalDataBeforeReadingBusinessTables() = runTest {
        val dao = FakeSearchDao()
        val taskDao = FakeTaskDao(tasks = emptyList())
        val repository = RoomSearchRepository(
            searchDao = dao,
            taskDao = taskDao,
            seedLocalData = {
                taskDao.upsertTask(
                    TaskEntity(
                        id = "task-seeded",
                        title = "预约健康检查",
                        description = "确认可预约时间和所需材料",
                        status = TaskStatus.Active.name,
                        priority = TaskPriority.Medium.name,
                        dueAtEpochMillis = null,
                        tags = "健康",
                        createdAtEpochMillis = 1L,
                        updatedAtEpochMillis = 1L,
                    ),
                )
            },
        )

        val result = repository.search(query = " 健康 ", filter = SearchFilter.TASKS)

        val items = assertIs<AppResult.Success<List<SearchResultItem>>>(result).value
        assertEquals(listOf("task-seeded"), items.map { it.id })
        assertEquals(null, dao.lastQuery)
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

    private class FakeTaskDao(
        tasks: List<TaskEntity>,
    ) : TaskDao {
        private val taskEntities = MutableStateFlow(tasks)

        override fun observeTasks(): Flow<List<TaskEntity>> = taskEntities

        override suspend fun getTasks(): List<TaskEntity> = taskEntities.value

        override suspend fun getTask(id: String): TaskEntity? =
            taskEntities.value.firstOrNull { it.id == id }

        override suspend fun upsertTask(task: TaskEntity) {
            taskEntities.value = taskEntities.value.filterNot { it.id == task.id } + task
        }

        override suspend fun countTasks(): Int = taskEntities.value.size
    }
}
