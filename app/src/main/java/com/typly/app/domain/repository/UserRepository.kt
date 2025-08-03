package com.typly.app.domain.repository

import android.content.Context
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.model.UserResult
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing user data and profile operations.
 *
 * This interface defines the contract for user-related operations including
 * profile creation, updates, user search, and profile management. It handles
 * the complete user lifecycle from initial profile creation to ongoing
 * profile maintenance and provides caching mechanisms for optimal performance.
 *
 * The repository integrates with Firebase services for data persistence and
 * file storage while providing local caching for improved user experience.
 */
interface UserRepository {

    /**
     * Creates a basic user profile from Firebase Authentication data.
     *
     * This method creates an initial user profile using information available
     * from Firebase Authentication (email, display name, photo URL). The profile
     * is marked as incomplete, requiring additional user input to finalize.
     *
     * @return Flow emitting UserResult with the created User profile or error information
     */
    suspend fun basicSaveUser(): Flow<UserResult<User>>

    /**
     * Completes the user profile setup with comprehensive information.
     *
     * This method finalizes the user profile by collecting all required information
     * including profile picture upload, nickname registration, and FCM token setup.
     * It handles image compression, storage upload, and database updates atomically.
     *
     * @param uid the user's unique identifier
     * @param fullName the user's full display name
     * @param nickname the user's unique username
     * @param bio optional biographical information
     * @param profileImageUrl optional URI of the profile image to upload
     * @param context Android context for image processing operations
     * @param fcmToken Firebase Cloud Messaging token for push notifications
     * @return Flow emitting UserResult with the completed User profile or error information
     */
    suspend fun completeSaveUser(
        uid: String,
        fullName: String,
        nickname: String,
        bio: String?,
        profileImageUrl: String?,
        context: Context,
        fcmToken: String
    ): Flow<UserResult<User>>

    /**
     * Checks if a nickname is available for registration.
     *
     * This method verifies that a proposed nickname is not already taken by
     * another user. Nicknames must be unique across the entire application
     * to ensure proper user identification.
     *
     * @param nickname the nickname to check for availability
     * @return Flow emitting UserResult with boolean availability status or error information
     */
    suspend fun isNickNameAvailable(nickname: String): Flow<UserResult<Boolean>>

    /**
     * Reserves a nickname for a specific user.
     *
     * This method registers a nickname in the nicknames collection, creating
     * a mapping between the nickname and user ID. It includes validation to
     * prevent nickname conflicts and unauthorized changes.
     *
     * @param nickname the nickname to reserve
     * @param uid the user ID who will own this nickname
     * @return Flow emitting UserResult indicating success or error information
     */
    suspend fun saveNickname(nickname: String, uid: String): Flow<UserResult<Unit>>

    /**
     * Uploads and processes a user's profile picture.
     *
     * This method handles the complete profile picture upload process including
     * image compression, format conversion, and cloud storage upload. It returns
     * the downloadable URL of the processed image.
     *
     * @param uid the user's unique identifier (used in storage path)
     * @param profileImageUrl the local URI of the image to upload
     * @param context Android context for image processing operations
     * @return Flow emitting UserResult with the download URL or error information
     */
    suspend fun saveProfilePicture(uid: String, profileImageUrl: String, context: Context): Flow<UserResult<String>>

    /**
     * Checks if a user's profile setup is complete.
     *
     * This method verifies whether a user has completed all required profile
     * setup steps by checking the profile completion flag. It's used to
     * determine navigation flow and onboarding requirements.
     *
     * @param uid the user's unique identifier
     * @return Flow emitting UserResult with completion status or error information
     */
    suspend fun isProfileCompleted(uid: String): Flow<UserResult<Boolean>>

    /**
     * Creates an initial user profile from Firebase user data.
     *
     * This method generates a basic User profile using the provided FirebaseUser
     * information. It extracts available data like email, display name, and
     * photo URL to create an initial profile structure.
     *
     * @param firebaseUser the Firebase user object containing authentication data
     * @return Flow emitting UserResult with the created User profile or error information
     */
    suspend fun createUserProfile(firebaseUser: FirebaseUser): Flow<UserResult<User>>

    /**
     * Retrieves a user by their unique identifier with caching support.
     *
     * This method fetches user information by ID, utilizing an in-memory cache
     * for improved performance. It first checks the cache and falls back to
     * database queries when necessary.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return Flow emitting the User object if found, null otherwise
     */
    suspend fun getUserById(userId: String): Flow<User?>

    /**
     * Retrieves a user's nickname by their ID.
     *
     * This convenience method fetches only the nickname of a user, utilizing
     * the cached getUserById method for optimal performance.
     *
     * @param userId the unique identifier of the user
     * @return the user's nickname if found, null otherwise
     */
    suspend fun getUserNicknameById(userId: String): String?

    /**
     * Searches for users by their username with real-time results.
     *
     * This method performs case-insensitive username searches using Firestore
     * range queries. It returns up to 10 matching users and provides real-time
     * updates as user data changes.
     *
     * @param query the search string to match against usernames
     * @return Flow emitting list of matching User objects or null if no results
     */
    suspend fun searchUsersByUserName(query: String): Flow<List<User>?>

    /**
     * Retrieves the current user's profile with real-time updates.
     *
     * This method fetches the authenticated user's profile information,
     * utilizing local caching when available and setting up real-time listeners
     * for profile changes when cache is empty.
     *
     * @return Flow emitting the current User profile or null if not authenticated
     */
    suspend fun retrieveCurrentUserProfile(): Flow<User?>

    /**
     * Updates a user's profile picture with image processing.
     *
     * This method handles profile picture updates including image compression,
     * cloud storage upload, and database record updates. It processes the image
     * optimally for profile display purposes.
     *
     * @param uid the user's unique identifier
     * @param newProfileImageUrl the local URI of the new profile image
     * @param context Android context for image processing operations
     * @return Flow emitting true if update was successful, false otherwise
     */
    suspend fun updateUserProfilePicture(
        uid: String,
        newProfileImageUrl: String,
        context: Context
    ): Flow<Boolean>

    /**
     * Updates a user's display name and biographical information.
     *
     * This method allows users to modify their public profile information
     * including their full name and bio description without affecting other
     * profile fields like profile picture or nickname.
     *
     * @param uid the user's unique identifier
     * @param fullName the user's new full display name
     * @param bio the user's new biographical information
     * @return Flow emitting true if update was successful, false otherwise
     */
    suspend fun updateFullNameAndBio(uid: String, fullName: String, bio: String): Flow<Boolean>
}
