package com.my.eventtablemerger.features.screens.observe

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.my.eventtablemerger.core.navigation.AppScreens
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ObserveScreen(
    navController: NavController,
    viewModel: ObserveViewModel = koinViewModel()
) {

    val viewState by viewModel.viewState().collectAsState()
    ObserveView(
        viewState = viewState,
        onAction = viewModel::dispatch,
        navController = navController,
        navigateToSearchScreen = {
            navController.navigate(AppScreens.Search.route + "/${it}") {
                launchSingleTop = true
            }
        }
    )
}