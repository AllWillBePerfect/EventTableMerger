package com.my.eventtablemerger.core.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val localNavHost = staticCompositionLocalOf<NavHostController> { error("No default nav host") }