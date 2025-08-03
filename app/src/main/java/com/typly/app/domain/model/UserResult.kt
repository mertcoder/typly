package com.typly.app.domain.model

/**
 * Sealed class representing the result of user-related operations.
 *
 * This class provides a type-safe way to handle different states of user operations
 * such as profile creation, updates, retrieval, and other user management tasks.
 * It follows the common Result pattern used throughout the application for
 * consistent async operation handling.
 *
 * @param T the type of data returned on successful user operation
 */
sealed class UserResult<out T> {

    /**
     * Represents a successful user operation.
     *
     * @param data the result data of the successful user operation
     */
    data class Success<T>(val data: T) : UserResult<T>()

    /**
     * Represents a failed user operation.
     *
     * @param message descriptive error message explaining why the user operation failed
     */
    data class Error(val message: String) : UserResult<Nothing>()

    /**
     * Represents an ongoing user operation.
     *
     * This state indicates that a user-related request is currently being processed
     * and the result is not yet available.
     */
    data object Loading : UserResult<Nothing>()
}
