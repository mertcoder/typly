package com.typly.app.presentation.main.home

import androidx.lifecycle.ViewModel
import com.typly.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for the home screen functionality.
 * 
 * Provides access to current user authentication data and manages
 * home screen-related state. Serves as the main entry point for
 * user identification and authentication status.
 * 
 * @property authRepository Repository for authentication operations
 */
@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val authRepository: AuthRepository
): ViewModel() {

    /**
     * Retrieves the current authenticated user's unique identifier.
     * 
     * @return The UID of the currently authenticated user
     * @throws NullPointerException if no user is currently authenticated
     */
    fun getCurrentUserId(): String {
        return authRepository.getCurrentUser()!!.uid
    }

}
