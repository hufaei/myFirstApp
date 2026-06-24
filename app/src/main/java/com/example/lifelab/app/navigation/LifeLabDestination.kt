package com.example.lifelab.app.navigation

import androidx.annotation.DrawableRes
import com.example.lifelab.R

data class LifeLabDestination(
    val route: String,
    val title: String,
    @DrawableRes val iconRes: Int,
)

object LifeLabRoutes {
    const val TODAY = "today"
    const val WORKBENCH = "workbench"
    const val DISCOVER = "discover"
    const val SEARCH = "search"
    const val NOTIFICATIONS = "notifications"
    const val ME = "me"
}

val topLevelDestinations = listOf(
    LifeLabDestination(
        route = LifeLabRoutes.TODAY,
        title = "Today",
        iconRes = R.drawable.ic_today,
    ),
    LifeLabDestination(
        route = LifeLabRoutes.WORKBENCH,
        title = "Workbench",
        iconRes = R.drawable.ic_workbench,
    ),
    LifeLabDestination(
        route = LifeLabRoutes.DISCOVER,
        title = "Discover",
        iconRes = R.drawable.ic_discover,
    ),
    LifeLabDestination(
        route = LifeLabRoutes.ME,
        title = "Me",
        iconRes = R.drawable.ic_me,
    ),
)
