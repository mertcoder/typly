package com.typly.app.presentation.main.chats

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.typly.app.presentation.components.ChatPreviewList
import com.typly.app.presentation.components.TopBar

/**
 * Main chat list screen displaying user's chat conversations.
 * 
 * Shows a list of chat previews with loading states, error handling,
 * and navigation capabilities. Features a top bar with search functionality
 * and handles user interactions for chat selection and user search.
 * 
 * @param onSearchUserIconClicked Callback triggered when search user icon is clicked
 * @param onChatSelected Callback triggered when a chat is selected, receives user ID
 */
@Composable
fun ChatsScreen(
    onSearchUserIconClicked: ()-> Unit,
    onChatSelected: (String) -> Unit = {},
) {
    val viewModel: ChatsViewModel = hiltViewModel()

    val chatPreviewList by viewModel.chatPreviews.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit){
        viewModel.loadChatPreviews()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // TopBar at the top
        TopBar(
            searchUser = {
                onSearchUserIconClicked()
            }
        )
        
        // Loading state
        if (isLoading && chatPreviewList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
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
        } else {
            // Chat Preview List - With lazy column
            ChatPreviewList(
                chats = chatPreviewList,
                onChatClick = { userId ->
                    onChatSelected(userId)
                    Log.d("ChatsScreen", "Chat selected: $userId")
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Error message
        errorMessage?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "❌ $error",
                        color = Color.Red.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { 
                            viewModel.clearError()
                            viewModel.refreshChatPreviews()
                        }
                    ) {
                        Text("Try Again.")
                    }
                }
            }
        }
    }
}
