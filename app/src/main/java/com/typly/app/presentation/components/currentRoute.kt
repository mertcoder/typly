package com.typly.app.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Retrieves the current route from the navigation controller.
 * 
 * Observes the navigation back stack state and returns the route string
 * of the currently displayed destination. This is useful for determining
 * which screen is currently active in the navigation hierarchy.
 * 
 * @param navController The navigation controller to observe
 * @return The route string of the current destination, or null if no destination is active
 */
@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}
