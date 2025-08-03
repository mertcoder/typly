package com.typly.app.presentation.main.profile

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.typly.app.R
import com.typly.app.presentation.components.SemiTransparentTextField
import com.typly.app.data.remote.dto.User
import com.typly.app.domain.model.UserResult
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main profile screen composable for viewing and editing user profile information.
 * 
 * Provides comprehensive profile management including image upload, text editing,
 * profile validation, and update functionality. Features reactive state management,
 * permission handling for image selection, and glassmorphism design elements.
 * 
 * @param user Default user data for preview purposes
 * @param onUpdateProfile Callback triggered when profile is updated
 * @param onChangeProfileImage Callback triggered when profile image change is requested
 */
@Composable
fun ProfileScreen(
    user: User = User().copy(isProfileComplete = true), // Default empty user for preview
    onUpdateProfile: (User) -> Unit = {}, // TODO: Implement profile update functionality
    onChangeProfileImage: () -> Unit = {} // TODO: Implement profile image change functionality
) {

    val viewModel = hiltViewModel<ProfileViewModel>()
    var isEditing by remember { mutableStateOf(false) }
    var editedFullName by remember { mutableStateOf(user.fullName) }
    var editedNickname by remember { mutableStateOf(user.nickname) }
    var editedBio by remember { mutableStateOf(user.bio) }
    val userProfileData by viewModel.currentUserData.collectAsState()

    val context = LocalContext.current
    val profileUpdateResult by viewModel.profileUpdateResult.collectAsState()

    // Gallery launcher for profile image selection
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfileImageUri(it)
            // Immediately start upload process
            viewModel.updateProfileImage(context)
        }
    }

    // Permission launcher for gallery access
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        }
    }

    // Handle profile update results
    LaunchedEffect(profileUpdateResult) {
        when (profileUpdateResult) {
            is UserResult.Success -> {
                // Show success message or handle UI update
                viewModel.clearUpdateResult()
            }
            is UserResult.Error -> {
                // Show error message
                // You can add a Snackbar or Toast here
                viewModel.clearUpdateResult()
            }
            else -> {}
        }
    }


    LaunchedEffect(Unit) {
        viewModel.fetchUserProfileData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Profile Image Section with Glass Effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.025f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (userProfileData?.profileImageUrl.isNullOrEmpty()) {
                        // Default profile image
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.1f),
                                            Color.White.copy(alpha = 0.05f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.default_profile_picture),
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(60.dp),
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    } else {
                      SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(userProfileData?.profileImageUrl)
                                .crossfade(true)
                                .size(120)               // decode
                                .build(),
                            contentDescription = "Profile Image",
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        ) {
                            when (painter.state) {
                                is AsyncImagePainter.State.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            strokeWidth = 2.dp,
                                            color = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(30.dp)
                                        )
                                    }
                                }

                                is AsyncImagePainter.State.Success -> {
                                    SubcomposeAsyncImageContent()          // real image
                                }

                                is AsyncImagePainter.State.Error,
                                is AsyncImagePainter.State.Empty -> {
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(
                                                brush = Brush.radialGradient(
                                                    colors = listOf(
                                                        Color.White.copy(alpha = 0.1f),
                                                        Color.White.copy(alpha = 0.05f)
                                                    )
                                                )
                                            ),
        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Profile Error",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(60.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Edit Image Button
                    IconButton(
                        onClick = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            permissionLauncher.launch(permission)
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(36.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Change Profile Image",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Anonymous Badge if applicable
                if (userProfileData?.isAnonymous == true) {
                    Card(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.1f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = "Anonymous User",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Profile Information Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.025f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with Edit Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Profile Information",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    TextButton(
                        onClick = {
                            if (isEditing) {
                                // Validation ile güncelleme
                                viewModel.updateTextProfile(editedFullName, editedBio, context)
                                isEditing = false
                            } else {
                                // Edit mode'a geç
                                userProfileData?.let { currentData ->
                                    editedFullName = currentData.fullName
                                    editedBio = currentData.bio
                                }
                                isEditing = true
                            }
                        }
                    ) {
                        Text(
                            text = if (isEditing) "Save" else "Edit",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Full Name
                Text(
                    text ="Full Name",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (isEditing) {
                    SemiTransparentTextField(
                        value = editedFullName,
                        onValueChange = { editedFullName = it },
                        placeholder = "Enter your full name"
                    )
                } else {
                    Text(
                        text = userProfileData?.fullName ?:"",
                        color = if (user.fullName.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Nickname
                Text(
                    text = "Nickname",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                //Nickname read-only
                Text(
                    text =userProfileData?.nickname ?:"",
                    color = if (user.nickname.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                Spacer(modifier = Modifier.height(16.dp))

                // Bio
                Text(
                    text = "Bio",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                if (isEditing) {
                    SemiTransparentTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        placeholder = "Tell us about yourself"
                    )
                } else {
                    Text(
                        text = if(userProfileData?.bio.isNullOrEmpty())  "Hi, I'm using Typly! (am i?)" else userProfileData?.bio?:"",
                        color = if (user.bio.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email (Read-only)
                Text(
                    text = "Email",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text =userProfileData?.email ?:"",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Account Statistics Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.025f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column {
        Text(
                    text = "Profile Insights",
            color = Color.White,
                    fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Member Since
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Member since:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(userProfileData?.createdAt?: System.currentTimeMillis())),
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Activity Streak 🔥
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🔥 Activity streak:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${(3..30).random()} days", // TODO: Calculate real streak
                        color = Color(0xFFFF6B35),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Chat Personality 🎭
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎭 Chat style:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = listOf("💬 Chatty", "🎯 Direct", "😄 Funny", "🤔 Thoughtful", "⚡ Quick").random(), // TODO: Analyze user's chat patterns
                        color = Color(0xFF4ECDC4),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Response Speed ⚡
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ Avg response time:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${(1..5).random()}m ${(10..59).random()}s", // TODO: Calculate real response time
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Favorite Time to Chat 🌙☀️
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🌙 Most active:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    val timePrefs = listOf("🌅 Early Bird", "☀️ Day Time", "🌆 Evening", "🌙 Night Owl")
                    Text(
                        text = timePrefs.random(), // TODO: Analyze user's most active hours
                        color = Color(0xFFFFD93D),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Emoji Usage 😊
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "😊 Favorite emoji:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Text(
                        text = listOf("😂", "❤️", "👍", "🔥", "💯", "🤔", "😊", "🎉").random(), // TODO: Track most used emoji
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Profile Completion Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✨ Profile complete:",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Card(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = if (user.isProfileComplete) {
                                        listOf(
                                            Color.Green.copy(alpha = 0.2f),
                                            Color.Green.copy(alpha = 0.1f)
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFFF9500).copy(alpha = 0.2f),
                                            Color(0xFFFF9500).copy(alpha = 0.1f)
                                        )
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = if (user.isProfileComplete) "Yes" else "No",
                            color = if (user.isProfileComplete) Color.Green.copy(alpha = 0.9f) else Color(0xFFFF9500).copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom navigation
    }
}

@Preview()
@Composable
fun ProfileScreenPreview() {
    // Sample user data for preview
    val sampleUser = User(
        id = "sample123",
        email = "john.doe@example.com",
        fullName = "John Doe",
        nickname = "johndoe",
        bio = "Flutter developer passionate about creating amazing mobile experiences",
        profileImageUrl = null,
        isAnonymous = false,
        createdAt = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000), // 30 days ago
        lastActive = System.currentTimeMillis() - (2L * 60 * 60 * 1000), // 2 hours ago
        recentChats = listOf("chat1", "chat2", "chat3"),
        isProfileComplete = true
    )

    ProfileScreen(
        user = sampleUser,
        onUpdateProfile = { /* Preview action */ },
        onChangeProfileImage = { /* Preview action */ }
    )
}
