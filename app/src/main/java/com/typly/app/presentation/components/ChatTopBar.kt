package com.typly.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.typly.app.R
import com.typly.app.data.remote.dto.User
import com.typly.app.presentation.call.UserPresenceManager
import com.typly.app.presentation.call.UserPresenceViewModel

/**
 * Composable that displays the top bar for chat screens.
 *
 * Shows user profile information, online status, typing indicator, and action buttons
 * including back navigation, voice call, and menu options. Features glass effect styling
 * with real-time presence updates.
 *
 * @param user The user information to display in the top bar
 * @param isTyping Whether the user is currently typing
 * @param onBackPressed Callback invoked when the back button is pressed
 * @param onUserProfileClick Callback invoked when the user profile area is clicked
 * @param onMenuClick Callback invoked when the menu button is pressed
 * @param onAudioCallClick Callback invoked when the audio call button is pressed
 * @param presenceManager Manager for tracking user presence and online status
 */
@Composable
fun ChatTopBar(
    user: User,
    isTyping: Boolean,
    onBackPressed: () -> Unit,
    onUserProfileClick: () -> Unit,
    onMenuClick: () -> Unit,
    onAudioCallClick: () -> Unit,
    presenceManager: UserPresenceManager = hiltViewModel<UserPresenceViewModel>().presenceManager
) {
    val isOnline = presenceManager.observeUserOnlineStatus(user.id).collectAsState(initial = false)
    val lastSeenTimestamp = presenceManager.getUserLastSeen(user.id).collectAsState(initial = 0L)

    // Glass effect container with optimized sizing
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.025f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(horizontal = 6.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back navigation button
            IconButton(
                onClick = onBackPressed,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(6.dp))

            // User profile picture with online indicator
            Box(
                modifier = Modifier.size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                if (user.profileImageUrl != null) {
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = user.profileImageUrl)
                            .apply { crossfade(true) }.build()
                    )
                    Image(
                        painter = painter,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Default profile icon
                    Box(
                        modifier = Modifier
                            .size(40.dp)
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
                            painter = painterResource(id = R.drawable.ic_select_pp),
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(22.dp),
                            alpha = 0.8f
                        )
                    }
                }

                // Online status indicator
                if (isOnline.value) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color.Green)
                            .align(Alignment.BottomEnd)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // User information - clickable area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onUserProfileClick() }
                    .padding(vertical = 1.dp)
            ) {
                Text(
                    text = user.nickname,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = when {
                        isTyping -> "typing..."
                        isOnline.value -> "online"
                        else -> presenceManager.formatLastSeen(lastSeenTimestamp.value)
                    },
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Voice call button
            IconButton(
                onClick = onAudioCallClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice Call",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }

            // Menu button
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
