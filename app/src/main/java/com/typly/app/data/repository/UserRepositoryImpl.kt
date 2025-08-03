package com.typly.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import com.typly.app.data.cache.UserProfileCache
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.model.UserResult
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Provider

/**
 * Implementation of [UserRepository] that handles user data operations with Firebase services.
 *
 * This repository provides comprehensive user management functionality including:
 * - User profile creation and updates
 * - User search capabilities
 * - Profile picture management with compression
 * - Nickname availability checking
 * - Global user caching system for improved performance
 * - Real-time user data synchronization
 *
 * The implementation uses Firebase Firestore for data persistence, Firebase Storage for file uploads,
 * and Firebase Authentication for user management. It includes an in-memory caching system to reduce
 * database calls and improve application performance.
 *
 * @param firestore Firebase Firestore instance for database operations
 * @param auth Firebase Authentication instance for user authentication
 * @param authRepository Provider for AuthRepository to avoid circular dependency issues
 * @param storage Firebase Storage instance for file operations
 */
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val authRepository: Provider<AuthRepository>, // Use Provider to avoid circular dependency issues
    private val storage : FirebaseStorage,
): UserRepository {

    /**
     * Companion object containing global user cache management functionality.
     *
     * This caching system stores user data in memory for quick access and reduces
     * the number of database calls. The cache has a 5-minute validity period and
     * provides methods for cache management and monitoring.
     */
    companion object {
        /**
         * Global user cache - stores user data in RAM for quick access.
         * Maps user IDs to User objects for fast retrieval.
         */
        private val globalUserCache = mutableMapOf<String, User>()

        /**
         * Timestamp of the last cache update in milliseconds.
         * Used to determine cache validity.
         */
        private var lastCacheUpdate = 0L

        /**
         * Cache duration in milliseconds (5 minutes).
         * After this duration, cache is considered stale and needs refresh.
         */
        private const val CACHE_DURATION = 5 * 60 * 1000L // 5 minutes

        /**
         * Checks if the current cache is still valid based on the cache duration.
         *
         * @return true if cache is valid, false if it needs to be refreshed
         */
        private fun isCacheValid(): Boolean {
            return System.currentTimeMillis() - lastCacheUpdate < CACHE_DURATION
        }

        /**
         * Updates the cache timestamp to the current time.
         * Should be called whenever cache data is updated.
         */
        private fun updateCacheTimestampCache() {
            lastCacheUpdate = System.currentTimeMillis()
        }

        /**
         * Clears all cached user data and resets the cache timestamp.
         * Useful for memory management or when cache needs to be invalidated.
         */
        fun clearUserCache() {
            globalUserCache.clear()
            lastCacheUpdate = 0L
        }

        /**
         * Returns the current size of the global user cache.
         *
         * @return number of users currently cached in memory
         */
        fun getCacheSize(): Int = globalUserCache.size
    }

    /**
     * Returns a defensive copy of the current global user cache.
     * This prevents external modification of the internal cache structure.
     *
     * @return immutable map containing all cached users
     */
    fun getGlobalUserCacheSnapshot(): Map<String, User> {
        return globalUserCache.toMap() // Defensive copy
    }

    /**
     * Adds or updates a user in the global cache.
     *
     * @param userId the unique identifier of the user
     * @param user the user object to cache
     */
    fun putUserInGlobalCache(userId: String, user: User) {
        globalUserCache[userId] = user
    }

    /**
     * Updates the cache timestamp to mark the cache as recently updated.
     * Should be called after successful cache operations.
     */
    fun updateCacheTimestamp() {
        updateCacheTimestampCache()
    }

    /**
     * Searches for users by their username using real-time Firestore queries.
     *
     * This method performs a case-insensitive search on user nicknames and returns
     * up to 10 matching results. The search uses Firestore's range queries with
     * startAt/endAt to find usernames that begin with the search query.
     *
     * @param query the search string to match against usernames (case-insensitive)
     * @return Flow emitting a list of matching users, or empty list if no matches found
     */
    override suspend fun searchUsersByUserName(query: String): Flow<List<User>?> = callbackFlow {
        if (query.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val searchQuery = query.trim().lowercase()
        val listenerRegistration = firestore.collection("users")
            .orderBy("nickname")
            .startAt(searchQuery)
            .endAt(searchQuery + "\uf8ff")
            .limit(10)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val users = snapshot?.toObjects(User::class.java) ?: emptyList<User>()
                trySend(users)
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieves a user by their unique identifier with caching support.
     *
     * This method first checks the global cache for the user data. If found and valid,
     * it returns the cached data immediately. Otherwise, it fetches the user from
     * Firestore, updates the cache, and returns the fresh data.
     *
     * @param userId the unique identifier of the user to retrieve
     * @return Flow emitting the user object if found, null if user doesn't exist
     */
    override suspend fun getUserById(userId: String): Flow<User?> = flow {
        try {
            // First check cache
            val cachedUser = globalUserCache[userId]
            if (cachedUser != null && isCacheValid()) {
                emit(cachedUser)
                return@flow
            }

            // If not in cache or cache is stale, fetch from Firestore
            val userDoc = firestore.collection("users").document(userId).get().await()
            if (userDoc.exists()) {
                val user = userDoc.toObject(User::class.java)
                if (user != null) {
                    globalUserCache[userId] = user // Add to cache
                    updateCacheTimestamp()
                }
                emit(user)
            } else {
                emit(null) // user not found
            }
        } catch (e: Exception) {
            emit(null)
        }
    }

    /**
     * Retrieves the current user's profile with real-time updates.
     *
     * This method first checks the local user profile cache. If no cached data is found,
     * it sets up a real-time listener on the current user's Firestore document to receive
     * updates automatically. The profile data is cached locally for improved performance.
     *
     * @return Flow emitting the current user's profile, null if user is not authenticated
     */
    override suspend fun retrieveCurrentUserProfile(): Flow<User?> = callbackFlow {
        val currentUser = UserProfileCache.getUserFromCache()
        if (currentUser != null) {
            trySend(currentUser)
        } else {
            val firebaseUser = authRepository.get().getCurrentUser()
            if (firebaseUser != null) {
                firestore.collection("users").document(firebaseUser.uid).addSnapshotListener{ snapshot, exception ->
                    if(exception!= null) {
                        close(exception)
                        trySend(null)
                        return@addSnapshotListener
                    }
                    val user = snapshot?.toObject(User::class.java)
                    UserProfileCache.saveUserToCache(user ?: User())
                    trySend(user)
                }
            } else {
                trySend(null)
            }
        }
        awaitClose()
    }

    /**
     * Retrieves the nickname of a user by their ID.
     *
     * This is a convenience method that fetches a user and returns only their nickname.
     * It uses the cached getUserById method for optimal performance.
     *
     * @param userId the unique identifier of the user
     * @return the user's nickname if found, null otherwise
     */
    override suspend fun getUserNicknameById(userId: String): String? {
        return getUserById(userId).firstOrNull()?.nickname
    }

    /**
     * Creates a basic user profile for the currently authenticated user.
     *
     * This method creates a minimal user profile using the information available
     * from Firebase Authentication. It's typically called during the initial
     * user registration process before the user completes their profile.
     *
     * @return Flow emitting UserResult with the created user profile or error information
     */
    override suspend fun basicSaveUser(): Flow<UserResult<User>> =
        flow {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                emit(UserResult.Error("Failed to get current user."))
                return@flow
            }
            createUserProfile(firebaseUser).collect { emit(it) }
        }

    /**
     * Updates a user's profile picture with image compression and cloud storage.
     *
     * This method handles the complete process of updating a user's profile picture:
     * - Compresses the image to reduce file size
     * - Uploads to Firebase Storage
     * - Updates the user document in Firestore with the new image URL
     *
     * @param uid the user's unique identifier
     * @param newProfileImageUrl the local URI of the new profile image
     * @param context Android context for content resolver operations
     * @return Flow emitting true if update was successful, false otherwise
     */
    override suspend fun updateUserProfilePicture(
        uid: String,
        newProfileImageUrl: String,
        context: Context
    ): Flow<Boolean> = flow{
        try{

            val userDoc = firestore.collection("users").document(uid).get().await()
            saveProfilePicture(uid,newProfileImageUrl,context).collect{
                when(it){
                    is UserResult.Error -> {
                        emit(false)
                        return@collect
                    }
                    is UserResult.Loading -> {}
                    is UserResult.Success -> {
                        val updates = hashMapOf<String, Any>(
                            "profileImageUrl" to it.data
                        )
                        firestore.collection("users").document(userDoc.id)
                            .update(updates)
                            .await()

                        emit(true)

                    }
                }
            }
        }catch (e: Exception){
            emit(false)
        }
    }

    /**
     * Updates a user's full name and bio information.
     *
     * This method allows users to update their display name and biographical
     * information without affecting other profile fields.
     *
     * @param uid the user's unique identifier
     * @param fullName the user's new full name
     * @param bio the user's new biographical information
     * @return Flow emitting true if update was successful, false otherwise
     */
    override suspend fun updateFullNameAndBio(
        uid: String,
        fullName: String,
        bio: String
    ): Flow<Boolean> = flow {
        try {
            val userDoc = firestore.collection("users").document(uid).get().await()

            val updates = hashMapOf<String, Any>(
                "fullName" to fullName,
                "bio" to bio
            )

            firestore.collection("users").document(userDoc.id)
                .update(updates)
                .await()

            emit(true)
        } catch (e: Exception) {
            emit(false)
        }
    }

    /**
     * Completes the user profile setup with all required information.
     *
     * This method performs a comprehensive profile update including:
     * - Profile picture upload and compression
     * - Full name, nickname, and bio updates
     * - FCM token registration for push notifications
     * - Profile completion status update
     *
     * The method handles both new users and existing users by checking for
     * existing documents and updating them appropriately.
     *
     * @param uid the user's unique identifier
     * @param fullName the user's full display name
     * @param nickname the user's unique nickname
     * @param bio optional biographical information
     * @param profileImageUrl optional profile image URI
     * @param context Android context for image processing
     * @param fcmToken FCM token for push notifications
     * @return Flow emitting UserResult with the updated user profile or error information
     */
    override suspend fun completeSaveUser(
        uid: String,
        fullName: String,
        nickname: String,
        bio: String?,
        profileImageUrl: String?,
        context: Context,
        fcmToken: String
    ): Flow<UserResult<User>> = flow {
        emit(UserResult.Loading)
        try {
            // Try to find the user by UID first
            var userDoc = firestore.collection("users").document(uid).get().await()

            // If user document doesn't exist by direct ID lookup, try by email query as fallback
            if (!userDoc.exists()) {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userQuery = firestore.collection("users")
                        .whereEqualTo("email", currentUser.email)
                        .get()
                        .await()

                    if (userQuery.documents.isNotEmpty()) {
                        userDoc = userQuery.documents[0]
                    } else {
                        emit(UserResult.Error("User not found"))
                        return@flow
                    }
                } else {
                    emit(UserResult.Error("Current user is null"))
                    return@flow
                }
            }
            saveProfilePicture(uid,profileImageUrl.toString(),context).collect{
                when(it){
                    is UserResult.Error -> {
                        emit(UserResult.Error(it.message))
                        return@collect
                    }
                    is UserResult.Loading -> {}
                    is UserResult.Success -> {


                        // Create updated user object
                        val email = userDoc.getString("email") ?: ""

                        val updatedUser = User(
                            id = uid,
                            email = email,
                            fullName = fullName,
                            nickname = nickname,
                            bio = bio ?: "",
                            profileImageUrl = it.data,
                            isOnline = true,
                            isProfileComplete = true,
                            fcmToken = fcmToken
                        )

                        // Update the document
                        firestore.collection("users").document(userDoc.id)
                            .set(updatedUser)
                            .await()

                        emit(UserResult.Success(updatedUser))

                    }
                }
            }

        } catch (e: Exception) {
            emit(UserResult.Error(e.message ?: "Error updating user"))
        }
    }

    /**
     * Checks if a nickname is available for use.
     *
     * This method queries the nicknames collection to determine if a given
     * nickname is already taken by another user. Nicknames must be unique
     * across the entire application.
     *
     * @param nickname the nickname to check for availability
     * @return Flow emitting UserResult with boolean indicating availability or error information
     */
    override suspend fun isNickNameAvailable(nickname: String): Flow<UserResult<Boolean>> = flow {
        emit(UserResult.Loading)
        try {
            val nicknameDoc = firestore.collection("nicknames").document(nickname).get().await()
            if (!nicknameDoc.exists()) {
                emit(UserResult.Success(true))
            } else {
                emit(UserResult.Error("Nickname is already taken"))
            }
        } catch (e: Exception) {
            emit(UserResult.Error(e.message ?: "Error checking nickname availability"))
        }
    }

    /**
     * Saves a nickname reservation for a specific user.
     *
     * This method reserves a nickname in the nicknames collection, ensuring
     * that each nickname can only be used by one user. It includes validation
     * to prevent nickname theft by checking existing ownership.
     *
     * @param nickname the nickname to reserve
     * @param uid the user ID who will own this nickname
     * @return Flow emitting UserResult indicating success or error information
     */
    override suspend fun saveNickname(nickname: String, uid: String): Flow<UserResult<Unit>> =
        flow {
            emit(UserResult.Loading)
            try {
                // First check if the nickname already exists
                val nicknameDoc = firestore.collection("nicknames").document(nickname).get().await()
                if (nicknameDoc.exists()) {
                    // If it exists and belongs to a different user, return an error
                    val existingUid = nicknameDoc.getString("uid")
                    if (existingUid != null && existingUid != uid) {
                        emit(UserResult.Error("Nickname is already taken by another user"))
                        return@flow
                    }
                }

                // Save the nickname
                firestore.collection("nicknames").document(nickname)
                    .set(mapOf("nickname" to nickname, "uid" to uid)).await()
                emit(UserResult.Success(Unit))
            } catch (e: Exception) {
                emit(UserResult.Error(e.message ?: "Nickname save failed"))
            }
        }

    /**
     * Saves and compresses a profile picture to Firebase Storage.
     *
     * This method handles the complete profile picture upload process:
     * - Reads the image from the provided URI
     * - Compresses the image to JPEG format with 60% quality
     * - Uploads the compressed image to Firebase Storage
     * - Returns the download URL for the uploaded image
     *
     * The compression reduces file size while maintaining acceptable image quality
     * for profile pictures.
     *
     * @param uid the user's unique identifier (used in storage path)
     * @param profileImageUrl the local URI of the image to upload
     * @param context Android context for content resolver operations
     * @return Flow emitting UserResult with the download URL or error information
     */
    override suspend fun saveProfilePicture(
        uid: String,
        profileImageUrl: String,
        context: Context
    ): Flow<UserResult<String>> = flow {
        emit(UserResult.Loading)
        try {
            if (profileImageUrl.isBlank()) {

                emit(UserResult.Success(""))

                return@flow
            }

            val inputStream = context.contentResolver.openInputStream(profileImageUrl.toUri())
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (originalBitmap == null) {
                emit(UserResult.Success(""))
                return@flow
            }

            val outputStream = ByteArrayOutputStream()
            originalBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val compressedData = outputStream.toByteArray()

            val ref = storage.reference.child("profile_pictures/$uid.jpg")
            val uploadTask = ref.putBytes(compressedData).await()

            val downloadUrl = uploadTask.metadata?.reference?.downloadUrl?.await()?.toString()
                ?: throw IllegalStateException("Download URL not available after upload")

            emit(UserResult.Success(downloadUrl))
        } catch (e: StorageException) {
            emit(UserResult.Error("Storage error: ${e.message}"))
        } catch (e: Exception) {
            emit(UserResult.Error("Unexpected error: ${e.message}"))
        }
    }
        .flowOn(Dispatchers.IO)

    /**
     * Checks if a user's profile setup is complete.
     *
     * This method verifies whether a user has completed their initial profile
     * setup by checking the isProfileComplete flag in their Firestore document.
     * This is useful for determining whether to show onboarding flows.
     *
     * @param uid the user's unique identifier
     * @return Flow emitting UserResult with boolean indicating completion status or error information
     */
    override suspend fun isProfileCompleted(uid: String): Flow<UserResult<Boolean>> = flow {
        emit(UserResult.Loading)
        try {
            val userDoc = firestore.collection("users").document(uid).get().await()
            if (userDoc.exists()) {
                val isProfileComplete = userDoc.getBoolean("isProfileComplete") ?: false
                emit(UserResult.Success(isProfileComplete))
            } else {
                emit(UserResult.Error("User not found"))
            }
        } catch (e: Exception) {
            emit(UserResult.Error(e.message ?: "Error checking profile completion"))
        }
    }

    /**
     * Creates an initial user profile from Firebase Authentication data.
     *
     * This method creates a basic user profile using information available from
     * Firebase Authentication (email, display name, photo URL). The profile is
     * marked as incomplete, requiring the user to provide additional information
     * like nickname and bio to complete their setup.
     *
     * @param firebaseUser the Firebase user object containing authentication data
     * @return Flow emitting UserResult with the created user profile or error information
     */
    override suspend fun createUserProfile(firebaseUser: FirebaseUser): Flow<UserResult<User>> = flow {
        emit(UserResult.Loading)
        try {
            val newUserProfile = User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                fullName = firebaseUser.displayName ?: "",
                nickname = firebaseUser.email?.split("@")?.get(0) ?: "user_${firebaseUser.uid.take(5)}",
                profileImageUrl = firebaseUser.photoUrl?.toString(),
                isProfileComplete = false
            )
            firestore.collection("users").document(firebaseUser.uid).set(newUserProfile).await()

            emit(UserResult.Success(newUserProfile))

        } catch (e: Exception) {
            emit(UserResult.Error(e.message ?: "User profile creation failed."))
        }
    }
}
