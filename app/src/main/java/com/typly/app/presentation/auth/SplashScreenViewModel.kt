package com.typly.app.presentation.auth

import SplashNavigationState
import androidx.lifecycle.ViewModel
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.UserRepository
import com.typly.app.domain.model.UserResult
import com.typly.app.services.fcm.TokenManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

/**
 * ViewModel for the [SplashScreen].
 *
 * This ViewModel is responsible for determining the initial navigation path for the user
 * when the app starts. It checks the user's authentication status and profile completion
 * state to decide whether to navigate to the main app, profile setup, or the authentication flow.
 *
 * @property authRepository Repository for checking the current authentication state.
 * @property userRepository Repository for fetching user profile data, like completion status.
 * @property tokenManager Manager for synchronizing the FCM token.
 */
@HiltViewModel
class SplashScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    /** A [StateFlow] that represents the determined navigation destination from the splash screen. */
    private val _navigationState = MutableStateFlow<SplashNavigationState>(SplashNavigationState.Loading)
    val navigationState: StateFlow<SplashNavigationState> = _navigationState.asStateFlow()

    /**
     * Orchestrates the logic to determine the next screen for navigation.
     *
     * This function performs the following steps:
     * 1. Synchronizes the FCM token to ensure it's up-to-date.
     * 2. Checks if a user is currently authenticated.
     * 3. If authenticated, checks if their profile is complete.
     * 4. Updates the [navigationState] to direct the UI to the appropriate destination.
     */
    suspend fun navigateToNextScreen() {
        tokenManager.syncFcmToken()

        val currentUser = authRepository.getCurrentUser()
        if (currentUser != null) {
            // User is logged in, check if their profile is complete.
            userRepository.isProfileCompleted(currentUser.uid)
                .collectLatest { result ->
                    when (result) {
                        is UserResult.Loading -> {
                            _navigationState.value = SplashNavigationState.Loading
                        }
                        is UserResult.Success -> {
                            if (result.data) {
                                // User profile is complete, navigate to the main application screen.
                                _navigationState.value = SplashNavigationState.AlreadyAuthenticated(currentUser)
                            } else {
                                // User is authenticated but hasn't completed their profile.
                                _navigationState.value = SplashNavigationState.CompleteProfile
                            }
                        }
                        is UserResult.Error -> {
                            // An error occurred fetching profile status, direct to authentication.
                            _navigationState.value = SplashNavigationState.Error(result.message)
                        }
                    }
                }
        } else {
            // No user is logged in, direct to the authentication flow.
            _navigationState.value = SplashNavigationState.Authenticate
        }
    }

    /**
     * Signs out the current user and resets the navigation state to require authentication.
     * Useful for development or explicit sign-out actions.
     */
    suspend fun signOut() {
        authRepository.logout()
        _navigationState.value = SplashNavigationState.Authenticate
    }
}
