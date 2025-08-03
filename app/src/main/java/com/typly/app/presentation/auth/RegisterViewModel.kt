package com.typly.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.domain.model.AuthMethod
import com.typly.app.domain.model.AuthResult
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.UserRepository
import com.typly.app.domain.model.UserResult
import com.typly.app.util.RegisterValidationResult
import com.typly.app.domain.usecase.ValidateBasicRegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the user registration screen.
 *
 * This class manages the UI state and business logic for the registration process. It handles
 * input validation, orchestrates authentication calls via different methods (Basic, Google),
 * and coordinates with the UserRepository to save new user profiles to the database.
 *
 * @property validateBasicRegisterUseCase Use case for validating email and password inputs.
 * @property authRepository Repository for handling authentication operations with Firebase.
 * @property userRepository Repository for handling user profile data operations in Firestore.
 */
@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val validateBasicRegisterUseCase: ValidateBasicRegisterUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** A [StateFlow] that holds the result of input validation for the registration form. */
    private val _registerState = MutableStateFlow<RegisterValidationResult?>(null)
    val registerState: StateFlow<RegisterValidationResult?> = _registerState.asStateFlow()

    /** A [StateFlow] that holds the result of the authentication attempt from the repository. */
    private val _basicAuthState = MutableStateFlow<AuthResult<User>?>(null)
    val basicAuthState: StateFlow<AuthResult<User>?> = _basicAuthState.asStateFlow()

    /** A [StateFlow] that holds the result of saving the new user profile to the database. */
    private val _basicSaveToDbState = MutableStateFlow<UserResult<User>?>(null)
    val basicSaveToDbState: StateFlow<UserResult<User>?> = _basicSaveToDbState.asStateFlow()

    /** A [StateFlow] representing the overall loading state of the registration process. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /**
     * Initiates the registration process based on the provided authentication method.
     *
     * For [AuthMethod.Basic], it first validates the inputs. If successful, it proceeds
     * to call the authentication repository. For [AuthMethod.Google], it directly calls
     * the repository to sign in with the provided ID token.
     *
     * @param method The authentication method to be used, either [AuthMethod.Basic] or [AuthMethod.Google].
     */
    fun register(method: AuthMethod) {
        _isLoading.value = true
        _registerState.value = null // Reset validation state before a new attempt

        when (method) {
            is AuthMethod.Basic -> {
                val validationResult = validateBasicRegisterUseCase(method.email, method.password)
                _registerState.value = validationResult
                if (validationResult is RegisterValidationResult.Success) {
                    viewModelScope.launch {
                        authRepository.basicRegister(method.email, method.password)
                            .collectLatest { result ->
                                handleResult(method, result)
                            }
                    }
                } else {
                    _isLoading.value = false // Stop loading if validation fails
                }
            }

            is AuthMethod.Google -> {
                viewModelScope.launch {
                    authRepository.signInWithGoogle(idToken = method.idToken).collectLatest { result ->
                        handleResult(method, result)
                    }
                }
            }
        }
    }

    /**
     * A private helper function to process the [AuthResult] from the repository.
     *
     * After an authentication attempt, this function updates the relevant UI state flows.
     * If authentication is successful, it proceeds to save the user's profile to the database.
     *
     * @param method The [AuthMethod] that was used for the attempt.
     * @param result The [AuthResult] received from the repository.
     */
    private suspend fun handleResult(method: AuthMethod, result: AuthResult<User>?) {
        _basicAuthState.value = result
        when (result) {
            is AuthResult.Success -> {
                // For Google sign-in, only save to DB if it's a new user or profile is incomplete.
                val shouldSaveUser = when (method) {
                    is AuthMethod.Basic -> true
                    is AuthMethod.Google -> !result.data.isProfileComplete
                }

                if (shouldSaveUser) {
                    userRepository.basicSaveUser().collectLatest { resultSave ->
                        _basicSaveToDbState.value = resultSave
                    }
                } else {
                    _basicSaveToDbState.value = UserResult.Success(result.data)
                }
            }
            is AuthResult.Error -> {
                _basicSaveToDbState.value = UserResult.Error(result.message)
            }
            else -> Unit // Handle Loading or null states
        }
        if (result !is AuthResult.Loading) {
            _isLoading.value = false
        }
    }

    /**
     * Resets the authentication state to null.
     * This is useful to clear UI feedback (like error messages) after navigation.
     */
    fun resetAuthState() {
        _basicAuthState.value = null
    }
}
