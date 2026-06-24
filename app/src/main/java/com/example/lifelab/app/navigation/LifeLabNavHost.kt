package com.example.lifelab.app.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lifelab.app.workbench.WorkbenchRoute
import com.example.lifelab.feature.discover.presentation.DiscoverRoute
import com.example.lifelab.feature.home.presentation.HomeRoute
import com.example.lifelab.feature.notifications.presentation.NotificationsRoute
import com.example.lifelab.feature.profile.presentation.ProfileRoute
import com.example.lifelab.feature.search.presentation.SearchRoute

@Composable
fun LifeLabNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues,
) {
    NavHost(
        navController = navController,
        startDestination = LifeLabRoutes.TODAY,
    ) {
        composable(LifeLabRoutes.TODAY) { HomeRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.WORKBENCH) { WorkbenchRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.DISCOVER) { DiscoverRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.SEARCH) { SearchRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.NOTIFICATIONS) { NotificationsRoute(contentPadding = contentPadding) }
        composable(LifeLabRoutes.ME) { ProfileRoute(contentPadding = contentPadding) }
    }
}
