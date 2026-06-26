package com.example.lifelab.feature.search.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchDao {
    @Query("SELECT * FROM search_history ORDER BY position ASC")
    fun observeHistory(): Flow<List<SearchHistoryEntity>>

    @Query("SELECT query FROM search_history ORDER BY position ASC")
    suspend fun getHistoryQueries(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: List<SearchHistoryEntity>)

    @Query("DELETE FROM search_history")
    suspend fun clearHistory()

    @Query("SELECT * FROM hot_keywords ORDER BY position ASC")
    fun observeHotKeywords(): Flow<List<HotKeywordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotKeywords(keywords: List<HotKeywordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchResults(results: List<SearchResultEntity>)

    @Query(
        """
        SELECT * FROM search_results
        WHERE (:type IS NULL OR type = :type)
            AND (title LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%')
        ORDER BY title ASC
        """,
    )
    suspend fun search(query: String, type: String?): List<SearchResultEntity>

    @Query("SELECT COUNT(*) FROM search_results")
    suspend fun countSearchResults(): Int
}
