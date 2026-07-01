package com.example.lifelab.app.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.lifelab.R
import com.example.lifelab.feature.search.domain.SearchResultType

data class LifeLabDestination(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector,
)

object LifeLabRoutes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val TASKS_CREATE = "tasks/create"
    const val HABITS = "habits"
    const val DISCOVER = "discover"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val WEB_LAB = "web_lab"

    fun relatedRouteFor(type: SearchResultType): String =
        when (type) {
            SearchResultType.ARTICLE,
            SearchResultType.OFFER -> DISCOVER
            SearchResultType.TASK -> TASKS
            SearchResultType.HABIT -> HABITS
            SearchResultType.NOTIFICATION -> NOTIFICATIONS
        }
}

val topLevelDestinations = listOf(
    LifeLabDestination(
        route = LifeLabRoutes.HOME,
        titleRes = R.string.nav_home,
        icon = Icons.Filled.Home,
    ),
    LifeLabDestination(
        route = LifeLabRoutes.TASKS,
        titleRes = R.string.nav_tasks,
        icon = Icons.Filled.TaskAlt,
    ),
    LifeLabDestination(
        route = LifeLabRoutes.HABITS,
        titleRes = R.string.nav_habits,
        icon = Icons.Filled.CheckCircle,
    ),
    LifeLabDestination(
        route = LifeLabRoutes.PROFILE,
        titleRes = R.string.nav_profile,
        icon = Icons.Filled.Person,
    ),
)
