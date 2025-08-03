package com.typly.app.data.repository

import com.typly.app.domain.model.AuthResult
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.model.UserResult
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [AuthRepository] that handles user authentication using Firebase.
 *
 * @property firebaseAuth Firebase authentication instance.
 * @property userRepository Reference to user repository for user-related operations.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userRepository: UserRepository,
) : AuthRepository {

    // Holds the current authenticated Firebase user
    private var _currentUser = firebaseAuth.currentUser

    /**
     * Logs in the user with the given email and password.
     *
     * @param email The user's email address.
     * @param password The user's password.
     * @return A flow emitting the result of the login operation.
     */
    override suspend fun login(email: String, password: String): Flow<AuthResult<FirebaseUser?>> = flow {
        try {
            emit(AuthResult.Loading)
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                emit(AuthResult.Success(result.user))
            } else {
                emit(AuthResult.Error("User not found."))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.localizedMessage ?: "User not found."))
        }
    }

    /**
     * Registers a new user using email and password.
     *
     * @param email The email address to register with.
     * @param password The password to register with.
     * @return A flow emitting the result of the registration operation.
     */
    override suspend fun basicRegister(email: String, password: String): Flow<AuthResult<User>> = flow {
        emit(AuthResult.Loading)
        try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                val newUser = User(
                    id = user.uid,
                    email = user.email!!,
                    fullName = "",
                    nickname = "",
                )
                emit(AuthResult.Success(newUser))
            } else {
                emit(AuthResult.Error("User creation failed."))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.localizedMessage ?: "An error occurred."))
        }
    }

    /**
     * Signs in the user using Google authentication.
     *
     * @param idToken The ID token obtained from Google Sign-In.
     * @return A flow emitting the result of the sign-in operation.
     */
    override suspend fun signInWithGoogle(idToken: String): Flow<AuthResult<User>> = flow {
        emit(AuthResult.Loading)
        try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            val firebaseUser = authResult.user
            if (firebaseUser != null) {
                val isNewUser = authResult.additionalUserInfo?.isNewUser ?: false
                val userProfileFlow: Flow<AuthResult<User>> = if (isNewUser) {
                    userRepository.createUserProfile(firebaseUser)
                        .map { result ->
                            when (result) {
                                is UserResult.Success -> AuthResult.Success(result.data)
                                is UserResult.Error -> AuthResult.Error(result.message)
                                is UserResult.Loading -> AuthResult.Loading
                            }
                        }
                } else {
                    userRepository.getUserById(firebaseUser.uid)
                        .map { user ->
                            if (user != null) {
                                AuthResult.Success(user)
                            } else {
                                AuthResult.Error("User not found.")
                            }
                        }
                }
                emitAll(userProfileFlow)
            } else {
                emit(AuthResult.Error("Google authentication failed."))
            }
        } catch (e: Exception) {
            emit(AuthResult.Error(e.localizedMessage ?: "Google authentication failed."))
        }
    }

    /**
     * Logs in anonymously by generating a mock anonymous user.
     *
     * @return A flow emitting the result of the anonymous login.
     */
    override suspend fun loginAnonymously(): Flow<AuthResult<User>> = flow {
        emit(AuthResult.Loading)

        // Simulate network delay
        delay(500)

        val anonymousUser = User(
            id = UUID.randomUUID().toString(),
            isAnonymous = true
        )

        emit(AuthResult.Success(anonymousUser))
    }

    /**
     * Logs out the current user and resets the local user reference.
     */
    override suspend fun logout() {
        firebaseAuth.signOut()
        resetCurrentUser()
    }

    /**
     * Checks whether there is an authenticated Firebase user.
     *
     * @return True if a user is authenticated, false otherwise.
     */
    override fun isUserAuthenticated(): Boolean {
        return firebaseAuth.currentUser != null
    }

    /**
     * Retrieves the currently authenticated Firebase user.
     *
     * @return The current Firebase user, or null if none is logged in.
     */
    override fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Resets the local reference to the current user.
     *
     * @return Always returns true after resetting.
     */
    override fun resetCurrentUser(): Boolean {
        _currentUser = null
        return true
    }
}
