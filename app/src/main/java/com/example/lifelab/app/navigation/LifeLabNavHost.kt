package com.example.lifelab.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lifelab.feature.discover.presentation.DiscoverRoute
import com.example.lifelab.feature.habits.presentation.HabitsRoute
import com.example.lifelab.feature.home.presentation.HomeRoute
import com.example.lifelab.feature.notifications.presentation.NotificationsRoute
import com.example.lifelab.feature.profile.presentation.ProfileRoute
import com.example.lifelab.feature.search.presentation.SearchRoute
import com.example.lifelab.feature.tasks.presentation.TasksRoute
import com.example.lifelab.feature.weblab.presentation.WebLabRoute

@Composable
fun LifeLabNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = LifeLabRoutes.HOME,
    ) {
        composable(LifeLabRoutes.HOME) {
            HomeRoute(
                contentPadding = contentPadding,
                onOpenRoute = { route -> navController.navigateSingleTop(route) },
                onOpenSearch = { navController.navigateSingleTop(LifeLabRoutes.SEARCH) },
                onOpenNotifications = {
                    navController.navigateSingleTop(LifeLabRoutes.NOTIFICATIONS)
                },
                onCreateTask = { navController.navigateSingleTop(LifeLabRoutes.TASKS_CREATE) },
                onOpenTasks = { navController.navigateSingleTop(LifeLabRoutes.TASKS) },
                onOpenHabits = { navController.navigateSingleTop(LifeLabRoutes.HABITS) },
                onOpenDiscover = { navController.navigateSingleTop(LifeLabRoutes.DISCOVER) },
            )
        }
        composable(LifeLabRoutes.TASKS) { TasksRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.TASKS_CREATE) {
            TasksRoute(
                contentPadding = contentPadding,
                startInCreateMode = true,
            )
        }
        composable(LifeLabRoutes.HABITS) { HabitsRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.DISCOVER) {
            DiscoverRoute(
                contentPadding = contentPadding,
                onBack = { navController.popBackStack() },
            )
        }
        composable(LifeLabRoutes.SEARCH) {
            SearchRoute(
                contentPadding = contentPadding,
                onBack = { navController.popBackStack() },
                onOpenResultDestination = { type ->
                    navController.navigateSingleTop(LifeLabRoutes.relatedRouteFor(type))
                },
            )
        }
        composable(LifeLabRoutes.NOTIFICATIONS) {
            NotificationsRoute(
                contentPadding = contentPadding,
                onBack = { navController.popBackStack() },
            )
        }
        composable(LifeLabRoutes.PROFILE) {
            ProfileRoute(
                contentPadding = contentPadding,
                onOpenNotifications = {
                    navController.navigateSingleTop(LifeLabRoutes.NOTIFICATIONS)
                },
                onOpenWebLab = { navController.navigate(LifeLabRoutes.WEB_LAB) },
            )
        }
        composable(LifeLabRoutes.WEB_LAB) {
            WebLabRoute(
                contentPadding = contentPadding,
                onClose = { navController.popBackStack() },
            )
        }
    }
}

private fun NavHostController.navigateSingleTop(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
