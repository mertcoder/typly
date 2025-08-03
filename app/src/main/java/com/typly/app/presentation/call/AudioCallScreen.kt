package com.typly.app.presentation.call

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typly.app.domain.repository.CallResult
import kotlinx.coroutines.delay
import pub.devrel.easypermissions.EasyPermissions

/**
 * A composable function that displays the user interface for an active audio call.
 *
 * This screen handles the entire lifecycle of a call, including:
 * - Requesting necessary permissions (microphone, etc.).
 * - Displaying the call status (connecting, active, ended).
 * - Showing the other participant's name and the call duration.
 * - Providing call controls like muting and ending the call.
 * - Automatically navigating away when the call is terminated.
 *
 * @param targetUserId The unique ID of the other user in the call.
 * @param isReceiver A boolean flag indicating if the current user is the one receiving the call.
 * This affects the initial logic for starting or joining the call.
 * @param callViewModel The ViewModel that manages the call state and business logic.
 * @param onCallEnd A callback function that is invoked to navigate away from the screen when the call ends.
 */
@Composable
fun AudioCallScreen(
    targetUserId: String,
    isReceiver: Boolean = false,
    callViewModel: CallViewModel,
    onCallEnd: () -> Unit
) {
    // State holders for UI and call logic
    val callState by callViewModel.callState.collectAsState(initial = null)
    val isConnected by callViewModel.isConnected.collectAsState(initial = false)
    val isInCall by callViewModel.isInCall.collectAsState(initial = false)
    val context = LocalContext.current
    val currentCallState by callViewModel.callState.collectAsState(initial = null)

    // UI-specific states
    var isMuted by remember { mutableStateOf(false) }
    var showPermissionError by remember { mutableStateOf(false) }
    var duration by remember { mutableIntStateOf(0) }
    var targetUserName by remember { mutableStateOf<String?>("Arayor...") }



    /**
     * Fetches the nickname of the target user when the screen is first composed
     * or when the targetUserId changes.
     */
    LaunchedEffect(targetUserId) {
        // Since getUserById is now in CallRepository, we access it via the ViewModel.
        // Assuming CallViewModel has a reference to the repository.
        // If not, you should add a method in CallViewModel to expose this.
        val userNickname = callViewModel.getUserNicknameById(targetUserId)
        targetUserName = userNickname ?: "Unknown User"
    }


    // Required permissions for making an audio call, adapting to the Android version.
    val requiredPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.MODIFY_AUDIO_SETTINGS
    ).apply {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            add(Manifest.permission.READ_PHONE_STATE)
        }
    }.toTypedArray()

    // ActivityResultLauncher for handling permission requests.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            callViewModel.startAudioCall(targetUserId)
        } else {
            showPermissionError = true
        }
    }


    /**
     * Tracks the call duration by incrementing a counter every second
     * while the call is connected and active.
     */
    LaunchedEffect(isConnected, isInCall) {
        if (isConnected && isInCall) {
            duration = 0
            while (isConnected && isInCall) {
                delay(1000) // Update every second
                duration += 1
            }
        }
    }


    /**
     * Handles automatic navigation away from the screen when the call is no longer active.
     * It differentiates logic for the caller and the receiver to ensure a smooth exit.
     */
    LaunchedEffect(isInCall, isConnected, isReceiver) {
        if (isReceiver) {
            // RECEIVER: Only exit if definitely not in call AND not trying to connect
            if (!isInCall && !isConnected && currentCallState !is CallResult.Loading) {
                kotlinx.coroutines.delay(3000) // Give time for connection
                if (!isInCall && !isConnected) {
                    onCallEnd()
                }
            }
        } else {
            // CALLER: Only navigate if definitely not in call and not trying to connect
            if (!isInCall && currentCallState !is CallResult.Loading) {
                kotlinx.coroutines.delay(1000)
                if (!isInCall) {
                    onCallEnd()
                }
            }
        }
    }


    /**
     * Checks for permissions when the screen is launched.
     * If the user is the caller, it requests permissions and starts the call.
     * If the user is the receiver, it just checks if permissions are already granted.
     */
    LaunchedEffect(targetUserId, isReceiver) {
        if (isReceiver) {
            // Receiver mode: check for permissions, show error if not granted.
            if (!EasyPermissions.hasPermissions(context, *requiredPermissions)) {
                showPermissionError = true
            }
        } else {
            // Caller mode: start call if permissions are granted, otherwise request them.
            if (EasyPermissions.hasPermissions(context, *requiredPermissions)) {
                callViewModel.startAudioCall(targetUserId)
            } else {
                permissionLauncher.launch(requiredPermissions)
            }
        }
    }


    // Display an error dialog if required permissions are not granted.
    if (showPermissionError) {
        AlertDialog(
            onDismissRequest = { 
                showPermissionError = false
                onCallEnd()
            },
            title = { Text("❌ Permissions Required") },
            text = { 
                Text("Audio call cannot start without microphone permissions.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermissionError = false
                        permissionLauncher.launch(requiredPermissions)
                    }
                ) {
                    Text("Try Again")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermissionError = false
                        onCallEnd()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    // Main UI for the call screen.
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            // Display the name of the person being called/calling.
            Text(
                text = targetUserName ?: "",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            // Dynamic text to show the current status of the call.
            Text(
                text = when {
                    !isInCall -> "📞 Call Ended"
                    isConnected -> "Connected"
                    isReceiver -> "Preparing for a call..."
                    callState is CallResult.Loading -> "Connecting..."
                    callState is CallResult.Success -> "Calling..."
                    callState is CallResult.Error -> "Call Failed"
                    else -> "Preparing for a call..."
                },
                color = when {
                    !isInCall || callState is CallResult.Error -> Color.Red
                    isConnected -> Color.Green
                    else -> Color.White
                },
                fontSize = 24.sp
            )

            // Display the call duration timer when connected.
            if (isConnected) {
                val minutes = duration / 60
                val seconds = duration % 60
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Call control buttons (Mute and End Call).
            Row(
                horizontalArrangement = Arrangement.spacedBy(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Mute button is only visible when the call is connected.
                if (isConnected) {
                    FloatingActionButton(
                        onClick = {
                            isMuted = !isMuted
                            callViewModel.muteAudio(isMuted)
                        },
                        containerColor = if (isMuted) Color.Red else Color.DarkGray
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                            contentDescription = "Mute",
                            tint = Color.White
                        )
                    }
                }

                // Button to end the call.
                FloatingActionButton(
                    onClick = {
                        callViewModel.endCall()
                        // onCallEnd is triggered by the LaunchedEffect to ensure a clean exit.
                    },
                    containerColor = Color.Red,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
