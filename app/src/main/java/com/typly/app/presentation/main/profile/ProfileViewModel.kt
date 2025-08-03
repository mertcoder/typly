package com.typly.app.presentation.main.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.ChatRepository
import com.typly.app.domain.repository.UserRepository
import com.typly.app.domain.model.UserResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing user profile operations and data.
 * 
 * Handles profile data retrieval, profile image updates, text profile updates,
 * and manages UI states for profile-related operations. Provides validation
 * for profile updates and reactive state flows for UI consumption.
 * 
 * @property chatRepository Repository for chat-related operations
 * @property userRepository Repository for user-related operations
 * @property authRepository Repository for authentication operations
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
    ): ViewModel(){

    /** StateFlow containing current user's profile data */
    private val _currentUserData = MutableStateFlow<User?>(null)
    val currentUserData = _currentUserData.asStateFlow()

    /** StateFlow containing selected profile image URI */
    private val _profileImageUri = MutableStateFlow<Uri?>(null)
    val profileImageUri: StateFlow<Uri?> = _profileImageUri.asStateFlow()

    /** StateFlow indicating whether a profile update operation is in progress */
    private val _isUpdatingProfile = MutableStateFlow(false)
    val isUpdatingProfile: StateFlow<Boolean> = _isUpdatingProfile.asStateFlow()

    /** StateFlow containing the result of profile update operations */
    private val _profileUpdateResult = MutableStateFlow<UserResult<User>?>(null)
    val profileUpdateResult: StateFlow<UserResult<User>?> = _profileUpdateResult.asStateFlow()



    /**
     * Fetches the current user's profile data from the repository.
     * 
     * Retrieves and observes the current user's profile information,
     * updating the currentUserData StateFlow when data is available.
     * Should be called to initialize or refresh user profile data.
     */
    suspend fun fetchUserProfileData() {
        userRepository.retrieveCurrentUserProfile().collectLatest {
            if(it != null) {
                _currentUserData.value = it
            }
        }
    }
    /**
     * Updates the selected profile image URI.
     * 
     * Sets the URI of the image selected for profile update.
     * This does not immediately upload the image - call updateProfileImage() to upload.
     * 
     * @param uri The URI of the selected image, or null to clear selection
     */
    fun updateProfileImageUri(uri: Uri?) {
        _profileImageUri.value = uri
    }


    /**
     * Updates the user's profile image.
     * 
     * Uploads the selected image to the server and updates the user's profile.
     * Requires a previously selected image URI and authenticated user.
     * Manages loading states and provides result feedback.
     * 
     * @param context Android context for file operations
     */
    fun updateProfileImage(context: Context) {
        val currentUser = authRepository.getCurrentUser()
        val imageUri = _profileImageUri.value

        if (currentUser == null || imageUri == null) {
            _profileUpdateResult.value = UserResult.Error("User not authenticated or no image selected")
            return
        }

        _isUpdatingProfile.value = true
        _profileUpdateResult.value = null

        viewModelScope.launch {
            try {
                val userData = _currentUserData.value

                if (userData != null) {
                    userRepository.updateUserProfilePicture(userData.id,imageUri.toString(),context).collectLatest {
                        when(it){
                            false->{ _profileUpdateResult.value = UserResult.Error("Unknown error occurred") }
                            else->{}
                        }
                    }
                }
            } catch (e: Exception) {
                _profileUpdateResult.value = UserResult.Error(e.message ?: "Unknown error occurred")
                _isUpdatingProfile.value = false
            }
        }
    }
    
    /**
     * Updates the user's text profile information with validation.
     * 
     * Updates full name and bio with comprehensive validation including
     * length checks, emptiness validation, and change detection.
     * Provides detailed error messages for validation failures.
     * 
     * @param fullName The new full name for the user
     * @param bio The new bio/description for the user
     * @param context Android context for operations
     */
    fun updateTextProfile(fullName: String, bio: String, context: Context) {
        val currentUser = authRepository.getCurrentUser()
        val userData = _currentUserData.value
        
        if (currentUser == null || userData == null) {
            _profileUpdateResult.value = UserResult.Error("User not authenticated")
            return
        }
        
        // Validation and cleaning
        val trimmedFullName = fullName.trim()
        val trimmedBio = bio.trim()
        
        // Validation checks
        if (trimmedFullName.isBlank()) {
            _profileUpdateResult.value = UserResult.Error("Full name cannot be empty")
            return
        }
        
        if (trimmedFullName.length < 3) {
            _profileUpdateResult.value = UserResult.Error("Full name must be at least 3 characters")
            return
        }
        
        if (trimmedBio.isBlank()) {
            _profileUpdateResult.value = UserResult.Error("Bio cannot be empty")
            return
        }
        
        if (trimmedBio.length < 3) {
            _profileUpdateResult.value = UserResult.Error("Bio must be at least 3 characters")
            return
        }
        
        // Change detection - skip update if no changes
        if (userData.fullName.trim() == trimmedFullName && userData.bio.trim() == trimmedBio) {
            _profileUpdateResult.value = UserResult.Error("No changes detected")
            return
        }
        
        _isUpdatingProfile.value = true
        _profileUpdateResult.value = null
        
        viewModelScope.launch {
            try {
                userRepository.updateFullNameAndBio(userData.id, trimmedFullName, trimmedBio).collectLatest { success ->
                    when(success) {
                        false -> { 
                            _profileUpdateResult.value = UserResult.Error("Update failed")
                            _isUpdatingProfile.value = false
                        }
                        true -> { 
                            _profileUpdateResult.value = UserResult.Success(userData.copy(fullName = trimmedFullName, bio = trimmedBio))
                            _isUpdatingProfile.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _profileUpdateResult.value = UserResult.Error(e.message ?: "Unknown error")
                _isUpdatingProfile.value = false
            }
        }
    }
    
    /**
     * Clears the current profile update result.
     * 
     * Resets the update result state to allow dismissing success/error messages
     * or preparing for new update operations.
     */
    fun clearUpdateResult() {
        _profileUpdateResult.value = null
    }
}
