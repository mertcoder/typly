package com.typly.app.presentation.navigation

import com.typly.app.R

/**
 * Sealed class defining all navigation destinations in the application.
 *
 * Provides a type-safe way to define navigation routes with optional labels and icons
 * for bottom navigation. Each screen object represents a unique destination with
 * its route string and optional UI metadata.
 *
 * @property route The navigation route string used by Jetpack Compose Navigation
 * @property label Optional display label for bottom navigation items
 * @property icon Optional icon resource ID for bottom navigation items
 */
sealed class Screen(val route: String, val label: String?=null,val icon: Int?= null) {
    object SplashScreen : Screen("splash_screen")
    object LoginScreen : Screen("login_screen")
    object OnboardingScreen : Screen("onboarding_screen")
    object RegisterScreen : Screen("register_screen")
    object ProfileSetupScreen : Screen("profile_setup_screen")
    object HomeScreen : Screen("home_screen")
    object Chats : Screen("chats", "Chats",  R.drawable.default_chats)
    object Profile : Screen("profile", "Profile",R.drawable.default_profile_picture)
    object Settings : Screen("settings", "Settings",R.drawable.default_settings)
    object SearchUser: Screen("search_user", "Search User")
    /**
     * One-to-one chat screen with secure token-based navigation.
     *
     * Uses a secure token parameter to establish chat sessions with proper
     * authentication and authorization checks.
     */
    object OneToOneChat: Screen("one_to_one_chat/{token}", "One to One Chat") {
        /**
         * Creates the navigation route with the provided secure token.
         *
         * @param token Secure chat session token for authentication
         * @return Complete navigation route string with token parameter
         */
        fun createRoute(token: String): String = "one_to_one_chat/$token"
    }

    /**
     * Audio call screen for voice communication between users.
     *
     * Supports both initiating and receiving calls with proper user identification
     * and call state management.
     */
    data object AudioCall : Screen("audio_call/{targetUserId}?isReceiver={isReceiver}") {
        /**
         * Creates the navigation route for audio call functionality.
         *
         * @param targetUserId ID of the user to call or being called by
         * @param isReceiver Whether this user is receiving the call (default: false)
         * @return Complete navigation route string with user ID and receiver status
         */
        fun createRoute(targetUserId: String, isReceiver: Boolean = false) = "audio_call/$targetUserId?isReceiver=$isReceiver"
    }

    /**
     * Incoming call screen for handling received call notifications.
     *
     * Displays incoming call interface with caller information and call controls
     * for accepting or declining calls.
     */
    data object IncomingCall : Screen("incoming_call/{callId}/{callerName}") {
        /**
         * Creates the navigation route for incoming call handling.
         *
         * @param callId Unique identifier for the incoming call session
         * @param callerName Display name of the user initiating the call
         * @return Complete navigation route string with call ID and caller name
         */
        fun createRoute(callId: String, callerName: String) = "incoming_call/$callId/$callerName"
    }

}
