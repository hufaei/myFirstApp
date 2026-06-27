package com.example.lifelab.app

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.lifelab.app.navigation.LifeLabNavHost
import com.example.lifelab.app.navigation.topLevelDestinations
import com.example.lifelab.core.datastore.AppPreferences
import com.example.lifelab.core.datastore.DataStoreAppPreferencesRepository
import com.example.lifelab.core.datastore.LanguageMode
import com.example.lifelab.core.datastore.appPreferencesDataStore
import com.example.lifelab.core.ui.theme.LifeLabTheme

@Composable
fun LifeLabApp() {
    val context = LocalContext.current
    val preferencesRepository = remember(context.applicationContext) {
        DataStoreAppPreferencesRepository(context.applicationContext.appPreferencesDataStore)
    }
    val appPreferences by preferencesRepository.appPreferences.collectAsStateWithLifecycle(
        initialValue = AppPreferences(),
    )

    LaunchedEffect(appPreferences.languageMode) {
        AppCompatDelegate.setApplicationLocales(appPreferences.languageMode.toLocaleList())
    }

    LifeLabTheme(themeMode = appPreferences.themeMode) {
        val navController = rememberNavController()
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route ?: topLevelDestinations.first().route
        val showBottomBar = topLevelDestinations.any { destination ->
            destination.route == currentRoute
        }

        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 0.dp,
                    ) {
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
                                label = { Text(stringResource(destination.titleRes)) },
                                icon = {
                                    Icon(
                                        imageVector = destination.icon,
                                        contentDescription = stringResource(destination.titleRes),
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                ),
                            )
                        }
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

private fun LanguageMode.toLocaleList(): LocaleListCompat =
    when (this) {
        LanguageMode.System -> LocaleListCompat.getEmptyLocaleList()
        LanguageMode.Zh -> LocaleListCompat.forLanguageTags("zh")
        LanguageMode.En -> LocaleListCompat.forLanguageTags("en")
    }
