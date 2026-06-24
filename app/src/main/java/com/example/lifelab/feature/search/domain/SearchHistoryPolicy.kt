package com.example.lifelab.feature.search.domain

object SearchHistoryPolicy {
    private const val MAX_HISTORY_SIZE = 8

    fun submit(
        query: String,
        currentHistory: List<String>,
    ): List<String> {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isEmpty()) {
            return currentHistory
        }

        val historyWithoutDuplicate = currentHistory.filterNot { historyItem ->
            historyItem.equals(trimmedQuery, ignoreCase = true)
        }

        return (listOf(trimmedQuery) + historyWithoutDuplicate).take(MAX_HISTORY_SIZE)
    }

    fun update(
        query: String,
        currentHistory: List<String>,
    ): List<String> = submit(query, currentHistory)
}
