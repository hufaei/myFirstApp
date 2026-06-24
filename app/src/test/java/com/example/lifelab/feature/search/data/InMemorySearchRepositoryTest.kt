package com.example.lifelab.feature.search.data

import com.example.lifelab.core.common.AppResult
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySearchRepositoryTest {
    @Test
    fun `hot keywords are exposed as a non-empty list`() = runTest {
        val repository = InMemorySearchRepository()

        val hotKeywords = repository.hotKeywords.first()

        assertTrue(hotKeywords.isNotEmpty())
    }

    @Test
    fun `recordHistory applies SearchHistoryPolicy rules through history Flow`() = runTest {
        val repository = InMemorySearchRepository()

        repository.recordHistory("  focus  ")
        repository.recordHistory("routine")
        repository.recordHistory("FOCUS")
        repository.recordHistory("   ")

        assertEquals(listOf("FOCUS", "routine"), repository.history.first())
    }

    @Test
    fun `search returns matching content for a query`() = runTest {
        val repository = InMemorySearchRepository()

        val results = repository.search("focus").successValue()

        assertTrue(results.any { it.title.contains("focus", ignoreCase = true) })
    }

    @Test
    fun `selected filter restricts result types`() = runTest {
        val repository = InMemorySearchRepository()

        val results = repository.search(
            query = "plan",
            filter = SearchFilter.TASKS,
        ).successValue()

        assertTrue(results.isNotEmpty())
        assertTrue(results.all { it.type == SearchResultType.TASK })
    }

    @Test
    fun `unmatched query returns successful empty list`() = runTest {
        val repository = InMemorySearchRepository()

        val results = repository.search("no matching search term").successValue()

        assertEquals(emptyList<SearchResultItem>(), results)
    }

    private fun AppResult<List<SearchResultItem>>.successValue(): List<SearchResultItem> {
        return when (this) {
            is AppResult.Success -> value
            is AppResult.Failure -> error("Expected successful search result.")
        }
    }
}
