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
                "profile",
            ),
            routes,
        )
    }

    @Test
    fun taskCreateRouteStaysOutsideTopLevelDestinations() {
        assertEquals("tasks/create", LifeLabRoutes.TASKS_CREATE)
        assertEquals(false, topLevelDestinations.any { it.route == LifeLabRoutes.TASKS_CREATE })
    }
}
