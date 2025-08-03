package com.typly.app.domain.model

/**
 * Sealed class representing the result of chat-related operations.
 *
 * This class provides a type-safe way to handle different states of chat operations
 * such as message sending, chat creation, or chat data retrieval. It follows the
 * common Result pattern for consistent error handling across chat functionality.
 *
 * @param T the type of data returned on successful chat operation
 */
sealed class ChatResult<out T> {

    /**
     * Represents a successful chat operation.
     *
     * @param data the result data of the successful chat operation
     */
    data class Success<T>(val data: T) : ChatResult<T>()

    /**
     * Represents a failed chat operation.
     *
     * @param message descriptive error message explaining why the chat operation failed
     */
    data class Error(val message: String) : ChatResult<Nothing>()

    /**
     * Represents an ongoing chat operation.
     *
     * This state indicates that a chat request is currently being processed
     * and the result is not yet available.
     */
    data object Loading : ChatResult<Nothing>()
}
