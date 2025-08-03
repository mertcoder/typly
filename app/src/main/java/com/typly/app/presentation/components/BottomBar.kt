package com.typly.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.typly.app.presentation.navigation.Screen
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * Composable that displays the bottom navigation bar with glass effect styling.
 *
 * Provides navigation between main app sections (Chats, Profile, Settings) with
 * modern glass morphism design. Handles navigation state and prevents duplicate
 * navigation to the same screen.
 *
 * Features:
 * - Glass effect background with gradient transparency
 * - Rounded corners for modern appearance
 * - Icon-based navigation with labels
 * - Active state indication
 * - Single-top navigation to prevent stack buildup
 *
 * @param navController The navigation controller for handling screen transitions
 */
@Composable
fun BottomBar(navController: NavHostController) {
    val items = listOf(Screen.Chats, Screen.Profile, Screen.Settings)
    val navBackStackEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = navBackStackEntry?.destination?.route

    // Modern glass effect container with optimized dimensions
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.025f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        // Navigation bar with transparent background and height optimization
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 70.dp),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp
        ) {
            items.forEach { screen ->
                NavigationBarItem(
                    icon = {
                        // Drawable resource icon usage
                        screen.icon?.let { resId ->
                            Icon(
                                painter = painterResource(id = resId),
                                contentDescription = screen.label,
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                        }
                    },
                    label = { Text(screen.label ?: "") },
                    selected = currentRoute == screen.route,
                    onClick = {
                        // Prevent navigation to the same screen
                        if (currentRoute != screen.route) {
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Chats.route)
                                launchSingleTop = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = Color.Transparent,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
