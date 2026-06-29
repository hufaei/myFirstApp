package com.example.lifelab.feature.search.domain

enum class SearchFilter {
    ALL,
    ARTICLES,
    OFFERS,
    TASKS,
    HABITS,
    NOTIFICATIONS,
}

enum class SearchResultType {
    ARTICLE,
    OFFER,
    TASK,
    HABIT,
    NOTIFICATION,
}

data class SearchResultItem(
    val id: String,
    val title: String,
    val summary: String,
    val type: SearchResultType,
)
