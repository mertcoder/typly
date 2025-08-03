package com.typly.app.presentation.call

import com.typly.app.data.remote.dto.Call
import com.typly.app.data.remote.dto.CallStatus
import com.typly.app.domain.repository.CallResult

/**
 * Represents the UI state for call-related functionality.
 *
 * This data class encapsulates all the necessary state information
 * for managing voice/video calls within the application.
 *
 * @property callStatus The current status of the call (IDLE, PENDING, ACTIVE, etc.)
 * @property isConnectedToAgora Whether the client is connected to Agora service
 * @property isMuted Whether the microphone is currently muted
 * @property callId The unique identifier of the current call, if any
 * @property statusListenerCallId The call ID being monitored for status changes
 * @property currentTargetUserId The user ID of the person being called or calling
 * @property incomingCall The incoming call object, if there's an incoming call
 * @property callResult The result of call operations (success, error, loading)
 * @property error Error message if any call operation failed
 */
data class CallUiState(
    val callStatus: CallStatus = CallStatus.IDLE,
    val isConnectedToAgora: Boolean = false,
    val isMuted: Boolean = false,
    val callId: String? = null,
    val statusListenerCallId: String? = null,
    val currentTargetUserId: String? = null,
    val incomingCall: Call? = null,
    val callResult: CallResult<String>? = null,
    val error: String? = null,
) {
    /**
     * Indicates whether the user is currently in an active call session.
     *
     * A user is considered to be "in call" if the call status is either
     * PENDING (call is being established) or ACTIVE (call is ongoing).
     *
     * @return true if the user is in a call, false otherwise
     */
    val isInCall: Boolean
        get() = callStatus in listOf(CallStatus.PENDING, CallStatus.ACTIVE)
}
