package com.typly.app.domain.model

import com.typly.app.data.remote.dto.User
import com.google.firebase.auth.FirebaseUser

/**
 * Data class representing the authentication state of a logged-in user.
 *
 * This class encapsulates the complete authentication state including both
 * Firebase authentication data and application-specific user data. It handles
 * different login types and tracks the state of both Firebase user authentication
 * and custom user profile data retrieval.
 *
 * @param firebaseUser the result of Firebase authentication operation, containing FirebaseUser data
 * @param userForGoogleUser the result of retrieving application-specific user data, particularly for Google Sign-In users
 * @param loginType the method used for authentication (Basic email/password or Google Sign-In)
 */
data class LoginAuthStateUser(
    val firebaseUser: AuthResult<FirebaseUser?>? = null,
    val userForGoogleUser: AuthResult<User?>? = null,
    val loginType: LoginType
)
