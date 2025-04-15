package com.my.eventtablemerger

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.my.eventtablemerger.core.navigation.AppScreens
import com.my.eventtablemerger.core.navigation.localNavHost
import com.my.eventtablemerger.core.theme.AppTheme
import com.my.eventtablemerger.features.screens.login.presentation.LoginScreen
import com.my.eventtablemerger.features.screens.observe.ObserveScreen
import com.my.eventtablemerger.features.screens.search.SearchScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() = ScheduleApp()

@Composable
fun ScheduleApp(
    navController: NavHostController = rememberNavController(),
) = AppTheme {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = backStackEntry?.destination?.route ?: AppScreens.Observe.route

    CompositionLocalProvider(
        localNavHost provides navController
    ) {
        NavHost(
            navController = navController,
            startDestination = AppScreens.Login.route
        ) {
            composable(
                route = AppScreens.Login.route
            ) {
                LoginScreen(
                    navController = navController
                )
            }

            composable(
                route = AppScreens.Observe.route
            ) { ObserveScreen(
                navController = navController
            ) }

            composable(
                route = AppScreens.Search.route + "/{eventId}"
            ) { stackEntry ->

                val eventId = stackEntry.arguments?.getString("eventId")!!

                SearchScreen(
                    navController = navController,
                    folderId = eventId
                )
            }

        }
    }
}


