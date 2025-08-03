package com.typly.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.typly.app.data.remote.dto.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Message item composable for displaying individual chat messages.
 * 
 * Supports both text and image messages with different styling based on sender.
 * Provides click functionality for images to open fullscreen viewer.
 * Features glassmorphism design with message status indicators.
 * 
 * @param message The message data object containing content and metadata
 * @param currentUserId ID of the current user to determine message alignment
 * @param onImageClick Callback triggered when an image message is clicked
 */
@Composable
fun MessageItem(
    message: Message,
    currentUserId: String,
    onImageClick: (String) -> Unit = {}
) {
    val isSentByCurrentUser = message.senderId==currentUserId
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSentByCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(min = 80.dp, max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isSentByCurrentUser) 18.dp else 4.dp,
                        bottomEnd = if (isSentByCurrentUser) 4.dp else 18.dp
                    )
                )
                .background(
                    brush = if (isSentByCurrentUser) {
                        // Sent message - slightly more opaque
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        )
                    } else {
                        // Received message - less opaque
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    }
                )
                .padding(10.dp)
        ) {
            Column {
                // Message media (if exists)
                if (message.media.isNotEmpty()) {
                    val painter = rememberAsyncImagePainter(message.media)
                    
                    Box(
                        modifier = Modifier
                            .widthIn(max = 250.dp)
                            .aspectRatio(1.2f) // 5:4 aspect ratio for images
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onImageClick(message.media)
                            }
                    ) {
                        Image(
                            painter = painter,
                            contentDescription = "Message Image",
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Loading indicator
                        if (painter.state is AsyncImagePainter.State.Loading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.2f)
                                    .background(Color.White.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White.copy(alpha = 0.6f),
                                    strokeWidth = 2.dp
                                )
                            }
                        }
                    }
                    
                    // Add spacing between image and text if text exists
                    if (message.content.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // Message text (if exists)
                if (message.content.isNotEmpty()) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time and status row - always at the bottom right
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatTimestampToTimeString(message.timestamp),
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 10.sp
                    )


                    if (isSentByCurrentUser) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = when {
                                message.isRead -> "✓✓"
                                else -> "✓"
                            },
                            color = if (message.isRead) Color.Blue.copy(alpha = 0.8f)
                            else Color.White.copy(alpha = 0.6f),
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }



}

/**
 * Formats a timestamp in milliseconds to a human-readable time string.
 * 
 * @param timestampMillis The timestamp in milliseconds since epoch
 * @return Formatted time string in HH:mm format (e.g., "14:30")
 */
fun formatTimestampToTimeString(timestampMillis: Long): String {
    val date = Date(timestampMillis)
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(date)
}
