package com.example.lifelab.feature.search.domain

import com.example.lifelab.core.common.AppResult
import kotlinx.coroutines.flow.Flow

interface SearchRepository {
    val history: Flow<List<String>>

    val hotKeywords: Flow<List<String>>

    suspend fun search(
        query: String,
        filter: SearchFilter = SearchFilter.ALL,
    ): AppResult<List<SearchResultItem>>

    suspend fun recordHistory(query: String)

    suspend fun clearHistory()
}
