package com.typly.app.presentation.components

import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Scale
import com.typly.app.R
import kotlinx.coroutines.launch

/**
 * An interactive icon that can be spun with drag gestures or display a selected image.
 * 
 * When no image is selected, displays a default profile icon that can be rotated
 * by dragging horizontally with momentum-based animation. When an image is provided,
 * displays the image in a circular frame without spin functionality. Includes
 * click handling for both states.
 * 
 * @param modifier Modifier to be applied to the icon container
 * @param imageUri URI of selected image to display, or null for default spinning icon
 * @param onClick Callback triggered when the icon is clicked
 */
@Composable
fun SpinnableIcon(
    modifier: Modifier = Modifier,
    imageUri: Uri? = null,
    onClick: () -> Unit = {}
) {
    val rotation = remember { Animatable(0f) }
    var lastDragDelta by remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    if (imageUri != null) {
        // Display selected image (without spinning animation)
        val painter = rememberAsyncImagePainter(
            ImageRequest.Builder(context)
                .data(data = imageUri)
                .apply {
                    crossfade(true)
                    scale(Scale.FILL)
                }.build()
        )
        
        Image(
            painter = painter,
            contentDescription = "Selected Profile Image",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .clip(CircleShape)
                .clickable(
                    indication = null, // Remove ripple effect
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
        )
    } else {
        // Display default icon with spinning animation
        Icon(
            painter = painterResource(id = R.drawable.ic_select_pp),
            contentDescription = "3D Flip Icon",
            modifier = modifier
                .clip(CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            lastDragDelta = dragAmount.x
                            scope.launch {
                                rotation.snapTo(rotation.value + dragAmount.x)
                            }
                        },
                        onDragEnd = {
                            val decay = exponentialDecay<Float>()
                            scope.launch {
                                rotation.animateDecay(lastDragDelta * 30f, decay)
                            }
                        }
                    )
                }
                .graphicsLayer {
                    rotationY = rotation.value % 360f
                    cameraDistance = 12 * density
                }
                .clickable(
                    indication = null, // Remove ripple effect
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                ),
            tint = Color.Unspecified
        )
    }
}
