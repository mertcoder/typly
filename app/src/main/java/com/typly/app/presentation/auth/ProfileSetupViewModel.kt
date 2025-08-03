package com.typly.app.presentation.auth

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.domain.model.AuthResult
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.data.remote.service.AuthService
import com.typly.app.domain.repository.UserRepository
import com.typly.app.domain.model.UserResult
import com.typly.app.services.fcm.TokenManager
import com.typly.app.presentation.call.UserPresenceManager
import com.typly.app.util.RegisterValidationResult
import com.typly.app.domain.usecase.ValidateCompleteRegisterUseCase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing the UI state and business logic for the profile setup screen.
 *
 * This ViewModel handles:
 * - Storing user inputs for nickname, full name, bio, and profile image.
 * - Validating these inputs using the [ValidateCompleteRegisterUseCase].
 * - Coordinating with the [UserRepository] to check for nickname availability and save the
 * completed user profile to Firestore.
 * - Managing loading and error states for the UI.
 */
@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authService: AuthService,
    private val validateCompleteRegisterUseCase: ValidateCompleteRegisterUseCase,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val firestore: FirebaseFirestore,
    private val userPresenceManager: UserPresenceManager,
    private val tokenManager: TokenManager
) : ViewModel() {

    /** A [StateFlow] holding the current value of the nickname input field. */
    private val _nickname = MutableStateFlow("")
    val nickname: StateFlow<String> = _nickname.asStateFlow()

    /** A [StateFlow] holding the current value of the bio input field. */
    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    /** A [StateFlow] holding the current value of the full name input field. */
    private val _fullName = MutableStateFlow("")
    val fullName: StateFlow<String> = _fullName.asStateFlow()

    /** A [StateFlow] holding the [Uri] of the selected profile image. */
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    /** A [StateFlow] that holds the result of input validation from the use case. */
    private val _registerState = MutableStateFlow<RegisterValidationResult?>(null)
    val registerState: StateFlow<RegisterValidationResult?> = _registerState.asStateFlow()

    /** A [StateFlow] representing the overall loading state of the profile completion process. */
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    /** A [StateFlow] that holds the final result of the profile save operation. */
    private val _completeRegisterState = MutableStateFlow<UserResult<User>?>(null)
    val completeRegisterState: StateFlow<UserResult<User>?> = _completeRegisterState.asStateFlow()

    /**
     * Retrieves the current authenticated user from the authentication service.
     * @return The [FirebaseUser] object, or `null` if no user is authenticated.
     */
    fun getCurrentUser(): FirebaseUser? {
        return authService.getCurrentUser()
    }

    /**
     * Validates profile inputs and saves the completed user profile to the backend.
     *
     * This function orchestrates the entire profile completion process:
     * 1. Performs input validation using [ValidateCompleteRegisterUseCase].
     * 2. Checks if the chosen nickname is available.
     * 3. Fetches the latest FCM device token.
     * 4. Calls [UserRepository] to save the user data and the chosen nickname.
     * 5. Updates the UI state ([_isLoading], [_completeRegisterState]) to reflect the operation's progress.
     * 6. Sets the user's presence to online upon successful completion.
     *
     * @param uid The user's unique ID from Firebase Auth.
     * @param fullName The user's full name.
     * @param nickname The user's chosen unique nickname.
     * @param bio An optional short biography.
     * @param profileImageUrl A string representation of the selected profile image URI.
     * @param context The application context, required for image processing.
     */
    fun completeRegister(uid: String, fullName: String, nickname: String, bio: String?, profileImageUrl: String?, context: Context) {
        // 1. Validate the input first
        _registerState.value = null // Reset the state before validation
        val validationResult = validateCompleteRegisterUseCase(nickname, fullName)
        _registerState.value = validationResult

        // 2. If validation is successful, proceed
        if (validationResult is RegisterValidationResult.Success && getCurrentUser() != null) {
            _isLoading.value = true
            viewModelScope.launch {
                val token = tokenManager.getCurrentDeviceToken()

                // 3. Check for nickname availability
                val isAvailable = userRepository.isNickNameAvailable(nickname).first()
                if (isAvailable is UserResult.Error) {
                    _registerState.value = RegisterValidationResult.NicknameAlreadyExists
                    _isLoading.value = false
                    return@launch
                }

                // 4. Save the completed user profile
                userRepository.completeSaveUser(
                    uid = uid,
                    fullName = fullName,
                    nickname = nickname,
                    bio = bio,
                    profileImageUrl = profileImageUrl,
                    context = context,
                    fcmToken = token
                ).collectLatest { userSaveResult ->
                    when (userSaveResult) {
                        is UserResult.Success -> {
                            // 5. If user save is successful, save the nickname to reserve it
                            userRepository.saveNickname(nickname, uid).collectLatest { saveNickNameResult ->
                                when (saveNickNameResult) {
                                    is UserResult.Success -> {
                                        _completeRegisterState.value = userSaveResult
                                        _isLoading.value = false
                                        userPresenceManager.setUserOnlineManually()
                                    }
                                    is UserResult.Error -> {
                                        _completeRegisterState.value = saveNickNameResult
                                        _isLoading.value = false
                                    }
                                    else -> {}
                                }
                            }
                        }
                        is UserResult.Error -> {
                            _completeRegisterState.value = userSaveResult
                            _isLoading.value = false
                        }
                        else -> {}
                    }
                }
            }
        } else {
            _completeRegisterState.value = UserResult.Error("Invalid input or no current user")
            _isLoading.value = false
        }
    }

    /** Updates the internal state for the user's nickname. */
    fun updateNickname(newNickname: String) {
        _nickname.value = newNickname
    }

    /** Updates the internal state for the user's bio. */
    fun updateBio(newBio: String) {
        _bio.value = newBio
    }

    /** Updates the internal state for the user's full name. */
    fun updateFullName(fullName: String) {
        _fullName.value = fullName
    }

    /** Updates the internal state for the user's selected profile image URI. */
    fun updateProfileImage(uri: Uri?) {
        _profileImageUri.value = uri
    }

    /** A utility function for debugging that prints the selected image URI to the console. */
    fun logSelectedImageUri() {
        _profileImageUri.value?.let {
            Log.d("ProfileSetupViewModel", "Selected image URI: $it")
        }
    }
}
