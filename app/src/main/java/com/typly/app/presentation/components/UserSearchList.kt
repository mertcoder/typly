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
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.typly.app.R
import com.typly.app.data.remote.dto.User

/**
 * A scrollable list component for displaying user search results.
 * 
 * Presents a list of users in a glassmorphism-styled container with loading states,
 * empty state handling, and individual user items. Each user item displays profile
 * picture, name, email, and bio information. Supports click interactions for user selection.
 * 
 * @param users List of user objects to display
 * @param onUserClick Callback triggered when a user item is clicked
 * @param isLoading Whether the search is currently in progress
 * @param modifier Modifier to be applied to the list container
 */
@Composable
fun UserSearchList(
    users: List<User>,
    onUserClick: (User) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Main container with glass effect
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 8.dp)
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
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Searching...",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
            users.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add_people),
                            contentDescription = "No Users Found",
                            modifier = Modifier.size(64.dp),
                            tint = Color.White.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No Users Found",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(users) { user ->
                        UserSearchItem(
                            user = user,
                            onClick = { onUserClick(user) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual user item component for the search results list.
 * 
 * Displays a single user's information including profile picture, nickname,
 * email, and bio in a glassmorphism-styled container. Handles click interactions
 * and provides fallback UI for missing profile pictures.
 * 
 * @param user The user data object to display
 * @param onClick Callback triggered when this user item is clicked
 */
@Composable
fun UserSearchItem(
    user: User,
    onClick: () -> Unit
) {
    // Separate glass effect container for each user item
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
            // Profile picture container
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                // Profile picture
                if (user.profileImageUrl != null && user.profileImageUrl.isNotEmpty()) {
                    val painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = user.profileImageUrl)
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
                            painter = painterResource(id = R.drawable.ic_select_pp),
                            contentDescription = "Default Profile",
                            modifier = Modifier.size(32.dp),
                            alpha = 0.8f
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // User information
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // User name
                Text(
                    text = user.nickname,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                // Email address (if available)
                if (user.email.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = user.email,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Biography or status (if available)
                if (user.bio?.isNotEmpty() == true) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = user.bio,
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Additional info icon on the right (optional)
            Icon(
                painter = painterResource(id = R.drawable.ic_text_bubble),
                contentDescription = "Text User",
                modifier = Modifier.size(20.dp),
                tint = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview()
@Composable
fun UserSearchListPreview() {
    val sampleUsers = listOf(
        User(
            id = "1",
            nickname = "ahmet_yilmaz",
            email = "ahmet@example.com",
            bio = "Software developer"
        ),
        User(
            id = "2", 
            nickname = "ayse_demir",
            email = "ayse@example.com",
            bio = "UI/UX Designer"
        ),
        User(
            id = "3",
            nickname = "mehmet_kara", 
            email = "mehmet@example.com",
            bio = "Project manager"
        ),
        User(
            id = "4",
            nickname = "fatma_ozkan",
            email = "fatma@example.com",
            bio = "Backend developer"
        )
    )
    
    UserSearchList(
        users = sampleUsers,
        onUserClick = { /* Preview action */ }
    )
}

@Preview()
@Composable
fun UserSearchListEmptyPreview() {
    UserSearchList(
        users = emptyList(),
        onUserClick = { /* Preview action */ }
    )
}

@Preview()
@Composable
fun UserSearchListLoadingPreview() {
    UserSearchList(
        users = emptyList(),
        onUserClick = { /* Preview action */ },
        isLoading = true
    )
} 
