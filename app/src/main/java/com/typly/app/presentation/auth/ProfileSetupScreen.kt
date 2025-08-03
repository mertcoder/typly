package com.typly.app.presentation.auth

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.typly.app.R
import com.typly.app.presentation.components.SemiTransparentTextField
import com.typly.app.presentation.components.SpinnableIcon
import com.typly.app.domain.model.UserResult
import com.typly.app.util.RegisterValidationResult

/**
 * Composable function for the user profile setup screen.
 *
 * This screen handles the completion of user profile setup after initial registration.
 * It provides an interface for users to customize their profile with personal information
 * and profile picture. The screen includes comprehensive form validation, image selection
 * with proper permissions handling, and real-time state management.
 *
 * Features include:
 * - Profile picture selection with permission handling
 * - Nickname availability validation
 * - Full name and bio input fields
 * - Real-time form validation with error messages
 * - Loading states and progress indicators
 * - Auto-population of default values from authentication data
 * - Integration with FCM token for push notifications
 *
 * @param viewModel The ProfileSetupViewModel managing the screen's state and business logic
 * @param onSetupComplete Callback executed when profile setup is successfully completed
 */
@Composable
fun ProfileSetupScreen(
    viewModel: ProfileSetupViewModel = hiltViewModel(),
    onSetupComplete: () -> Unit = {}
) {
    // State collection from ViewModel
    val nickname by viewModel.nickname.collectAsState()
    val fullName by viewModel.fullName.collectAsState()
    val bio by viewModel.bio.collectAsState()
    val profileImageUri by viewModel.profileImageUri.collectAsState()
    val registerState by viewModel.registerState.collectAsState()
    val completeRegisterState by viewModel.completeRegisterState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // Extract default values from authenticated user
    val user = viewModel.getCurrentUser()
    val defaultNickname = user?.email?.split("@")?.get(0)
    val defaultFullName = viewModel.getCurrentUser()?.displayName

    /**
     * LaunchedEffect for auto-populating default values.
     *
     * Runs once when composable is first created and populates nickname
     * and full name fields with data from Firebase Authentication if available.
     */
    LaunchedEffect(Unit) {
        if (defaultNickname != null) {
            viewModel.updateNickname(defaultNickname)
        }
        if (defaultFullName != null) {
            viewModel.updateFullName(defaultFullName)
        }
    }

    /**
     * LaunchedEffect for debugging button state.
     *
     * Logs current form state and button enabled status for debugging purposes.
     * Helps track form validation and loading states during development.
     */
    LaunchedEffect(nickname, fullName, isLoading) {
        Log.d("ProfileSetupScreen", "Debug - Button State:")
        Log.d("ProfileSetupScreen", "Nickname: $nickname")
        Log.d("ProfileSetupScreen", "FullName: $fullName")
        Log.d("ProfileSetupScreen", "IsLoading: $isLoading")
        Log.d("ProfileSetupScreen", "Button Enabled: ${!isLoading && nickname.isNotBlank() && fullName.isNotBlank()}")
    }

    /**
     * LaunchedEffect for handling profile setup completion.
     *
     * Monitors the complete registration state and triggers navigation
     * when profile setup is successful or handles errors appropriately.
     */
    LaunchedEffect(completeRegisterState) {
        when (completeRegisterState) {
            is UserResult.Success -> {
                Log.d("ProfileSetupScreen", "Setup completed successfully: ${(completeRegisterState as UserResult.Success).data}")
                onSetupComplete()
            }

            is UserResult.Error -> {
                Log.e("ProfileSetupScreen", "Setup failed: ${(completeRegisterState as UserResult.Error).message}")
            }

            else -> {}
        }
    }

    /**
     * Activity result launcher for image selection from gallery.
     *
     * Handles the result of the image picker intent and updates the
     * profile image URI in the ViewModel when an image is selected.
     */
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            viewModel.updateProfileImage(it)
        }
    }

    /**
     * Activity result launcher for runtime permission requests.
     *
     * Handles the result of storage permission requests and launches
     * the image picker if permission is granted.
     */
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            galleryLauncher.launch("image/*")
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Background image
        Image(
            painter = painterResource(R.drawable.blurrybg_dark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.size(50.dp))

            /**
             * Profile picture selection component.
             *
             * Displays current profile image or default placeholder and handles
             * click events to trigger image selection with proper permissions.
             */
            SpinnableIcon(
                modifier = Modifier.size(220.dp),
                imageUri = profileImageUri,
                onClick = {
                    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    permissionLauncher.launch(permission)
                }
            )

            // Profile setup form container
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(min = 300.dp)
                    .background(Color(0xFF000000), shape = RoundedCornerShape(28.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Nickname input field with comprehensive validation
                SemiTransparentTextField(
                    value = nickname,
                    onValueChange = { viewModel.updateNickname(it) },
                    placeholder = "Nickname",
                    isError = registerState is RegisterValidationResult.EmptyNickname ||
                            registerState is RegisterValidationResult.InvalidNickname ||
                            registerState is RegisterValidationResult.NicknameAlreadyExists ||
                            completeRegisterState is UserResult.Error,
                    errorMessage = when (registerState) {
                        is RegisterValidationResult.EmptyNickname -> "Nickname can not be blank!"
                        is RegisterValidationResult.InvalidNickname -> "Please enter a valid nickname."
                        is RegisterValidationResult.NicknameAlreadyExists -> "Nickname already exists."
                        else -> {
                            if (completeRegisterState is UserResult.Error &&
                                (completeRegisterState as UserResult.Error).message.contains("Nickname is already taken")
                            ) {
                                "Nickname already exists."
                            } else null
                        }
                    }
                )
                Spacer(modifier = Modifier.size(22.dp))

                // Full name input field with validation
                SemiTransparentTextField(
                    value = fullName,
                    onValueChange = { viewModel.updateFullName(it) },
                    placeholder = "Full Name",
                    isError = registerState is RegisterValidationResult.EmptyFullName,
                    errorMessage = when (registerState) {
                        is RegisterValidationResult.EmptyFullName -> "Full name can not be blank!"
                        else -> null
                    }
                )
                Spacer(modifier = Modifier.size(22.dp))

                // Bio input field (optional)
                SemiTransparentTextField(
                    value = bio,
                    onValueChange = { viewModel.updateBio(it) },
                    placeholder = "Bio"
                )
                Spacer(modifier = Modifier.size(50.dp))

                // Complete setup button
                Button(
                    onClick = {
                        Log.d("ProfileSetupScreen", "Finish button clicked, user: ${viewModel.getCurrentUser() ?: "null"}")
                        viewModel.getCurrentUser()?.let { user ->
                            viewModel.completeRegister(
                                uid = user.uid,
                                fullName = fullName,
                                nickname = nickname,
                                bio = bio,
                                profileImageUrl = profileImageUri?.toString() ?: "",
                                context = context
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_button)),
                    enabled = !isLoading && nickname.isNotBlank() && fullName.isNotBlank()
                ) {
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Finish", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
