package com.typly.app.presentation.components

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.typly.app.R
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * A profile image picker component with permission handling and gallery access.
 * 
 * Provides a circular image picker that handles storage permissions automatically
 * based on the Android version. Shows either the selected image or a default
 * placeholder icon. When clicked, requests necessary permissions and opens the
 * device gallery for image selection.
 * 
 * @param imageUri Currently selected image URI, or null if no image is selected
 * @param onImageSelected Callback triggered when an image is selected from gallery
 * @param modifier Modifier to be applied to the image picker container
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ProfileImagePicker(
    imageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // Determine which permission to request based on Android version
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    
    // Permission handling
    val permissionState = rememberPermissionState(permission = permission)
    
    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        onImageSelected(uri)
        uri?.let {
            Log.d("ProfileImagePicker", "Selected image URI: $it")
        }
    }
    
    // Image painter setup
    val painter = if (imageUri != null) {
        rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(data = imageUri)
                .apply {
                    crossfade(true)
                    scale(Scale.FILL)
                }.build()
        )
    } else {
        painterResource(id = R.drawable.ic_select_pp)
    }
    
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(180.dp)
            .clip(CircleShape)
            .border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            .clickable {
                if (permissionState.status.isGranted) {
                    // Permission is granted, launch gallery
                    galleryLauncher.launch("image/*")
                } else {
                    // Request permission
                    permissionState.launchPermissionRequest()
                }
            }
    ) {
        Image(
            painter = painter,
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    }
} 
