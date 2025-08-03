package com.typly.app.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.typly.app.presentation.auth.LoginScreen
import com.typly.app.presentation.auth.OnboardingScreen
import com.typly.app.presentation.auth.ProfileSetupScreen
import com.typly.app.presentation.auth.RegisterScreen
import com.typly.app.presentation.auth.SplashScreen
import com.typly.app.presentation.main.home.HomeScreen
import com.typly.app.presentation.auth.LoginViewModel
import com.typly.app.presentation.auth.ProfileSetupViewModel
import com.typly.app.presentation.auth.RegisterViewModel

/**
 * Main application navigation composable that defines the complete navigation graph.
 * 
 * Sets up the navigation structure for the entire application including authentication
 * flow (splash, onboarding, login, register, profile setup) and main app navigation.
 * Features smooth transitions between screens and proper navigation state management.
 * 
 * Handles the complete user journey from initial app launch through authentication
 * to the main application experience with appropriate back stack management.
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {
        composable(
            route = Screen.SplashScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {            
            SplashScreen(onAuthenticate = {
                navController.navigate(Screen.OnboardingScreen.route) {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
            },
            onProfileSetup = {
                navController.navigate(Screen.ProfileSetupScreen.route) {
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
            },
            onAlreadyAuthenticated = {
                navController.navigate(Screen.HomeScreen.route){
                    popUpTo(Screen.SplashScreen.route) { inclusive = true }
                }
            })
        }
        
        // Other authentication screens
        composable(
            route = Screen.LoginScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            val viewModel: LoginViewModel = hiltViewModel()

            LoginScreen(viewModel, onLoginSuccess = {
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }, onRegisterClick = {
                navController.navigate(Screen.RegisterScreen.route) {
                    popUpTo(Screen.LoginScreen.route) { inclusive = true }
                }
            }, onCreateGoogleProfile = {
                navController.navigate(Screen.ProfileSetupScreen.route){
                    popUpTo(Screen.LoginScreen.route){inclusive = true}
                }
            })
        }
        
        // More authentication screens
        composable(
            route = Screen.OnboardingScreen.route,
            enterTransition = { slideInHorizontally(initialOffsetX = { 1000 }) },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -1000 }) }
        ) {
            OnboardingScreen(onFinished = {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.OnboardingScreen.route) { inclusive = true }
                }
            })
        }
        
        composable(
            route = Screen.RegisterScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            val viewModel: RegisterViewModel = hiltViewModel()
            RegisterScreen(viewModel, onLoginClick = {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.RegisterScreen.route) { inclusive = true }
                }
            }, onRegisterSuccess = {
                navController.navigate(Screen.ProfileSetupScreen.route) {
                    popUpTo(Screen.RegisterScreen.route) { inclusive = true }
                }
            }, onAlreadyAuthenticated = {
                navController.navigate(Screen.HomeScreen.route){
                    popUpTo(Screen.RegisterScreen.route){inclusive = true}
                }
            })
        }

        composable(
            route = Screen.ProfileSetupScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            val viewModel: ProfileSetupViewModel = hiltViewModel()
            ProfileSetupScreen(
                viewModel = viewModel,
                onSetupComplete = {
                    navController.navigate(Screen.HomeScreen.route){
                        popUpTo(Screen.ProfileSetupScreen.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Main app HomeScreen - this is where the BottomNavigation is located
        composable(
            route = Screen.HomeScreen.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            HomeScreen(onSignedOut = {
                navController.navigate(Screen.LoginScreen.route) {
                    popUpTo(Screen.HomeScreen.route) { inclusive = true }
                }
            })
        }

    }
}
