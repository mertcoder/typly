package com.typly.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.typly.app.R
import com.typly.app.data.remote.dto.Message
import com.typly.app.data.remote.dto.User

/**
 * Data class representing a chat preview item for the chat list.
 *
 * Contains all necessary information to display a chat preview including
 * user information, last message details, and interaction state.
 *
 * @property chatId Unique identifier for the chat conversation
 * @property otherUser The other participant in the chat conversation
 * @property lastMessage The most recent message in the chat, null if no messages exist
 * @property updatedAt Timestamp of the last chat activity
 * @property unreadCount Number of unread messages in this chat
 * @property typingStatus Map tracking typing status of users (userId to typing boolean)
 */
data class ChatPreview(
    val chatId: String,
    val otherUser: User,
    val lastMessage: Message?,
    val updatedAt: Long,
    val unreadCount: Int = 0,
    val typingStatus: Map<String, Boolean> = emptyMap()
)

/**
 * Composable that displays a list of chat previews with glass effect styling.
 *
 * Shows either a list of chat conversations or an empty state message when no chats exist.
 * Each chat item displays user information, last message preview, and unread count.
 *
 * @param chats List of chat previews to display
 * @param onChatClick Callback invoked when a chat item is clicked, receives the user ID
 * @param modifier Modifier for customizing the component's appearance and behavior
 */
@Composable
fun ChatPreviewList(
    chats: List<ChatPreview>,
    onChatClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Main container with glass effect
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 1.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.025f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        if (chats.isEmpty()) {
            // Empty list state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_select_pp),
                        contentDescription = "No Chats",
                        modifier = Modifier.size(64.dp),
                        alpha = 0.5f
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "There are no chats yet",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Search for users to start chatting",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(chats) { chat ->
                    ChatPreviewItem(
                        chat = chat,
                        onClick = { onChatClick(chat.otherUser.id) }
                    )
                }
            }
        }
    }
}

/**
 * Composable that displays a single chat preview item.
 *
 * Shows user profile picture, name, last message preview, timestamp,
 * typing indicator, and unread message count with glass effect styling.
 *
 * @param chat The chat preview data to display
 * @param onClick Callback invoked when the chat item is clicked
 */
@Composable
fun ChatPreviewItem(
    chat: ChatPreview,
    onClick: () -> Unit
) {
    val isTyping = chat.typingStatus[chat.otherUser.id] ?: false
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile image container
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile image
                if (!chat.otherUser.profileImageUrl.isNullOrEmpty()) {
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = chat.otherUser.profileImageUrl)
                            .apply {
                                crossfade(true)
                            }.build()
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default profile icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.2f),
                                        Color.White.copy(alpha = 0.1f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_picture),
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(32.dp),
                            alpha = 0.8f
                        )
                    }
                }

                // Online status indicator (currently removed but can be re-enabled)
                /*
                if (true) { // chat.otherUser.isOnline TODO
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }
                */
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Chat information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // User name and timestamp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.otherUser.nickname,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = formatTimestampToTimeString(chat.lastMessage?.timestamp ?: 2),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Last message and unread count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isTyping) "typing..." else chat.lastMessage?.content ?: "No chats yet.",
                        color = if (isTyping) Color(0xFF4ECDC4) else Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Unread message count badge
                    if (chat.unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (chat.unreadCount > 9) "9+" else chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
