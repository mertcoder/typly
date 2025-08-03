package com.typly.app.presentation.components

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/**
 * A composable that displays a simple bottom sheet overlay for attachment options.
 *
 * Currently, it provides a single option to select an image from the device's gallery.
 * It handles the necessary storage permissions for modern Android versions.
 * The UI has a semi-transparent "glass" effect.
 *
 * @param isVisible Controls the visibility of the bottom sheet.
 * @param onDismiss A callback invoked when the user clicks the background to dismiss the sheet.
 * @param onImageSelected A callback that provides the [Uri] of the selected image.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun AttachmentOptionsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onImageSelected: (Uri) -> Unit
) {
    // Determine the appropriate storage permission based on the Android SDK version.
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    // State for managing the runtime permission request.
    val permissionState = rememberPermissionState(permission = permission)

    // Activity result launcher for picking content from the gallery.
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            onImageSelected(selectedUri)
            onDismiss()
        }
    }

    if (isVisible) {
        // Full-screen overlay to capture background clicks for dismissal.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Container for the attachment options, positioned above the input bar.
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 100.dp)
                    .clickable(enabled = false) { }, // Prevent clicks from passing through.
                contentAlignment = Alignment.Center
            ) {
                // The visual "glass effect" row for the image selection button.
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(25.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.1f),
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .clickable {
                            if (permissionState.status.isGranted) {
                                // If permission is already granted, launch the gallery.
                                galleryLauncher.launch("image/*")
                            } else {
                                // Otherwise, request the permission.
                                permissionState.launchPermissionRequest()
                            }
                        }
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Select Image",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Image",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
