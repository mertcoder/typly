package com.typly.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter


/**
 * Message input bar composable with text input, image preview, and action buttons.
 * 
 * Provides a comprehensive messaging interface including text input field,
 * image attachment preview, attachment button, and send functionality.
 * Features glassmorphism design with dynamic height based on content.
 * 
 * @param messageText Current text content of the message
 * @param onMessageTextChange Callback triggered when message text changes
 * @param onSendMessage Callback triggered when send button is pressed
 * @param onAttachmentClick Callback triggered when attachment button is pressed
 * @param selectedImageUri URI of currently selected image for preview, null if none
 * @param onImageRemove Callback triggered when image remove button is pressed
 * @param isLoading Whether the message is currently being sent
 */
@Composable
fun MessageInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onAttachmentClick: () -> Unit = {},
    selectedImageUri: Uri? = null,
    onImageRemove: () -> Unit = {},
    isLoading: Boolean = false
) {
    // Glass effect container
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp)
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Image preview section
            selectedImageUri?.let { uri ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image preview
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                Color.White.copy(alpha = 0.3f),
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        
                        // Remove button
                        IconButton(
                            onClick = onImageRemove,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove Image",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Image info text
                    Text(
                        text = "Image attached",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Message input row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
            // Message input field
            BasicTextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp, max = 120.dp)
                    .padding(end = 12.dp),
                textStyle = TextStyle(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                ),
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSendMessage() }
                ),
                cursorBrush = SolidColor(Color.White.copy(alpha = 0.8f)),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "Message",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )


            IconButton(
                onClick = onAttachmentClick,
                enabled = true,
                modifier = Modifier.size(40.dp)
            ){
                Icon(
                    imageVector = Icons.Default.AttachFile, // Replace with attachment icon
                    contentDescription = "Attach",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }

            // Send button
            IconButton(
                onClick = onSendMessage,
                enabled = messageText.isNotBlank() && !isLoading,
                modifier = Modifier.size(40.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White.copy(alpha = 0.6f),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (messageText.isNotBlank())
                            Color.White.copy(alpha = 0.8f)
                        else Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            } // Row kapanış
        } // Column kapanış
    } // Box kapanış
} // Function kapanış
