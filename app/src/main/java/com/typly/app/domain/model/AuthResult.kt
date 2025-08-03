
package com.typly.app.domain.model

/**
 * Sealed class representing the result of authentication operations.
 *
 * This class provides a type-safe way to handle different states of authentication
 * operations, including success, error, and loading states. It follows the common
 * Result pattern used throughout the application for async operations.
 *
 * @param T the type of data returned on successful authentication
 */
sealed class AuthResult<out T> {

    /**
     * Represents a successful authentication operation.
     *
     * @param data the result data of the successful authentication operation
     */
    data class Success<T>(val data: T) : AuthResult<T>()

    /**
     * Represents a failed authentication operation.
     *
     * @param message descriptive error message explaining why the authentication failed
     */
    data class Error(val message: String) : AuthResult<Nothing>()

    /**
     * Represents an ongoing authentication operation.
     *
     * This state indicates that an authentication request is currently being processed
     * and the result is not yet available.
     */
    data object Loading : AuthResult<Nothing>()
}
