package com.typly.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import com.typly.app.presentation.components.ChatPreview
import com.typly.app.data.remote.dto.Chat
import com.typly.app.data.remote.dto.Message
import com.typly.app.data.remote.dto.MessageStatus
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.model.ChatResult
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.ChatRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of [ChatRepository] providing chat and messaging functionality.
 *
 * This class serves as the single source of truth for all chat-related data. It handles
 * chat creation, sending text and media messages, retrieving message history, and listening
 * for real-time updates using Firebase Firestore and Firebase Storage. It also interacts
 * with a user cache to optimize performance.
 *
 * @property firestore The Firebase Firestore instance for database operations.
 * @property authRepository The repository for accessing current user authentication data.
 * @property userRepo The repository for user-related operations, including a global user cache.
 * @property storage The Firebase Storage instance for media file uploads.
 */
@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val authRepository: AuthRepository,
    private val userRepo: UserRepositoryImpl,
    private val storage: FirebaseStorage
) : ChatRepository {

    companion object {
        private const val TAG = "ChatRepositoryImpl"
    }

    /**
     * Generates a consistent, sorted chat ID for two users.
     *
     * By sorting the user IDs alphabetically, it ensures that the chat ID is
     * always the same regardless of which user initiates the chat.
     *
     * @param userId1 The ID of the first user.
     * @param userId2 The ID of the second user.
     * @return A predictable, unique chat ID string in the format "chat_{sortedId1}_{sortedId2}".
     */
    private fun generateChatId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "chat_${sortedIds[0]}_${sortedIds[1]}"
    }

    /**
     * Generates a unique message ID based on the chat ID and the current timestamp.
     *
     * @param chatId The ID of the chat where the message belongs.
     * @return A unique message ID string.
     */
    private fun generateMessageId(chatId: String): String {
        return "msg_${chatId}_${System.currentTimeMillis()}"
    }

    /**
     * Creates a new chat between two participants if one does not already exist.
     * If a chat already exists, it returns the existing chat document.
     *
     * @param participants A list of user IDs, which must contain exactly two participants.
     * @return The newly created or existing [Chat] object.
     * @throws IllegalArgumentException if the participant count is not 2 or if chat creation fails.
     */
    override suspend fun createChat(participants: List<String>): Chat {
        try {
            if (participants.size != 2) {
                throw IllegalArgumentException("Only two participants are allowed")
            }

            val chatId = generateChatId(participants[0], participants[1])

            val existingChat = firestore.collection("chats").document(chatId).get().await()
            if (existingChat.exists()) {
                val chat = existingChat.toObject(Chat::class.java)
                if (chat != null) {
                    Log.d(TAG, "Chat already exists, returning existing chat: $chatId")
                    return chat
                }
            }

            Log.d(TAG, "Creating new chat: $chatId")
            val chat = Chat(
                id = chatId,
                participants = participants,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                typingStatus = mapOf(participants[0] to false, participants[1] to false)
            )

            // Save the chat document
            firestore.collection("chats").document(chatId).set(chat).await()

            // Update the recentChats list for each participant
            participants.forEach { participantId ->
                firestore.collection("users").document(participantId)
                    .update("recentChats", FieldValue.arrayUnion(chatId))
                    .await()
            }

            return chat
        } catch (e: Exception) {
            Log.e(TAG, "Chat creation failed", e)
            throw IllegalArgumentException("Chat creation failed: ${e.message}")
        }
    }

    /**
     * Sends a message within a specified chat. Supports both text and image messages.
     *
     * If an `imageUri` is provided, the image is compressed, uploaded to Firebase Storage,
     * and the resulting URL is stored in the message's `media` field. Otherwise, a text-only
     * message is sent. After sending, it updates the parent chat's `lastMessage` field.
     *
     * @param chatId The ID of the chat to send the message to.
     * @param content The text content of the message (can be a caption for an image).
     * @param senderId The ID of the user sending the message.
     * @param receiverId The ID of the user receiving the message.
     * @param imageUri An optional [Uri] for an image to be sent. If null, a text message is sent.
     * @param context The [Context] required for processing the image URI.
     * @return The sent [Message] object.
     * @throws IllegalArgumentException if message sending fails.
     */
    override suspend fun sendMessage(
        chatId: String,
        content: String,
        senderId: String,
        receiverId: String,
        imageUri: Uri?,
        context: Context
    ): Message {
        if (imageUri == null) {
            // Text-only message logic
            try {
                val messageId = generateMessageId(chatId)
                val message = Message(
                    id = messageId,
                    chatId = chatId,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    status = MessageStatus.SENT
                )

                val chatRef = firestore.collection("chats").document(chatId)
                val messageRef = chatRef.collection("messages").document(messageId)
                messageRef.set(message).await()

                chatRef.update(
                    mapOf(
                        "lastMessage" to message,
                        "updatedAt" to System.currentTimeMillis(),
                        "participants" to listOf(senderId, receiverId)
                    )
                ).await()

                firestore.collection("users").document(receiverId)
                    .update("recentChats", FieldValue.arrayUnion(chatId))
                    .await()

                return message
            } catch (e: Exception) {
                Log.e(TAG, "Text message sending failed for chatId: $chatId", e)
                throw IllegalArgumentException("Message sending failed: ${e.message}")
            }
        } else {
            // Image message logic
            try {
                Log.d(TAG, "Processing image message for chatId: $chatId")

                val inputStream = context.contentResolver.openInputStream(imageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (originalBitmap == null) throw IllegalArgumentException("Failed to decode image from Uri.")

                val outputStream = ByteArrayOutputStream()
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
                val compressedData = outputStream.toByteArray()
                Log.d(TAG, "Image compressed, new size: ${compressedData.size} bytes")

                val timestamp = System.currentTimeMillis()
                val ref = storage.reference.child("message_images/${senderId}_${timestamp}.jpg")
                val uploadTask = ref.putBytes(compressedData).await()
                val downloadUrl = uploadTask.metadata?.reference?.downloadUrl?.await()?.toString()
                    ?: throw IllegalStateException("Failed to get download URL after upload.")

                Log.d(TAG, "Image uploaded successfully: $downloadUrl")

                val messageId = generateMessageId(chatId)
                val message = Message(
                    id = messageId,
                    chatId = chatId,
                    senderId = senderId,
                    receiverId = receiverId,
                    content = content,
                    media = downloadUrl,
                    timestamp = System.currentTimeMillis(),
                    isRead = false,
                    status = MessageStatus.SENT
                )

                val chatRef = firestore.collection("chats").document(chatId)
                val messageRef = chatRef.collection("messages").document(messageId)
                messageRef.set(message).await()

                chatRef.update(
                    mapOf(
                        "lastMessage" to message,
                        "updatedAt" to System.currentTimeMillis(),
                        "participants" to listOf(senderId, receiverId)
                    )
                ).await()

                firestore.collection("users").document(receiverId)
                    .update("recentChats", FieldValue.arrayUnion(chatId))
                    .await()

                Log.d(TAG, "Image message sent successfully: $messageId")
                return message
            } catch (e: Exception) {
                Log.e(TAG, "Image message sending failed for chatId: $chatId", e)
                throw IllegalArgumentException("Image message sending failed: ${e.message}")
            }
        }
    }

    /**
     * Fetches a list of chat documents based on the user's `recentChats` field.
     * This is a one-time fetch and does not listen for real-time updates.
     *
     * @param userId The ID of the current user.
     * @return A [Flow] emitting [ChatResult] which contains a list of [Chat] objects on success.
     */
    override suspend fun getChats(userId: String): Flow<ChatResult<List<Chat>>> = flow {
        emit(ChatResult.Loading)
        try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val recentChats = userDoc.get("recentChats") as? List<String> ?: emptyList()

            if (recentChats.isEmpty()) {
                emit(ChatResult.Success(emptyList()))
                return@flow
            }

            val chats = mutableListOf<Chat>()
            for (chatId in recentChats) {
                val chatDoc = firestore.collection("chats").document(chatId).get().await()
                chatDoc.toObject(Chat::class.java)?.let { chats.add(it) }
            }

            emit(ChatResult.Success(chats))
        } catch (e: Exception) {
            Log.e(TAG, "Could not retrieve chats for user: $userId", e)
            emit(ChatResult.Error(e.message ?: "Chats could not be retrieved"))
        }
    }

    /**
     * Marks a specific message as read in Firestore.
     *
     * @param chatId The ID of the chat containing the message.
     * @param messageId The ID of the message to be marked as read.
     * @return A [Flow] emitting [ChatResult] indicating the outcome of the operation.
     */
    override suspend fun markMessageAsRead(
        chatId: String,
        messageId: String
    ): Flow<ChatResult<Unit>> = flow {
        emit(ChatResult.Loading)
        try {
            firestore.collection("chats").document(chatId).collection("messages").document(messageId)
                .update("isRead", true, "status", MessageStatus.READ)
                .await()
            emit(ChatResult.Success(Unit))
        } catch (e: Exception) {
            Log.w(TAG, "Message read status could not be updated for message: $messageId", e)
            emit(ChatResult.Error(e.message ?: "Message read status could not be updated"))
        }
    }

    /**
     * Listens for real-time updates to a single chat document.
     *
     * @param chatId The ID of the chat document to listen to.
     * @return A [Flow] via `callbackFlow` that emits a [ChatResult] with the updated [Chat]
     * object whenever the document changes in Firestore.
     */
    override fun getChat(chatId: String): Flow<ChatResult<Chat>> = callbackFlow {
        // Register the snapshot listener
        val listener = firestore.collection("chats").document(chatId)
            .addSnapshotListener { snapshot, error ->
                // If there's an error, close the Flow with an error
                if (error != null) {
                    trySend(ChatResult.Error(error.message ?: "Could not listen to chat"))
                    close(error)
                    return@addSnapshotListener
                }

                // If the document exists, convert it to a Chat object and send it to the Flow
                if (snapshot != null && snapshot.exists()) {
                    val chat = snapshot.toObject(Chat::class.java)
                    if (chat != null) {
                        trySend(ChatResult.Success(chat))
                    } else {
                        trySend(ChatResult.Error("Failed to parse chat data"))
                    }
                } else {
                    trySend(ChatResult.Error("Chat not found"))
                }
            }

        // When the Flow is cancelled (e.g., screen is closed), remove the listener
        awaitClose {
            listener.remove()
        }
    }

    /**
     * Retrieves the chat ID for a given pair of users, if a chat already exists.
     *
     * @param userId1 The ID of the first user.
     * @param userId2 The ID of the second user.
     * @return The existing `chatId` as a [String], or `null` if no chat exists.
     * @throws IllegalArgumentException on failure.
     */
    override suspend fun getChatIdBetweenUsers(
        userId1: String,
        userId2: String
    ): String? {
        return try {
            val chatId = generateChatId(userId1, userId2)
            val chatDoc = firestore.collection("chats").document(chatId).get().await()
            if (chatDoc.exists()) chatId else null
        } catch (e: Exception) {
            Log.e(TAG, "Chat check could not be performed between $userId1 and $userId2", e)
            throw IllegalArgumentException("Chat check could not be performed: ${e.message}")
        }
    }

    /**
     * Listens for real-time updates to all messages between two users.
     *
     * @param userId1 The ID of the current user.
     * @param userId2 The ID of the other user.
     * @return A [Flow] that emits the complete list of [Message] objects whenever there is a change.
     */
    override suspend fun retrieveAllMessages(
        userId1: String,
        userId2: String
    ): Flow<List<Message>> = callbackFlow {
        val chatId = generateChatId(userId1, userId2)
        val listenerRegistration = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    close(exception)
                    return@addSnapshotListener
                }
                val allMessageList = snapshot?.toObjects(Message::class.java) ?: emptyList()
                trySend(allMessageList)
            }
        awaitClose { listenerRegistration.remove() }
    }

    /**
     * Retrieves and listens for real-time updates for the user's chat previews.
     *
     * This complex flow performs the following steps:
     * 1. Fetches the current user's list of recent chat IDs.
     * 2. Fetches all corresponding chat documents.
     * 3. Aggregates all unique participant IDs from those chats.
     * 4. Populates a local user cache, fetching any missing user profiles from Firestore to minimize reads.
     * 5. Constructs and emits the initial list of [ChatPreview] objects.
     * 6. Sets up real-time listeners on each chat document to push updates as they happen.
     *
     * @return A [Flow] that emits an updated list of [ChatPreview] objects whenever a change occurs.
     */
    override suspend fun retrieveChatPreviews(): Flow<List<ChatPreview>> = callbackFlow {
        val currentFirebaseUser = authRepository.getCurrentUser()
        if (currentFirebaseUser == null) {
            close(Exception("Current user not found"))
            return@callbackFlow
        }

        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        val chatPreviews = mutableMapOf<String, ChatPreview>()
        val localUserCache = mutableMapOf<String, User>()
        val listeners = mutableListOf<ListenerRegistration>()

        try {
            // 1. Get the user's recent chat list
            val userDoc = firestore.collection("users").document(currentFirebaseUser.uid).get().await()
            val currentUserData = userDoc.toObject(User::class.java)

            if (currentUserData == null) {
                close(Exception("Current user data not found in Firestore"))
                return@callbackFlow
            }

            val recentChatsIdList: List<String> = currentUserData.recentChats
            if (recentChatsIdList.isEmpty()) {
                trySend(emptyList()) // User has no chats, send empty list and close
                close()
                return@callbackFlow
            }

            // 2. Fetch all chat documents in parallel
            val chatJobs: List<Deferred<Chat?>> = recentChatsIdList.map { chatId ->
                scope.async {
                    try {
                        firestore.collection("chats").document(chatId).get().await()
                            .toObject(Chat::class.java)
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to fetch chat document: $chatId", e); null
                    }
                }
            }
            val chats: List<Chat> = chatJobs.mapNotNull { it.await() }

            // 3. Collect unique participant IDs
            val uniqueUserIds: Set<String> = chats
                .flatMap { it.participants }
                .filter { it != currentFirebaseUser.uid }
                .toSet()

            // 4. Use the global cache and fetch any missing users
            localUserCache.putAll(userRepo.getGlobalUserCacheSnapshot().filterKeys { it in uniqueUserIds })
            val missingUserIds: List<String> = uniqueUserIds.filter { it !in localUserCache }

            if (missingUserIds.isNotEmpty()) {
                missingUserIds.chunked(10).forEach { userIdChunk ->
                    try {
                        val usersSnapshot = firestore.collection("users")
                            .whereIn("id", userIdChunk).get().await()
                        usersSnapshot.documents.forEach { doc ->
                            doc.toObject(User::class.java)?.let { user ->
                                localUserCache[user.id] = user
                                userRepo.putUserInGlobalCache(user.id, user)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to fetch chunk of missing users", e)
                    }
                }
                userRepo.updateCacheTimestamp()
            }

            // 5. Build the initial list of chat previews
            chats.forEach { chat ->
                val otherUserId = chat.participants.firstOrNull { it != currentFirebaseUser.uid }
                val otherUser = otherUserId?.let { localUserCache[it] }

                if (otherUser != null) {
                    chatPreviews[chat.id] = ChatPreview(
                        chatId = chat.id,
                        otherUser = otherUser,
                        lastMessage = chat.lastMessage,
                        updatedAt = chat.updatedAt,
                        typingStatus = chat.typingStatus
                    )
                }
            }

            // 6. Emit the initial data
            trySend(chatPreviews.values.sortedByDescending { it.updatedAt })

            // 7. Attach real-time listeners for updates
            recentChatsIdList.forEach { chatId ->
                val listener = firestore.collection("chats").document(chatId)
                    .addSnapshotListener { snapshot, exception ->
                        if (exception != null) {
                            Log.w(TAG, "Listener error for chat: $chatId", exception)
                            return@addSnapshotListener
                        }
                        val updatedChat = snapshot?.toObject(Chat::class.java) ?: return@addSnapshotListener
                        val otherUserId = updatedChat.participants.firstOrNull { it != currentFirebaseUser.uid }

                        if (otherUserId != null) {
                            // User should be in cache by now, but check just in case
                            val otherUser = localUserCache[otherUserId] ?: userRepo.getGlobalUserCacheSnapshot()[otherUserId]
                            if (otherUser != null) {
                                chatPreviews[chatId] = ChatPreview(
                                    chatId = updatedChat.id,
                                    otherUser = otherUser,
                                    lastMessage = updatedChat.lastMessage,
                                    updatedAt = updatedChat.updatedAt
                                )
                                trySend(chatPreviews.values.sortedByDescending { it.updatedAt })
                            }
                            // If user is not in cache, we could fetch them here, but for simplicity
                            // we rely on the initial fetch.
                        }
                    }
                listeners.add(listener)
            }

            // Cleanup listeners and scope when the flow is cancelled
            awaitClose {
                listeners.forEach { it.remove() }
                scope.cancel()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving chat previews", e)
            close(e)
        }
    }

    /**
     * Updates the typing status for a specific user within a chat.
     *
     * @param chatId The ID of the chat.
     * @param userId The ID of the user whose typing status is changing.
     * @param isTyping `true` if the user is typing, `false` otherwise.
     */
    override suspend fun updateUserTypingStatus(chatId: String, userId: String, isTyping: Boolean) {
        try {
            val chatRef = firestore.collection("chats").document(chatId)
            chatRef.update("typingStatus.$userId", isTyping).await()
        } catch (e: Exception) {
            Log.w(TAG, "Typing status could not be updated for user $userId in chat $chatId", e)
            throw e
        }
    }
}
