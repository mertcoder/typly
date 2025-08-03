package com.typly.app.data.remote.service

import com.typly.app.domain.model.AuthResult
import com.typly.app.data.remote.dto.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

/**
 * Service interface for authentication-related operations.
 * This is a higher-level abstraction that can combine multiple repositories
 * or data sources for complex authentication flows.
 */
interface AuthService {
    /**
     * Authenticates a user with email and password
     */
    suspend fun authenticateWithCredentials(email: String, password: String): Flow<AuthResult<FirebaseUser?>>

    /**
     * Registers a new user with email, password, and profile information
     */
    //suspend fun completeRegisterUser(uid: String , fullName: String, nickname: String, bio: String?, profileImageUrl: String?, context: Context): Flow<AuthResult<User>>
    suspend fun basicRegisterUser(email: String, password: String): Flow<AuthResult<User>>

    /**
     * Signs in anonymously
     */
    suspend fun authenticateAnonymously(): Flow<AuthResult<User>>
    
    /**
     * Signs out the current user
     */
    suspend fun signOut()
    
    /**
     * Gets the current authenticated user
     */
    fun getCurrentUser(): FirebaseUser?

    /**
     * Observes authentication state changes
     */

    /**
     * Checks if the current user is authenticated
     */
    fun isAuthenticated(): Boolean
    
    /**
     * Updates the user's profile information
     */
    suspend fun updateUserProfile(updatedUser: User): Flow<AuthResult<User>>
    
    /**
     * Links an anonymous account to a permanent account
     */
    suspend fun linkAnonymousAccount(email: String, password: String): Flow<AuthResult<User>>
    
    /**
     * Sends a password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Flow<AuthResult<Unit>>
    
    /**
     * Verifies a user's email
     */
    suspend fun verifyEmail(): Flow<AuthResult<Unit>>
} 
