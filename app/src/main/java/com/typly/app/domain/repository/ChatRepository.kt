package com.typly.app.domain.repository

import android.content.Context
import android.net.Uri
import com.typly.app.presentation.components.ChatPreview
import com.typly.app.data.remote.dto.Chat
import com.typly.app.data.remote.dto.Message
import com.typly.app.domain.model.ChatResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing chat and messaging operations.
 *
 * This interface defines the contract for chat-related operations including
 * chat creation, message sending and retrieval, typing indicators, and chat
 * preview generation. It supports both text and image messages and provides
 * real-time messaging capabilities through reactive Flow streams.
 *
 * The repository manages the complete messaging lifecycle from chat creation
 * to message delivery and read status tracking.
 */
interface ChatRepository {

    /**
     * Creates a new chat between specified participants.
     *
     * This method initializes a new chat conversation with the provided participants.
     * It creates the necessary chat record in the database and returns the Chat
     * object that can be used for sending messages.
     *
     * @param participants list of user IDs who will participate in the chat
     * @return the created Chat object with all necessary metadata
     */
    suspend fun createChat(participants: List<String>): Chat

    /**
     * Sends a message in an existing chat.
     *
     * This method handles sending both text and image messages within a chat.
     * For image messages, it uploads the image to storage and includes the URL
     * in the message. It creates a Message object and updates the chat's last
     * message information.
     *
     * @param chatId the unique identifier of the chat to send the message to
     * @param content the text content of the message
     * @param senderId the unique identifier of the user sending the message
     * @param receiverId the unique identifier of the message recipient
     * @param imageUri optional URI of an image to include with the message
     * @param context Android context for image processing operations
     * @return the created Message object with all metadata
     */
    suspend fun sendMessage(
        chatId: String,
        content: String,
        senderId: String,
        receiverId: String,
        imageUri: Uri?,
        context: Context
    ): Message

    /**
     * Retrieves all chats for a specific user.
     *
     * This method fetches all chat conversations that involve the specified user,
     * ordered by the most recent activity. It provides real-time updates as
     * new chats are created or existing chats receive new messages.
     *
     * @param userId the unique identifier of the user whose chats to retrieve
     * @return Flow emitting ChatResult with list of Chat objects or error information
     */
    suspend fun getChats(userId: String): Flow<ChatResult<List<Chat>>>

    /**
     * Marks a specific message as read.
     *
     * This method updates the read status of a message, typically called when
     * the user views the message. It helps track message delivery and read
     * status for conversation participants.
     *
     * @param chatId the unique identifier of the chat containing the message
     * @param messageId the unique identifier of the message to mark as read
     * @return Flow emitting ChatResult indicating success or error information
     */
    suspend fun markMessageAsRead(chatId: String, messageId: String): Flow<ChatResult<Unit>>

    /**
     * Retrieves a specific chat by its ID with real-time updates.
     *
     * This method fetches detailed information about a specific chat and
     * provides real-time updates as the chat data changes. It's useful for
     * displaying chat details and monitoring chat state.
     *
     * @param chatId the unique identifier of the chat to retrieve
     * @return Flow emitting ChatResult with Chat object or error information
     */
    fun getChat(chatId: String): Flow<ChatResult<Chat>>

    /**
     * Finds the chat ID for a conversation between two specific users.
     *
     * This method searches for an existing chat between two users and returns
     * the chat ID if found. It's useful for determining if a conversation
     * already exists before creating a new chat.
     *
     * @param userId1 the unique identifier of the first user
     * @param userId2 the unique identifier of the second user
     * @return the chat ID if a conversation exists between the users, null otherwise
     */
    suspend fun getChatIdBetweenUsers(userId1: String, userId2: String): String?

    /**
     * Retrieves all messages between two users with real-time updates.
     *
     * This method fetches the complete message history between two users,
     * ordered chronologically. It provides real-time updates as new messages
     * are sent, enabling live conversation views.
     *
     * @param userId1 the unique identifier of the first user
     * @param userId2 the unique identifier of the second user
     * @return Flow emitting list of Message objects in chronological order
     */
    suspend fun retrieveAllMessages(userId1: String, userId2: String): Flow<List<Message>>

    /**
     * Retrieves chat preview information for the current user.
     *
     * This method generates a list of chat previews containing essential
     * information like last message, participant names, timestamps, and
     * unread message counts. It's optimized for displaying chat lists.
     *
     * @return Flow emitting list of ChatPreview objects for UI display
     */
    suspend fun retrieveChatPreviews(): Flow<List<ChatPreview>>

    /**
     * Updates the typing status of a user in a specific chat.
     *
     * This method manages typing indicators by updating the user's typing
     * status in a chat. It enables real-time typing notifications to be
     * displayed to other chat participants.
     *
     * @param chatId the unique identifier of the chat
     * @param userId the unique identifier of the user whose typing status to update
     * @param isTyping true if the user is currently typing, false otherwise
     */
    suspend fun updateUserTypingStatus(chatId: String, userId: String, isTyping: Boolean)
}
