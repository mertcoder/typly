package com.typly.app.domain.repository

import com.typly.app.domain.model.AuthResult
import com.typly.app.data.remote.dto.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for authentication operations.
 *
 * This interface defines the contract for authentication-related operations including
 * user registration, login, logout, and authentication state management. It provides
 * multiple authentication methods (email/password, Google Sign-In, anonymous) and
 * integrates with Firebase Authentication services.
 *
 * All authentication operations return Flow objects to support reactive programming
 * patterns and provide real-time updates on authentication state changes.
 */
interface AuthRepository {

    /**
     * Authenticates a user using email and password credentials.
     *
     * This method performs traditional email/password authentication through
     * Firebase Authentication. It validates the provided credentials and
     * returns the authenticated FirebaseUser object on success.
     *
     * @param email the user's email address
     * @param password the user's password
     * @return Flow emitting AuthResult containing FirebaseUser on success or error information
     */
    suspend fun login(email: String, password: String): Flow<AuthResult<FirebaseUser?>>

    /**
     * Registers a new user with email and password credentials.
     *
     * This method creates a new user account using email/password authentication
     * through Firebase Authentication. After successful registration, it creates
     * a corresponding User profile in the application database with basic information
     * derived from the Firebase user data.
     *
     * @param email the new user's email address
     * @param password the new user's chosen password
     * @return Flow emitting AuthResult containing the created User profile or error information
     */
    suspend fun basicRegister(email: String, password: String): Flow<AuthResult<User>>

    /**
     * Authenticates a user using Google Sign-In.
     *
     * This method performs OAuth 2.0 authentication using Google Sign-In SDK.
     * It takes an ID token obtained from the Google Sign-In flow and uses it
     * to authenticate with Firebase Authentication. If successful, it creates
     * or retrieves the corresponding User profile in the application database.
     *
     * @param idToken the Google ID token obtained from Google Sign-In SDK
     * @return Flow emitting AuthResult containing the User profile or error information
     */
    suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<User>>

    /**
     * Authenticates a user anonymously without requiring credentials.
     *
     * This method creates an anonymous user session through Firebase Authentication.
     * Anonymous users can use the application with limited functionality and can
     * later upgrade their account by linking it to permanent credentials.
     * A temporary User profile is created for the anonymous session.
     *
     * @return Flow emitting AuthResult containing the anonymous User profile or error information
     */
    suspend fun loginAnonymously(): Flow<AuthResult<User>>

    /**
     * Signs out the currently authenticated user.
     *
     * This method terminates the current user session by signing out from
     * Firebase Authentication. It clears all authentication state and
     * redirects the user to the authentication flow.
     */
    suspend fun logout()

    /**
     * Checks if a user is currently authenticated.
     *
     * This method provides a synchronous way to check the current authentication
     * state without triggering any network operations. It returns true if there
     * is a valid authenticated user session.
     *
     * @return true if a user is currently authenticated, false otherwise
     */
    fun isUserAuthenticated(): Boolean

    /**
     * Retrieves the currently authenticated Firebase user.
     *
     * This method returns the current FirebaseUser object if a user is
     * authenticated, or null if no user is signed in. The FirebaseUser
     * contains basic authentication information like UID, email, and display name.
     *
     * @return the current FirebaseUser if authenticated, null otherwise
     */
    fun getCurrentUser(): FirebaseUser?

    /**
     * Resets the current user authentication state.
     *
     * This method clears any cached authentication state and forces a
     * refresh of the current user information. It's useful for handling
     * authentication state inconsistencies or when user data needs to be
     * refreshed from the server.
     *
     * @return true if the reset operation was successful, false otherwise
     */
    fun resetCurrentUser(): Boolean
}
