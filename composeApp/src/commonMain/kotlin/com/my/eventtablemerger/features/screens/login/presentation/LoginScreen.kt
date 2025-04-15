package com.my.eventtablemerger.features.screens.login.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.my.eventtablemerger.core.navigation.AppScreens

@Composable
fun LoginScreen(
    navController: NavController
) {
    LoginView {navController.navigate(AppScreens.Observe.route)}
}