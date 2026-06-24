package com.example.lifelab.app

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifelab.app.navigation.LifeLabNavHost
import com.example.lifelab.app.navigation.topLevelDestinations
import com.example.lifelab.core.ui.theme.LifeLabTheme

@Composable
fun LifeLabApp() {
    LifeLabTheme {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: topLevelDestinations.first().route

        Scaffold(
            bottomBar = {
                NavigationBar {
                    topLevelDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                }
                            },
                            label = { Text(destination.title) },
                            icon = { Text(destination.title.take(1)) },
                        )
                    }
                }
            },
        ) { innerPadding ->
            LifeLabNavHost(
                navController = navController,
                contentPadding = innerPadding,
            )
        }
    }
}
