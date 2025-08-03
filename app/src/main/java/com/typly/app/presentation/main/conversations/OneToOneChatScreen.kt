package com.typly.app.presentation.main.conversations

import android.Manifest
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.typly.app.presentation.components.AttachmentOptionsBottomSheet
import com.typly.app.presentation.components.ChatTopBar
import com.typly.app.presentation.components.FullscreenImageViewer
import com.typly.app.presentation.components.MessageInputBar
import com.typly.app.presentation.components.MessageItem
import com.typly.app.domain.model.ChatResult
import com.typly.app.presentation.call.UserPresenceViewModel
import kotlinx.coroutines.delay
import pub.devrel.easypermissions.EasyPermissions
import androidx.compose.runtime.DisposableEffect
import com.typly.app.util.AppState

/**
 * Data class representing a chat message for UI display purposes.
 * 
 * @property id Unique identifier for the message
 * @property text The text content of the message
 * @property timestamp Formatted timestamp string
 * @property isSent Whether the message was sent by current user
 * @property isDelivered Whether the message has been delivered
 * @property isRead Whether the message has been read
 */
data class ChatMessage(
    val id: String,
    val text: String,
    val timestamp: String,
    val isSent: Boolean, // true if sent by current user, false if received
    val isDelivered: Boolean = true,
    val isRead: Boolean = false
)

/**
 * Main one-to-one chat screen composable for private messaging.
 * 
 * Provides comprehensive chat functionality including text and image messaging,
 * message status indicators, typing indicators, audio call initiation,
 * and fullscreen image viewing. Features real-time message synchronization
 * and proper permission handling.
 * 
 * @param targetUserId ID of the user being chatted with
 * @param onBackPressed Callback triggered when back button is pressed
 * @param onAudioCall Callback triggered when audio call is initiated
 * @param onIncomingCall Callback for handling incoming call navigation
 */
