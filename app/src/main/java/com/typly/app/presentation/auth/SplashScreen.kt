package com.typly.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.typly.app.R
import kotlinx.coroutines.delay

/**
 * A composable that displays the splash screen, acting as the initial entry point of the UI.
 *
 * This screen shows a logo for a brief period and then triggers a navigation check to determine
 * the user's authentication and profile status. Based on the result, it invokes one of the
 * provided callbacks to navigate to the appropriate next screen.
 *
 * @param onAuthenticate Callback to navigate to the authentication flow (e.g., Onboarding/Login).
 * @param onProfileSetup Callback to navigate to the profile setup screen for new users.
 * @param onAlreadyAuthenticated Callback to navigate directly to the main app screen for authenticated users.
 */
@Composable
fun SplashScreen(
    onAuthenticate: () -> Unit,
    onProfileSetup: () -> Unit,
    onAlreadyAuthenticated: () -> Unit
) {

    val viewModel = hiltViewModel<SplashScreenViewModel>()
    val navigationState by viewModel.navigationState.collectAsState()

    // This effect runs once when the composable enters the composition.
    // It waits for a short delay, then triggers the navigation logic in the ViewModel.
    LaunchedEffect(true) {
        delay(1000)
        viewModel.navigateToNextScreen()
    }

    // This effect observes the navigationState from the ViewModel.
    // When the state changes, it triggers the corresponding navigation callback.
    LaunchedEffect(navigationState) {
        when (navigationState) {
            is SplashNavigationState.AlreadyAuthenticated -> onAlreadyAuthenticated()
            is SplashNavigationState.CompleteProfile -> onProfileSetup()
            is SplashNavigationState.Authenticate -> onAuthenticate()
            else -> {
                // No-op for Loading and Error states, as the splash screen remains visible.
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = painterResource(id = R.drawable.blurrybg_dark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Application Logo
            Image(
                painter = painterResource(id = R.drawable.typlylogo),
                contentDescription = "Typly Logo",
                modifier = Modifier
                    .size(180.dp)
                    .offset(y = (-40).dp)
            )
        }
    }
}
