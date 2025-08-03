package com.typly.app.presentation.main.conversations

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.data.remote.dto.Chat
import com.typly.app.data.remote.dto.Message
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.ChatRepository
import com.typly.app.domain.model.ChatResult
import com.typly.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing the UI state for chat screens.
 * 
 * @property user The current chat participant user information
 * @property isLoading Whether a user-related operation is in progress
 * @property errorMessage Any error message to display to the user
 */
data class ChatUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel for managing one-to-one chat functionality.
 * 
 * Handles real-time messaging, user information retrieval, message sending,
 * typing indicators, and message read status management. Provides reactive
 * state flows for UI consumption and manages chat-related operations.
 * 
 * @property chatRepository Repository for chat-related operations
 * @property authRepository Repository for authentication operations
 * @property userRepository Repository for user-related operations
 */
@HiltViewModel
class OneToOneChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    /** StateFlow containing the current UI state for chat operations */
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    /** SharedFlow for message sending operation results */
    private val _sendMessageState = MutableSharedFlow<ChatResult<Message>>()
    val sendMessageState = _sendMessageState.asSharedFlow()

    /** StateFlow containing all messages in the current chat */
    private val _allMessages = MutableStateFlow<List<Message>>(emptyList())
    val allMessages = _allMessages.asStateFlow()

    /** StateFlow containing the active chat information */
    private val _activeChatState = MutableStateFlow<Chat?>(null)
    val activeChatState: StateFlow<Chat?> = _activeChatState.asStateFlow()

    /** Current authenticated user's ID */
    private val _currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    
    /** Job for managing typing indicator timing */
    private var typingJob: Job? = null


    /**
     * Starts real-time listeners for chat and message updates.
     * 
     * Establishes listeners for chat information and message updates between
     * two users. Creates a new chat if one doesn't exist and maintains
     * real-time synchronization of chat data.
     * 
     * @param currentUserId The current user's ID (defaults to authenticated user)
     * @param receiverUserId The ID of the user to chat with
     */
    fun startListeners(currentUserId: String = _currentUserId, receiverUserId: String) {
        viewModelScope.launch {
            val chatId = chatRepository.getChatIdBetweenUsers(currentUserId, receiverUserId)
                ?: chatRepository.createChat(listOf(currentUserId, receiverUserId)).id

            chatRepository.getChat(chatId).collect { result ->
                if (result is ChatResult.Success) {
                    _activeChatState.value = result.data
                }
            }
        }

        viewModelScope.launch {
            chatRepository.retrieveAllMessages(currentUserId, receiverUserId).collect { messages ->
                if (messages.isNotEmpty()) {
                    _allMessages.emit(messages)
                } else {
                    _allMessages.emit(emptyList())
                }
            }
        }
    }

    /**
     * Sends a message in the chat conversation.
     * 
     * Handles both text and image messages, creates chat if necessary,
     * manages loading states, and provides result feedback through SharedFlow.
     * Cancels any active typing indicators when sending.
     * 
     * @param chatId The chat ID (null to create new chat)
     * @param currentUserId The sender's user ID
     * @param receiverUserId The recipient's user ID
     * @param messageContent The text content of the message
     * @param imageUri Optional image URI to attach
     * @param context Android context for file operations
     */
    fun sendMessage(chatId: String?,currentUserId: String= _currentUserId, receiverUserId: String, messageContent: String, imageUri: android.net.Uri? = null, context: android.content.Context) {
        viewModelScope.launch {
            _sendMessageState.emit(ChatResult.Loading)
            try {
                val finalChatId = chatId ?: chatRepository.createChat(
                    listOf(currentUserId, receiverUserId)
                ).id
                typingJob?.cancel()

                val newMessage = chatRepository.sendMessage(finalChatId, messageContent, currentUserId, receiverUserId, imageUri, context)
                _sendMessageState.emit(ChatResult.Success(newMessage))

            } catch (e: Exception) {
                _sendMessageState.emit(ChatResult.Error(e.message ?: "An error occurred while sending the message"))
            }
        }
    }

    /**
     * Retrieves the current authenticated user's ID.
     * 
     * @return The UID of the currently authenticated user
     * @throws NullPointerException if no user is currently authenticated
     */
    fun getCurrentUserId(): String {
        return authRepository.getCurrentUser()!!.uid
    }

    /**
     * Retrieves user information by user ID.
     * 
     * Fetches user details from the repository and updates the UI state
     * with loading, success, or error states. Handles network errors
     * and provides appropriate error messages.
     * 
     * @param userId The ID of the user to retrieve
     */
    fun getUserById(userId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                userRepository.getUserById(userId).collectLatest { user ->
                    if (user != null) {
                        Log.d("OneToOneChatViewModel", "User found: $user")
                        _uiState.value = _uiState.value.copy(
                            user = user,
                            isLoading = false,
                            errorMessage = null
                        )
                    } else {
                        Log.d("OneToOneChatViewModel", "User not found")
                        _uiState.value = _uiState.value.copy(
                            user = null,
                            isLoading = false,
                            errorMessage = "User not found"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("OneToOneChatViewModel", "Error getting user: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error retrieving user information: ${e.message}"
                )
            }
        }
    }

    /**
     * Marks a specific message as read in the chat.
     * 
     * Updates the message read status in the repository and logs the operation
     * result. Handles both success and error cases with appropriate logging.
     * 
     * @param chatId The ID of the chat containing the message
     * @param messageId The ID of the message to mark as read
     */
    suspend fun markMessageAsRead(chatId: String, messageId: String) {
        chatRepository.markMessageAsRead(chatId, messageId)
            .collectLatest { result ->
                when (result) {
                    is ChatResult.Success -> {
                        Log.d("OneToOneChatViewModel", "Message marked as read successfully")
                    }
                    is ChatResult.Error -> {
                        Log.e("OneToOneChatViewModel", "Error marking message as read: ${result.message}")
                    }
                    else -> {}
                }
            }
    }

    /**
     * Updates user typing status with automatic timeout.
     * 
     * Sets the user as typing, waits for a short delay, then automatically
     * sets typing to false. Cancels any previous typing jobs to prevent
     * conflicts and ensures proper typing indicator behavior.
     * 
     * @param chatId The ID of the chat to update typing status
     * @param userId The ID of the user whose typing status to update
     */
    fun updateUserTypingStatus(chatId: String, userId: String) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            try {
                chatRepository.updateUserTypingStatus(chatId, userId, true)
                delay(200L)
                chatRepository.updateUserTypingStatus(chatId, userId, false)
            } catch (e: Exception) {
                Log.e("OneToOneChatViewModel", "Error in typing status update: ${e.message}", e)
            }
        }
    }
    /**
     * Immediately sets user typing status to false.
     * 
     * Cancels any active typing jobs and sets the typing status to false.
     * Used when user stops typing or when explicit typing cancellation is needed.
     * 
     * @param chatId The ID of the chat to update typing status
     * @param userId The ID of the user whose typing status to update
     */
    fun updateUserTypingStatusToFalse(chatId: String, userId: String) {
        typingJob?.cancel()
        typingJob = viewModelScope.launch {
            try {
                chatRepository.updateUserTypingStatus(chatId, userId, false)
            } catch (e: Exception) {
                Log.e("OneToOneChatViewModel", "Error in typing status update: ${e.message}", e)
            }
        }
    }
}
