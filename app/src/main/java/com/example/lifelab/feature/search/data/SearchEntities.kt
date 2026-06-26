package com.example.lifelab.feature.search.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.lifelab.feature.search.domain.SearchResultItem
import com.example.lifelab.feature.search.domain.SearchResultType

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    val position: Int,
)

@Entity(tableName = "hot_keywords")
data class HotKeywordEntity(
    @PrimaryKey val keyword: String,
    val position: Int,
)

@Entity(tableName = "search_results")
data class SearchResultEntity(
    @PrimaryKey val id: String,
    val title: String,
    val summary: String,
    val type: String,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
)

fun SearchResultEntity.toDomain(): SearchResultItem =
    SearchResultItem(
        id = id,
        title = title,
        summary = summary,
        type = SearchResultType.valueOf(type),
    )

fun SearchResultItem.toEntity(sortOrder: Int): SearchResultEntity =
    SearchResultEntity(
        id = id,
        title = title,
        summary = summary,
        type = type.name,
        sortOrder = sortOrder,
    )
