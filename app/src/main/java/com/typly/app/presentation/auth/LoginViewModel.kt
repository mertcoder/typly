package com.typly.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.domain.model.AuthResult
import com.typly.app.domain.model.LoginAuthStateUser
import com.typly.app.domain.model.LoginType
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.presentation.call.UserPresenceManager
import com.typly.app.util.LoginValidationResult
import com.typly.app.domain.usecase.ValidateLoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for handling user authentication operations including
 * email/password login, Google authentication, and anonymous login.
 * 
 * This ViewModel manages three main authentication states:
 * - Input validation state for login credentials
 * - Authentication result state from Firebase
 * - UI loading state for displaying progress indicators
 * 
 * The authentication flow follows these steps:
 * 1. Validate user input (email/password format, requirements)
 * 2. If validation passes, proceed with Firebase authentication
 * 3. Handle authentication result and update UI state accordingly
 * 4. Manage user presence status after successful authentication
 *
 * @property validateLoginUseCase Use case for validating login input according to business rules
 * @property authRepository Repository interface for Firebase authentication operations
 * @property userPresenceManager Manager for handling user online/offline presence status
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val validateLoginUseCase: ValidateLoginUseCase,
    private val authRepository: AuthRepository,
    private val userPresenceManager: UserPresenceManager
) : ViewModel() {
    
    /**
     * Holds the current validation state of login input.
     * Contains validation errors for email, password, or success state.
     * 
     * @see LoginValidationResult for possible validation states
     */
    private val _loginState = MutableStateFlow<LoginValidationResult?>(null)
    val loginState: StateFlow<LoginValidationResult?> = _loginState.asStateFlow()
    
    /**
     * Holds the current authentication state after attempting to login.
     * Contains the authentication result (success/error) and login type used.
     * 
     * @see LoginAuthStateUser for authentication state structure
     * @see LoginType for supported authentication methods
     */
    private val _authState = MutableStateFlow<LoginAuthStateUser?>(null)
    val authState: StateFlow<LoginAuthStateUser?> = _authState.asStateFlow()
    
    /**
     * Indicates whether an authentication operation is currently in progress.
     * Used to show loading indicators in the UI and prevent multiple simultaneous requests.
     */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Attempts to authenticate a user with email and password.
     * 
     * This method first validates the input using the validation use case,
     * and only proceeds with authentication if validation passes.
     * 
     * Authentication flow:
     * 1. Reset previous validation state
     * 2. Validate email and password format/requirements
     * 3. If valid, attempt Firebase authentication
     * 4. Update authentication state based on result
     * 5. Set user presence to online if successful
     * 
     * @param email User's email address for authentication
     * @param password User's password for authentication
     * 
     * @throws SecurityException if authentication fails due to security reasons
     * @throws NetworkException if authentication fails due to network issues
     */
    fun login(email: String, password: String) {
        // First validate the input
        viewModelScope.launch {

        _loginState.value = null // Reset the state before validation
        val validationResult = validateLoginUseCase(email, password)
        _loginState.value = validationResult
        
        // If validation is successful, proceed with authentication
        if (validationResult is LoginValidationResult.Success) {
            _isLoading.value = true
            authRepository.login(email,password).collectLatest { result ->
                when (result) {
                    is AuthResult.Success -> {
                        _authState.value = LoginAuthStateUser(firebaseUser = result, loginType = LoginType.BASIC)
                        _isLoading.value = false
                        userPresenceManager.setUserOnlineManually()
                    }
                    is AuthResult.Error -> {
                        _authState.value = LoginAuthStateUser(firebaseUser = result, loginType = LoginType.BASIC)
                        _isLoading.value = false
                    }else->{}
                }
            }

        }
    }
    }

    /**
     * Attempts to authenticate a user using Google Sign-In with the provided ID token.
     * 
     * This method bypasses input validation since Google handles credential validation,
     * and directly proceeds with Firebase authentication using the Google ID token.
     * 
     * @param idToken The ID token received from Google Sign-In process
     * 
     * @throws SecurityException if the Google ID token is invalid or expired
     * @throws NetworkException if authentication fails due to network issues
     */
    fun considerGoogleAuth(idToken: String){
        viewModelScope.launch {
            authRepository.signInWithGoogle(idToken).collectLatest { result->
                when(result){
                    is AuthResult.Success->{
                            _authState.value = LoginAuthStateUser(userForGoogleUser = result, loginType = LoginType.GOOGLE)
                        _isLoading.value = false
                        userPresenceManager.setUserOnlineManually()

                    }
                    is AuthResult.Error->{
                        _authState.value = LoginAuthStateUser(userForGoogleUser = result, loginType = LoginType.GOOGLE)
                        _isLoading.value = false

                    }
                    else->{}
                    }

                }
            }
        }

    /**
     * Attempts to authenticate anonymously without requiring user credentials.
     * 
     * Note: This feature is currently not implemented and serves as a placeholder
     * for future anonymous authentication functionality.
     * 
     * @deprecated This method is not yet implemented
     * @todo Implement anonymous authentication logic
     */
    fun loginAnonymously() {
        _isLoading.value = true
        /*
        viewModelScope.launch {
            authRepository.loginAnonymously().collectLatest { result ->
                _authState.value = result
                if (result !is AuthResult.Loading) {
                    _isLoading.value = false
                }
            }
        }

         */
    }
    
    /**
     * Resets the authentication state to null.
     * 
     * This method should be called when:
     * - User navigates away from login screen
     * - User wants to clear previous authentication errors
     * - Before attempting a new authentication after a previous failure
     */
    fun resetAuthState() {
        _authState.value = null
    }
}
