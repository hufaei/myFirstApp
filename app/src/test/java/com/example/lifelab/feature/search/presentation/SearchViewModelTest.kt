package com.example.lifelab.feature.search.presentation

import com.example.lifelab.core.common.AppError
import com.example.lifelab.core.common.AppResult
import com.example.lifelab.core.testing.MainDispatcherRule
import com.example.lifelab.feature.search.domain.SearchFilter
import com.example.lifelab.feature.search.domain.SearchRepository
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun initialStateReflectsRepositoryHotKeywordsAndHistory() = runTest {
        val repository = FakeSearchRepository(
            history = listOf("morning routine", "meal prep"),
            hotKeywords = listOf("focus", "fitness"),
        )

        val viewModel = SearchViewModel(repository)

        assertEquals(listOf("morning routine", "meal prep"), viewModel.uiState.value.history)
        assertEquals(listOf("focus", "fitness"), viewModel.uiState.value.hotKeywords)
        assertEquals(SearchResultContent.Idle, viewModel.uiState.value.resultContent)
    }

    @Test
    fun queryChangeUpdatesInput() = runTest {
        val viewModel = SearchViewModel(FakeSearchRepository())

        viewModel.onQueryChanged("deep work")

        assertEquals("deep work", viewModel.uiState.value.query)
    }

    @Test
    fun submitTrimsQueryRecordsHistoryAndRendersContentResults() = runTest {
        val result = searchResult(id = "article-1", title = "Deep Work")
        val repository = FakeSearchRepository(searchResults = mapOf("deep work" to listOf(result)))
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("  deep work  ")
        viewModel.submitCurrentQuery()

        val content = assertIs<SearchResultContent.Content>(viewModel.uiState.value.resultContent)
        assertEquals(listOf(result), content.items)
        assertEquals("deep work", viewModel.uiState.value.lastSubmittedQuery)
        assertEquals(listOf("deep work"), repository.recordedHistory)
        assertEquals(listOf(SearchCall("deep work", SearchFilter.ALL)), repository.searchCalls)
    }

    @Test
    fun selectingHistoryQuerySubmitsThatQuery() = runTest {
        val result = searchResult(id = "article-1", title = "Deep Work")
        val repository = FakeSearchRepository(
            history = listOf("deep work"),
            searchResults = mapOf("deep work" to listOf(result)),
        )
        val viewModel = SearchViewModel(repository)

        viewModel.selectHistoryQuery("deep work")

        val content = assertIs<SearchResultContent.Content>(viewModel.uiState.value.resultContent)
        assertEquals(listOf(result), content.items)
        assertEquals("deep work", viewModel.uiState.value.query)
        assertEquals("deep work", viewModel.uiState.value.lastSubmittedQuery)
        assertEquals(listOf("deep work"), repository.recordedHistory)
        assertEquals(listOf(SearchCall("deep work", SearchFilter.ALL)), repository.searchCalls)
    }

    @Test
    fun blankSubmitDoesNotRecordHistoryOrSearch() = runTest {
        val repository = FakeSearchRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.onQueryChanged("   ")
        viewModel.submitCurrentQuery()

        assertEquals(SearchResultContent.Idle, viewModel.uiState.value.resultContent)
        assertEquals(emptyList(), repository.recordedHistory)
        assertEquals(emptyList(), repository.searchCalls)
    }

    @Test
    fun emptySuccessfulResultRendersEmptyState() = runTest {
        val viewModel = SearchViewModel(FakeSearchRepository(searchResults = mapOf("unknown" to emptyList())))

        viewModel.submitQuery("unknown")

        assertEquals(SearchResultContent.Empty, viewModel.uiState.value.resultContent)
    }

    @Test
    fun repositoryFailureRendersErrorStateWithMessage() = runTest {
        val repository = FakeSearchRepository(
            failures = mapOf("offline" to "Search is unavailable"),
        )
        val viewModel = SearchViewModel(repository)

        viewModel.submitQuery("offline")

        val error = assertIs<SearchResultContent.Error>(viewModel.uiState.value.resultContent)
        assertEquals("Search is unavailable", error.message)
    }

    @Test
    fun selectingFilterRerunsLastSubmittedQueryWithSelectedFilter() = runTest {
        val repository = FakeSearchRepository(
            searchResults = mapOf("habits" to listOf(searchResult(type = SearchResultType.HABIT))),
        )
        val viewModel = SearchViewModel(repository)

        viewModel.submitQuery("habits")
        viewModel.selectFilter(SearchFilter.HABITS)

        assertEquals(SearchFilter.HABITS, viewModel.uiState.value.selectedFilter)
        assertEquals(
            listOf(
                SearchCall("habits", SearchFilter.ALL),
                SearchCall("habits", SearchFilter.HABITS),
            ),
            repository.searchCalls,
        )
    }

    @Test
    fun selectingHotKeywordSubmitsThatKeyword() = runTest {
        val repository = FakeSearchRepository(
            hotKeywords = listOf("focus"),
            searchResults = mapOf("focus" to listOf(searchResult(title = "Focus Basics"))),
        )
        val viewModel = SearchViewModel(repository)

        viewModel.selectHotKeyword("focus")

        val content = assertIs<SearchResultContent.Content>(viewModel.uiState.value.resultContent)
        assertEquals("focus", viewModel.uiState.value.query)
        assertEquals("focus", viewModel.uiState.value.lastSubmittedQuery)
        assertEquals("Focus Basics", content.items.single().title)
    }

    @Test
    fun clearHistoryClearsRepositoryHistory() = runTest {
        val repository = FakeSearchRepository(history = listOf("focus", "sleep"))
        val viewModel = SearchViewModel(repository)

        viewModel.clearHistory()

        assertEquals(emptyList(), viewModel.uiState.value.history)
        assertEquals(1, repository.clearHistoryCalls)
    }

    @Test
    fun retryRerunsLastSubmittedQuery() = runTest {
        val repository = FakeSearchRepository(
            searchResults = mapOf("focus" to listOf(searchResult(title = "Focus Basics"))),
        )
        val viewModel = SearchViewModel(repository)

        viewModel.submitQuery("focus")
        viewModel.retry()

        assertEquals(
            listOf(
                SearchCall("focus", SearchFilter.ALL),
                SearchCall("focus", SearchFilter.ALL),
            ),
            repository.searchCalls,
        )
        assertIs<SearchResultContent.Content>(viewModel.uiState.value.resultContent)
    }

    @Test
    fun slowerPreviousSearchDoesNotOverwriteNewerResult() = runTest {
        val repository = DeferredSearchRepository()
        val viewModel = SearchViewModel(repository)

        viewModel.submitQuery("first")
        viewModel.submitQuery("second")
        repository.complete(
            query = "second",
            results = listOf(searchResult(id = "second", title = "Second Result")),
        )
        repository.complete(
            query = "first",
            results = listOf(searchResult(id = "first", title = "First Result")),
        )

        val content = assertIs<SearchResultContent.Content>(viewModel.uiState.value.resultContent)
        assertEquals("second", content.items.single().id)
        assertEquals("second", viewModel.uiState.value.lastSubmittedQuery)
    }

    private data class SearchCall(
        val query: String,
        val filter: SearchFilter,
    )

    private class FakeSearchRepository(
        history: List<String> = emptyList(),
        hotKeywords: List<String> = emptyList(),
        private val searchResults: Map<String, List<SearchResultItem>> = emptyMap(),
        private val failures: Map<String, String> = emptyMap(),
    ) : SearchRepository {

        private val historyFlow = MutableStateFlow(history)
        private val hotKeywordsFlow = MutableStateFlow(hotKeywords)

        val recordedHistory = mutableListOf<String>()
        val searchCalls = mutableListOf<SearchCall>()
        var clearHistoryCalls = 0

        override val history: Flow<List<String>> = historyFlow
        override val hotKeywords: Flow<List<String>> = hotKeywordsFlow

        override suspend fun search(
            query: String,
            filter: SearchFilter,
        ): AppResult<List<SearchResultItem>> {
            searchCalls += SearchCall(query, filter)
            failures[query]?.let { message ->
                return AppResult.Failure(AppError.Unknown(message = message))
            }
            return AppResult.Success(searchResults[query].orEmpty())
        }

        override suspend fun recordHistory(query: String) {
            recordedHistory += query
            historyFlow.value = listOf(query) + historyFlow.value.filterNot { it.equals(query, ignoreCase = true) }
        }

        override suspend fun clearHistory() {
            clearHistoryCalls += 1
            historyFlow.value = emptyList()
        }
    }

    private class DeferredSearchRepository : SearchRepository {
        private val resultsByQuery = mutableMapOf<String, CompletableDeferred<List<SearchResultItem>>>()

        override val history: Flow<List<String>> = MutableStateFlow(emptyList())
        override val hotKeywords: Flow<List<String>> = MutableStateFlow(emptyList())

        override suspend fun search(
            query: String,
            filter: SearchFilter,
        ): AppResult<List<SearchResultItem>> {
            val deferred = resultsByQuery.getOrPut(query) { CompletableDeferred() }
            return AppResult.Success(deferred.await())
        }

        override suspend fun recordHistory(query: String) = Unit

        override suspend fun clearHistory() = Unit

        fun complete(
            query: String,
            results: List<SearchResultItem>,
        ) {
            resultsByQuery.getOrPut(query) { CompletableDeferred() }.complete(results)
        }
    }

    private fun searchResult(
        id: String = "result-1",
        title: String = "Result",
        type: SearchResultType = SearchResultType.ARTICLE,
    ) = SearchResultItem(
        id = id,
        title = title,
        summary = "Useful summary",
        type = type,
    )
}
