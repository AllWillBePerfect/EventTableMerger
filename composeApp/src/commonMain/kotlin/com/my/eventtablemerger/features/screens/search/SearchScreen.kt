package com.my.eventtablemerger.features.screens.search

import androidx.compose.runtime.Composable
import androidx.navigation.NavController

@Composable
fun SearchScreen(
    navController: NavController,
    folderId: String,
) {
    SearchView(
        globalNavController = navController,
        folderId = folderId,
    )
}