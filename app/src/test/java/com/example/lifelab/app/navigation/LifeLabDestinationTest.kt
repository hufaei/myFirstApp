package com.example.lifelab.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LifeLabDestinationTest {

    @Test
    fun topLevelDestinationsUseExpectedOrder() {
        assertEquals(
            listOf(
                "today",
                "workbench",
                "discover",
                "me",
            ),
            topLevelDestinations.map { it.route },
        )

        assertEquals(
            listOf(
                "Today",
                "Workbench",
                "Discover",
                "Me",
            ),
            topLevelDestinations.map { it.title },
        )
    }
}
