package com.example.lifelab.app.navigation

data class LifeLabDestination(
    val route: String,
    val title: String,
)

object LifeLabRoutes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val HABITS = "habits"
    const val DISCOVER = "discover"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
}

val topLevelDestinations = listOf(
    LifeLabDestination(route = LifeLabRoutes.HOME, title = "Home"),
    LifeLabDestination(route = LifeLabRoutes.TASKS, title = "Tasks"),
    LifeLabDestination(route = LifeLabRoutes.HABITS, title = "Habits"),
    LifeLabDestination(route = LifeLabRoutes.DISCOVER, title = "Discover"),
    LifeLabDestination(route = LifeLabRoutes.SEARCH, title = "Search"),
    LifeLabDestination(route = LifeLabRoutes.NOTIFICATIONS, title = "Notifications"),
    LifeLabDestination(route = LifeLabRoutes.PROFILE, title = "Profile"),
)
