package com.example.lifelab.feature.search.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class SearchHistoryPolicyTest {
    @Test
    fun `blank query is ignored`() {
        val currentHistory = listOf("focus", "routine")

        val updatedHistory = SearchHistoryPolicy.submit("   ", currentHistory)

        assertEquals(currentHistory, updatedHistory)
    }

    @Test
    fun `submitted query is trimmed before storage`() {
        val updatedHistory = SearchHistoryPolicy.submit("  focus plan  ", emptyList())

        assertEquals(listOf("focus plan"), updatedHistory)
    }

    @Test
    fun `case-insensitive duplicate is promoted to front`() {
        val currentHistory = listOf("focus", "Routine", "offers")

        val updatedHistory = SearchHistoryPolicy.submit("routine", currentHistory)

        assertEquals(listOf("routine", "focus", "offers"), updatedHistory)
    }

    @Test
    fun `history keeps latest eight entries`() {
        val currentHistory = listOf(
            "one",
            "two",
            "three",
            "four",
            "five",
            "six",
            "seven",
            "eight",
        )

        val updatedHistory = SearchHistoryPolicy.submit("nine", currentHistory)

        assertEquals(
            listOf("nine", "one", "two", "three", "four", "five", "six", "seven"),
            updatedHistory,
        )
    }
}
