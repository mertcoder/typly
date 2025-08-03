package com.typly.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

/**
 * Fullscreen image viewer composable for displaying images in modal dialog.
 * 
 * Provides an immersive fullscreen experience for viewing images with loading states,
 * error handling, and dismiss functionality. Features dark overlay background
 * and close button for user interaction.
 * 
 * @param imageUrl URL of the image to be displayed
 * @param isVisible Whether the viewer should be shown
 * @param onDismiss Callback triggered when viewer should be dismissed
 */
@Composable
fun FullscreenImageViewer(
    imageUrl: String,
    isVisible: Boolean,
    onDismiss: () -> Unit
) {
    if (isVisible) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f))
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(imageUrl)
                
                // Loading indicator
                if (painter.state is AsyncImagePainter.State.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(48.dp),
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Loading image...",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                        }
                    }
                }
                
                // Full screen image
                Image(
                    painter = painter,
                    contentDescription = "Full Screen Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .clickable(enabled = false) { }, // Prevent click-through to dismiss
                    contentScale = ContentScale.Fit
                )
                
                // Close button
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Error state
                if (painter.state is AsyncImagePainter.State.Error) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "❌",
                                fontSize = 48.sp
                            )
                            Text(
                                text = "Failed to load image",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Tap to close",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
