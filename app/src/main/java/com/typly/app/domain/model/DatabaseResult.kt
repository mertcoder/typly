package com.typly.app.domain.model

/**
 * Sealed class representing the result of database operations.
 *
 * This class provides a type-safe way to handle different states of database
 * operations including CRUD operations, queries, and data synchronization.
 * It ensures consistent error handling across all database interactions.
 *
 * @param T the type of data returned on successful database operation
 */
sealed class DatabaseResult<out T> {

    /**
     * Represents a successful database operation.
     *
     * @param data the result data of the successful database operation
     */
    data class Success<T>(val data: T) : DatabaseResult<T>()

    /**
     * Represents a failed database operation.
     *
     * @param message descriptive error message explaining why the database operation failed
     */
    data class Error(val message: String) : DatabaseResult<Nothing>()

    /**
     * Represents an ongoing database operation.
     *
     * This state indicates that a database request is currently being processed
     * and the result is not yet available.
     */
    data object Loading : DatabaseResult<Nothing>()
}
