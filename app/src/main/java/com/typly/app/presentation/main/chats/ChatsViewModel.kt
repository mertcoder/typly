package com.typly.app.presentation.main.chats

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.presentation.components.ChatPreview
import com.typly.app.data.repository.AuthRepositoryImpl
import com.typly.app.data.repository.ChatRepositoryImpl
import com.typly.app.data.repository.UserRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for managing chat previews and chat list functionality.
 * 
 * Handles loading, refreshing, and error management for chat previews.
 * Provides reactive state flows for UI consumption and manages chat
 * repository operations with proper error handling and loading states.
 * 
 * @property chatRepository Repository for chat-related operations
 * @property authRepository Repository for authentication operations  
 * @property firestore Firestore database instance
 */
@HiltViewModel
class ChatsViewModel
@Inject constructor(
    private val chatRepository: ChatRepositoryImpl,
    private val authRepository: AuthRepositoryImpl,
    private val firestore: FirebaseFirestore
)
    : ViewModel()
    {

        /** StateFlow containing the list of chat previews for display */
        private val _chatPreviews = MutableStateFlow<List<ChatPreview>>(emptyList())
        val chatPreviews: StateFlow<List<ChatPreview>> = _chatPreviews.asStateFlow()

        /** StateFlow indicating whether chat previews are currently loading */
        private val _isLoading = MutableStateFlow(false)
        val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

        /** StateFlow containing any error message to display to the user */
        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        /**
         * Loads chat previews from the repository.
         * 
         * Initiates a coroutine to fetch chat previews, manages loading state,
         * and handles any errors that occur during the operation. Updates the
         * UI state flows based on the operation results.
         */
        fun loadChatPreviews() {
            viewModelScope.launch {
                _isLoading.value = true
                _errorMessage.value = null
                
                chatRepository.retrieveChatPreviews()
                    .catch { e ->
                        _isLoading.value = false
                        _errorMessage.value = e.message ?: "Failed to load chat previews"
                        Log.e("ChatViewModel", "Failed to load chat previews", e)
                    }
                    .collect { previews ->
                        _isLoading.value = false
                        _chatPreviews.value = previews
                    }
            }
        }

        /**
         * Clears any current error message.
         * 
         * Resets the error state to allow the user to dismiss error notifications
         * or retry operations without displaying stale error messages.
         */
        fun clearError() {
            _errorMessage.value = null
        }

        /**
         * Refreshes chat previews by clearing cache and reloading data.
         * 
         * Clears the user cache to ensure fresh data is fetched from the
         * server and then reloads all chat previews. Useful for pull-to-refresh
         * functionality or when data synchronization is needed.
         */
        fun refreshChatPreviews() {
            // Clear cache and reload
            UserRepositoryImpl.clearUserCache()
            loadChatPreviews()
        }
    }
