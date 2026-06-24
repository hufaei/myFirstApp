package com.example.lifelab.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifelab.app.navigation.LifeLabRoutes
import com.example.lifelab.app.navigation.LifeLabNavHost
import com.example.lifelab.app.navigation.topLevelDestinations
import com.example.lifelab.core.ui.component.LifeLabBottomBar
import com.example.lifelab.core.ui.component.LifeLabBottomBarItem
import com.example.lifelab.core.ui.component.LifeLabTopBar
import com.example.lifelab.core.ui.theme.LifeLabTheme

@Composable
fun LifeLabApp() {
    LifeLabTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: topLevelDestinations.first().route

        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                LifeLabTopBar(
                    title = titleForRoute(currentRoute),
                    subtitle = subtitleForRoute(currentRoute),
                    showSearch = currentRoute != LifeLabRoutes.SEARCH,
                    showNotifications = currentRoute != LifeLabRoutes.NOTIFICATIONS,
                    onSearchClick = {
                        navController.navigate(LifeLabRoutes.SEARCH) {
                            launchSingleTop = true
                        }
                    },
                    onNotificationsClick = {
                        navController.navigate(LifeLabRoutes.NOTIFICATIONS) {
                            launchSingleTop = true
                        }
                    },
                )
            },
            bottomBar = {
                LifeLabBottomBar(
                    currentRoute = currentRoute,
                    items = topLevelDestinations.map { destination ->
                        LifeLabBottomBarItem(
                            route = destination.route,
                            title = destination.title,
                            iconRes = destination.iconRes,
                        )
                    },
                    onNavigate = { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        }
                    },
                )
            },
        ) { innerPadding ->
            LifeLabNavHost(
                navController = navController,
                contentPadding = innerPadding,
            )
        }
    }
}

private fun titleForRoute(route: String): String =
    topLevelDestinations.firstOrNull { it.route == route }?.title
        ?: when (route) {
            LifeLabRoutes.SEARCH -> "Search"
            LifeLabRoutes.NOTIFICATIONS -> "Notifications"
            else -> "LifeLab"
        }

private fun subtitleForRoute(route: String): String =
    when (route) {
        LifeLabRoutes.TODAY -> "Choose today's next move"
        LifeLabRoutes.WORKBENCH -> "Plan tasks and keep streaks alive"
        LifeLabRoutes.DISCOVER -> "Ideas for the next experiment"
        LifeLabRoutes.ME -> "Defaults, preferences, and identity"
        LifeLabRoutes.SEARCH -> "Find tasks, habits, and ideas"
        LifeLabRoutes.NOTIFICATIONS -> "Inbox and delivery controls"
        else -> "Your personal growth lab"
    }
