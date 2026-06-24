package com.example.lifelab.app.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class LifeLabDestinationTest {

    @Test
    fun topLevelDestinationsUseExpectedOrder() {
        val routes = topLevelDestinations.map { it.route }

        assertEquals(
            listOf(
                "home",
                "tasks",
                "habits",
                "discover",
                "search",
                "notifications",
                "profile",
            ),
            routes,
        )
    }
}