@Composable
fun OneToOneChatScreen(
    targetUserId: String,
    onBackPressed: () -> Unit,
    onAudioCall: () -> Unit,
    onIncomingCall: (callId: String, callerName: String) -> Unit = { _, _ -> }, // New parameter for incoming call navigation
) {

    val TAG = "OneToOneChatScreen"

    val viewModel = hiltViewModel<OneToOneChatViewModel>()
    val presenceViewModel = hiltViewModel<UserPresenceViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val currentUserId = viewModel.getCurrentUserId() // Replace with actual current user ID logic
    var isSendingMessage by remember { mutableStateOf(false) }
    var sendMessageError by remember { mutableStateOf<String?>(null) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    val activeChat by viewModel.activeChatState.collectAsState()

    val allMessages by viewModel.allMessages.collectAsState()
    
    // Attachment functionality states
    var showAttachmentBottomSheet by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Fullscreen image viewer states
    var showFullscreenImage by remember { mutableStateOf(false) }
    var fullscreenImageUrl by remember { mutableStateOf("") }



    // Required permissions for audio call
    val audioCallPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ).apply {
        // Add additional permissions for older Android versions
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            add(Manifest.permission.READ_PHONE_STATE)
        }
    }.toTypedArray()

    // Function to handle image selection
    fun handleImageSelection(imageUri: Uri) {
        selectedImageUri = imageUri
        Log.d(TAG, "Image selected: $imageUri")
    }

    // Function to check and request audio call permissions
    fun handleAudioCallRequest() {
        Log.d(TAG, "Audio call request initiated")
        
        if (EasyPermissions.hasPermissions(context, *audioCallPermissions)) {
            // All permissions granted, proceed with audio call
            Log.d(TAG, "Audio call permissions granted")
            onAudioCall()
        } else {
            // Show permission explanation dialog
            Log.d(TAG, "Audio call permissions missing, showing dialog")
            showPermissionDialog = true
        }
    }

    DisposableEffect(activeChat?.id) {
        val currentChatId = activeChat?.id
        if (currentChatId != null) {
            // Set active chat ID when entering this screen
            AppState.activeChatId = currentChatId
            Log.d(TAG, "Entered active chat screen: ${AppState.activeChatId}")
        }

        onDispose {
            // Clear active chat ID when leaving this screen
            // Only clear if we're leaving this specific chat
            if (AppState.activeChatId == currentChatId) {
                AppState.activeChatId = null
                Log.d(TAG, "Left active chat screen")
            }
        }
    }

    LaunchedEffect(allMessages){
        allMessages.filter { it.receiverId == currentUserId && !it.isRead }
            .forEach{message->
                // Mark message as read
                viewModel.markMessageAsRead(chatId = message.chatId, messageId = message.id)
            }
    }

    LaunchedEffect(targetUserId) {
        viewModel.getUserById(targetUserId)
        viewModel.startListeners(receiverUserId = targetUserId)
    }
    

    
    // Listen to send message state changes
    LaunchedEffect(Unit) {
        viewModel.sendMessageState.collect { result ->
            when (result) {
                is ChatResult.Loading -> {
                    isSendingMessage = true
                    sendMessageError = null
                }
                is ChatResult.Success -> {
                    isSendingMessage = false
                    sendMessageError = null
                }
                is ChatResult.Error -> {
                    isSendingMessage = false
                    sendMessageError = result.message
                }
            }
        }
    }


    // Auto-clear error message after 5 seconds
    LaunchedEffect(sendMessageError) {
        if (sendMessageError != null) {
            delay(5000)
            sendMessageError = null
        }
    }

    var messageText by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Loading state
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Loading...",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        return
    }
    
    // Error state
    if (uiState.errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    text = "❌",
                    style = MaterialTheme.typography.headlineLarge
                )
                Text(
                    text = uiState.errorMessage?:"",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Back to previous screen.",
                    color = Color.White.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    
    // Success state - user loaded
    val user = uiState.user
    if (user == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "User not found",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding() // This handles keyboard padding
    ) {
        // Chat TopBar - Fixed at top
        ChatTopBar(
            user = user,
            onBackPressed = onBackPressed,
            onUserProfileClick = {
                // TODO: Navigate to user profile
                Log.d(TAG, "Navigate to user profile for ${user.nickname}")
            },
            onMenuClick = {
                Log.d(TAG, "Chat menu clicked")
            },
            onAudioCallClick = {
                Log.d(TAG, "Audio call button clicked")
                handleAudioCallRequest()
            },
            isTyping = activeChat?.typingStatus?.get(targetUserId) ?: false,
            presenceManager = presenceViewModel.presenceManager
        )
        
        // Messages List - This will scroll up when keyboard appears
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = true // Latest messages at bottom
        ) {
            items(allMessages.reversed()) { message ->
                MessageItem(
                    message = message, 
                    currentUserId = currentUserId,
                    onImageClick = { imageUrl ->
                        fullscreenImageUrl = imageUrl
                        showFullscreenImage = true
                    }
                )
            }
        }



        // Message Input - Fixed at bottom
        MessageInputBar(
            messageText = messageText,
            onMessageTextChange = { newText ->
                messageText = newText
                activeChat?.id?.let { chatId ->
                    if (chatId.isNotEmpty()) {
                        viewModel.updateUserTypingStatus(
                            chatId = chatId,
                            userId = currentUserId
                        )
                    }
                }
            },
            onSendMessage = {
                if ((messageText.isNotBlank() || selectedImageUri != null) && !isSendingMessage) {
                    Log.d(TAG, "Sending message to ${user.nickname}: $messageText")
                    if (selectedImageUri != null) {
                        Log.d(TAG, "Also sending image: $selectedImageUri")
                    }
                    viewModel.sendMessage(
                        chatId = viewModel.activeChatState.value?.id,
                        receiverUserId = targetUserId, 
                        messageContent = messageText,
                        imageUri = selectedImageUri,
                        context = context
                    )
                    messageText = ""
                    selectedImageUri = null // Clear image after sending
                    activeChat?.id?.let { chatId ->
                        if (chatId.isNotEmpty()) {
                            viewModel.updateUserTypingStatusToFalse(chatId = chatId, userId = currentUserId)
                        }
                    }

                }
            },
            onAttachmentClick = {
                showAttachmentBottomSheet = true
            },
            selectedImageUri = selectedImageUri,
            onImageRemove = {
                selectedImageUri = null
            },
            isLoading = isSendingMessage
        )
        
        // Show error message if any
        sendMessageError?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "❌ $error",
                    color = Color.Red.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    // Attachment options bottom sheet
    AttachmentOptionsBottomSheet(
        isVisible = showAttachmentBottomSheet,
        onDismiss = { showAttachmentBottomSheet = false },
        onImageSelected = { imageUri ->
            handleImageSelection(imageUri)
        }
    )
    
    // Fullscreen image viewer
    FullscreenImageViewer(
        imageUrl = fullscreenImageUrl,
        isVisible = showFullscreenImage,
        onDismiss = { showFullscreenImage = false }
    )
    
    // Permission explanation dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { 
                Text("🎤 Audio Call Permissions") 
            },
            text = { 
                Text(
                    "To make audio calls, we need:\n\n" +
                    "• Microphone access to capture your voice\n" +
                    "• Audio settings to adjust call quality\n" +
                    (if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) "• Phone state for call management\n" else "") +
                    "\nThese permissions are only used during voice calls."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionDialog = false
                        // Use EasyPermissions to request permissions
                        // Note: EasyPermissions typically needs Activity context for requesting
                        // For Compose, we'll show a helpful message to grant manually
                        onAudioCall() // Let AudioCallScreen handle the permission request
                    }
                ) {
                    Text("Continue to Call")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showPermissionDialog = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Preview()
@Composable
fun OneToOneChatScreenPreview() {
    OneToOneChatScreen(
        targetUserId = "sample_user_id",
        onBackPressed = { /* Preview action */ },
        onAudioCall = { /* Preview action */ }
    )
}
