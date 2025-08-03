package com.typly.app.presentation.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typly.app.data.remote.dto.Call
import com.typly.app.data.remote.dto.CallStatus
import kotlinx.coroutines.delay


/**
 * Displays a full-screen UI for an incoming call, presenting the user with
 * options to accept or reject the call.
 *
 * This screen is responsible for:
 * - Showing the caller's name and the type of call.
 * - Providing clear "Accept" and "Reject" actions.
 * - Automatically closing if the call is terminated remotely (e.g., caller hangs up).
 *
 * @param call The [Call] data object containing details about the incoming call.
 * @param callerName The display name of the user who is calling.
 * @param callViewModel The ViewModel that manages the state and actions for the call.
 * @param onAccept A callback function that is invoked when the user accepts the call,
 * typically for navigating to the active call screen.
 * @param onReject A callback function that is invoked when the call is rejected or
 * terminated, used for navigating back.
 */
@Composable
fun IncomingCallScreen(
    call: Call,
    callerName: String,
    callViewModel: CallViewModel,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    // Collect the overall UI state from the ViewModel to react to call status changes.
    val uiState by callViewModel.uiState.collectAsState()

    /**
     * A side-effect that observes the call status from the ViewModel.
     *
     * It automatically dismisses the screen under several conditions:
     * - If the call is remotely [CallStatus.ENDED], [CallStatus.REJECTED], or [CallStatus.MISSED].
     * - If the call state becomes [CallStatus.IDLE] and the `incomingCall` object is cleared,
     * which can happen if the connection is lost or the call data becomes invalid.
     * This prevents the screen from being stuck if the caller hangs up before the user can respond.
     */
    LaunchedEffect(uiState.callStatus, uiState.incomingCall) {
        // Only exit if call status is explicitly ended/rejected/missed AND we don't have an incoming call
        when (uiState.callStatus) {
            CallStatus.ENDED, CallStatus.REJECTED, CallStatus.MISSED -> {
                delay(1500) // Give user time to see the status
                callViewModel.clearIncomingCall() // Clean up the state in the ViewModel.
                onReject() // Navigate back
            }
            CallStatus.IDLE -> {
                // Only navigate back if the incoming call object has been explicitly cleared.
                if (uiState.incomingCall == null) {
                    delay(500)
                    callViewModel.clearIncomingCall()
                    onReject()
                }
            }
            // Do nothing for ongoing states like PENDING or ACTIVE.
            CallStatus.PENDING, CallStatus.ACTIVE -> {}

        }
    }

    // Main layout container with a black background.
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
            // Static text indicating the type of notification.
            Text(
                text = "📞 Incoming Call",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )

            // Displays the name of the caller.
            Text(
                text = callerName,
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            // Specifies the type of call (e.g., Audio Call).
            Text(
                text = "Audio Call",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Row containing the action buttons (Reject and Accept).
            Row(
                horizontalArrangement = Arrangement.spacedBy(80.dp)
            ) {
                // Floating action button to reject the call.
                FloatingActionButton(
                    onClick = {
                        callViewModel.rejectCall(call.callId)
                        callViewModel.clearIncomingCall() // Immediately clear state.
                        onReject() // Navigate back.
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    containerColor = Color.Red
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "Reject Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Floating action button to accept the call.
                FloatingActionButton(
                    onClick = {
                        callViewModel.answerCall(call.callId)
                        onAccept() // Navigate to the active call screen.
                    },
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape),
                    containerColor = Color.Green
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Accept Call",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
} 
