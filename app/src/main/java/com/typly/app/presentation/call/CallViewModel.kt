package com.typly.app.presentation.call

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.data.remote.dto.CallStatus
import com.typly.app.domain.repository.AuthRepository
import com.typly.app.domain.repository.CallRepository
import com.typly.app.domain.repository.CallResult
import com.typly.app.domain.repository.CallType
import com.typly.app.domain.repository.ChatRepository
import com.typly.app.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject

/**
 * ViewModel responsible for managing call-related operations and UI state.
 *
 * This ViewModel handles audio call functionality including:
 * - Initiating outgoing calls
 * - Receiving and answering incoming calls
 * - Managing call status and connection state
 * - Integration with Agora SDK for audio communication
 * - Call state synchronization with Firebase
 *
 * @param callRepository Repository for call-related operations
 * @param chatRepository Repository for chat-related operations
 * @param authRepository Repository for authentication operations
 * @param userRepository Repository for user-related operations
 * @param firestore Firebase Firestore instance
 * @param agoraManager Manager for Agora SDK operations
 */
@HiltViewModel
class CallViewModel @Inject constructor(
    val callRepository: CallRepository,
    val chatRepository: ChatRepository,
    val authRepository: AuthRepository,
    val userRepository: UserRepository,
    val firestore: FirebaseFirestore,
    private val agoraManager: AgoraManager
): ViewModel(), AgoraManager.AgoraConnectionListener {

    /**
     * Internal mutable state flow for call UI state
     */
    private val _uiState = MutableStateFlow(CallUiState())

    /**
     * Exposed immutable state flow for UI consumption
     */
    val uiState = _uiState.asStateFlow()

    // Expose individual state properties for backward compatibility
    val callState = uiState.map { it.callResult }
    val isConnected = uiState.map { it.isConnectedToAgora }
    val incomingCall = uiState.map { it.incomingCall }
    val isInCall = uiState.map { it.isInCall }
    val currentTargetUserId = uiState.map { it.currentTargetUserId }

    /**
     * Job for managing incoming call listener lifecycle
     */
    private var incomingCallListenerJob: Job? = null

    /**
     * Job for managing call status listener lifecycle
     */
    private var callStatusListenerJob: Job? = null

    init {
        // Performance optimized initialization - minimal heavy operations
        agoraManager.initializeEngine()
        agoraManager.setConnectionListener(this)
        startListeningForIncomingCalls()
    }

    /**
     * Retrieves user nickname by user ID.
     *
     * @param userId The unique identifier of the user
     * @return The user's nickname, or null if not found or error occurred
     */
    suspend fun getUserNicknameById(userId: String): String? {
        try {
            return userRepository.getUserNicknameById(userId)
        } catch (e: Exception) {
            Log.e("CallViewModel", "Failed to get user by ID: ${e.message}")
            return null
        }
    }

    /**
     * Starts listening for incoming calls for the current authenticated user.
     *
     * This method sets up a continuous listener that monitors for incoming calls
     * and updates the UI state accordingly. It prevents overwriting incoming call
     * state if the user is already in an active call.
     */
    private fun startListeningForIncomingCalls() {
        // Cancel existing listener to prevent duplicates
        incomingCallListenerJob?.cancel()

        val currentUserId = authRepository.getCurrentUser()?.uid ?: return
        incomingCallListenerJob = viewModelScope.launch {
            callRepository.getIncomingCall(currentUserId).collectLatest { call ->
                // Don't overwrite incoming call if we're already in a call
                if (call != null) {
                    if (!_uiState.value.isInCall || _uiState.value.callId == null) {
                        _uiState.value = _uiState.value.copy(
                            incomingCall = call,
                            currentTargetUserId = call.callerId,
                            callId = call.callId
                        )
                    }
                } else {
                    // Only clear incoming call if we're not in an active call
                    if (!_uiState.value.isInCall) {
                        _uiState.value = _uiState.value.copy(
                            incomingCall = null,
                            currentTargetUserId = null,
                            callId = null
                        )
                    }
                }
            }
        }
    }

    /**
     * Starts listening to call status changes for a specific call.
     *
     * @param callId The unique identifier of the call to monitor
     */
    private fun startListeningToCallStatus(callId: String) {
        // Cancel existing listener to prevent duplicates
        callStatusListenerJob?.cancel()

        callStatusListenerJob = viewModelScope.launch {
            // Add delay to prevent rapid successive calls
            kotlinx.coroutines.delay(500)

            callRepository.listenToCallStatus(callId).collectLatest { call ->
                if (call != null) {
                    when (call.status) {
                        CallStatus.PENDING -> {
                            _uiState.value = _uiState.value.copy(callResult = CallResult.Success("Call is ringing..."))
                        }
                        CallStatus.ACTIVE -> {
                            _uiState.value = _uiState.value.copy(
                                callResult = CallResult.Success("Call connected"),
                                callStatus = CallStatus.ACTIVE
                            )
                        }
                        CallStatus.ENDED, CallStatus.REJECTED, CallStatus.MISSED -> {
                            _uiState.value = _uiState.value.copy(
                                callResult = CallResult.Success("Call ${call.status.name.lowercase()}"),
                                callStatus = CallStatus.IDLE
                            )
                            // Stop listening after call ends
                            callStatusListenerJob?.cancel()
                        }
                        CallStatus.IDLE -> {
                            _uiState.value = _uiState.value.copy(callResult = CallResult.Success("Call idle"))
                        }
                    }
                }
            }
        }
    }

    /**
     * Initiates an audio call to the specified target user.
     *
     * This method prevents duplicate calls and handles the complete call initiation flow
     * including Firebase call creation and Agora channel joining.
     *
     * @param targetUserId The unique identifier of the user to call
     */
    fun startAudioCall(targetUserId: String) {
        // Prevent duplicate calls
        if (_uiState.value.isInCall) {
            return
        }

        agoraManager.setConnectionListener(this)

        // Performance optimized: Single state update instead of multiple
        _uiState.value = _uiState.value.copy(
            callStatus = CallStatus.PENDING,
            currentTargetUserId = targetUserId,
            callId = null,
            statusListenerCallId = null
        )

        val currentUserId = authRepository.getCurrentUser()?.uid
        viewModelScope.launch {
            callRepository.initiateCall(targetUserId, CallType.AUDIO).collectLatest { result ->
                when (result) {
                    is CallResult.Loading -> {
                        _uiState.value = _uiState.value.copy(callResult = result)
                    }
                    is CallResult.Success -> {
                        // Single state update with all changes
                        _uiState.value = _uiState.value.copy(
                            callId = result.data,
                            callResult = result
                        )
                        startListeningToCallStatus(result.data)
                        agoraManager.joinAudioCall(result.data, currentUserId)
                    }
                    is CallResult.Error -> {
                        // Single state update for error case
                        _uiState.value = _uiState.value.copy(
                            callStatus = CallStatus.IDLE,
                            currentTargetUserId = null,
                            callId = null,
                            callResult = result
                        )
                    }
                }
            }
        }
    }

    /**
     * Answers an incoming call.
     *
     * @param callId The unique identifier of the call to answer
     */
    fun answerCall(callId: String) {
        // Prevent duplicate answerCall operations
        if (_uiState.value.callId == callId && _uiState.value.isInCall) {
            return
        }

        agoraManager.setConnectionListener(this)

        // Set target user ID from incoming call
        val incomingCall = _uiState.value.incomingCall
        if (incomingCall != null && incomingCall.callId == callId) {
            _uiState.value = _uiState.value.copy(
                currentTargetUserId = incomingCall.callerId,
                callId = callId,
                callStatus = CallStatus.PENDING
            )
        }

        startListeningToCallStatus(callId)

        val currentUserId = authRepository.getCurrentUser()?.uid
        viewModelScope.launch {
            callRepository.answerCall(callId).collectLatest { result ->
                when (result) {
                    is CallResult.Loading -> {
                        // Handle loading state if needed
                    }
                    is CallResult.Success -> {
                        _uiState.value = _uiState.value.copy(incomingCall = null)
                        agoraManager.joinAudioCall(callId, currentUserId)
                    }
                    is CallResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            callStatus = CallStatus.IDLE,
                            currentTargetUserId = null,
                            callId = null
                        )
                    }
                }
            }
        }
    }

    /**
     * Ends the current active call.
     *
     * This method handles cleanup of both local state and remote call status,
     * disconnects from Agora channel, and resets all call-related state.
     */
    fun endCall() {
        // Performance optimized: Single state update combining all changes
        val callId = _uiState.value.callId

        _uiState.value = _uiState.value.copy(
            isConnectedToAgora = false,
            callStatus = CallStatus.IDLE,
            currentTargetUserId = null,
            callResult = null,
            incomingCall = null,
            callId = null,
            statusListenerCallId = null
        )

        agoraManager.leaveCall()

        // Update Firebase call status
        callId?.let { id ->
            viewModelScope.launch {
                callRepository.endCall(id).collect { result ->
                    // Handle result if needed (minimal logging only)
                }
            }
        }
    }

    /**
     * Toggles audio mute state during a call.
     *
     * @param muted true to mute audio, false to unmute
     */
    fun muteAudio(muted: Boolean) {
        agoraManager.muteAudio(muted)
    }

    /**
     * Clears incoming call state.
     *
     * This method should be called by UI when navigating away from incoming call screen
     * to prevent stale incoming call notifications.
     */
    fun clearIncomingCall() {
        _uiState.value = _uiState.value.copy(
            incomingCall = null,
            callId = null,
            currentTargetUserId = null,
            statusListenerCallId = null
        )
    }

    /**
     * Rejects an incoming call.
     *
     * @param callId The unique identifier of the call to reject
     */
    fun rejectCall(callId: String) {
        viewModelScope.launch {
            try {
                callRepository.rejectCall(callId).collectLatest { result ->
                    when (result) {
                        is CallResult.Success -> {
                            _uiState.value = _uiState.value.copy(
                                callResult = CallResult.Success("Call rejected"),
                                incomingCall = null,
                            )
                            agoraManager.leaveCall()
                            cleanupAfterCallRejection()
                        }
                        is CallResult.Error -> {
                            _uiState.value = _uiState.value.copy(callResult = result)
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    callResult = CallResult.Error("Failed to reject call: ${e.message}")
                )
            }
        }
    }

    /**
     * Performs cleanup operations after a call rejection.
     *
     * Includes a delay to allow UI feedback before resetting state.
     */
    private fun cleanupAfterCallRejection() {
        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            resetAllCallStates()
        }
    }

    /**
     * Resets all call-related state to initial values.
     *
     * This method provides a clean slate for new call operations.
     */
    private fun resetAllCallStates() {
        _uiState.value = CallUiState(
            isConnectedToAgora = false,
            callId = null,
            currentTargetUserId = null,
            incomingCall = null,
            callResult = null,
            callStatus = CallStatus.IDLE,
            statusListenerCallId = null
        )
    }

    // AgoraManager.AgoraConnectionListener implementation

    /**
     * Called when the current user successfully connects to the call.
     *
     * Note: This indicates self-connection, waiting for other user to join.
     */
    override fun onCallConnected() {
        // Self joined - wait for other user
    }

    /**
     * Called when another user joins the call.
     *
     * @param uid The unique identifier of the user who joined
     */
    override fun onUserJoined(uid: Int) {
        _uiState.value = _uiState.value.copy(isConnectedToAgora = true)
    }

    /**
     * Called when a user leaves the call.
     *
     * @param uid The unique identifier of the user who left
     */
    override fun onUserLeft(uid: Int) {
        _uiState.value = _uiState.value.copy(isConnectedToAgora = false)

        viewModelScope.launch {
            if (_uiState.value.isInCall) {
                endCall()
            }
        }
    }

    /**
     * Called when connection to Agora is lost.
     *
     * Triggers call termination if currently in a call.
     */
    override fun onConnectionLost() {
        _uiState.value = _uiState.value.copy(isConnectedToAgora = false)
        if (_uiState.value.isInCall) {
            endCall()
        }
    }

    /**
     * Called when the ViewModel is being destroyed.
     *
     * Performs cleanup of resources including:
     * - Resetting call state
     * - Destroying Agora manager
     * - Canceling active listeners
     */
    override fun onCleared() {
        super.onCleared()
        _uiState.value = _uiState.value.copy(
            callStatus = CallStatus.IDLE,
            incomingCall = null,
            statusListenerCallId = null
        )
        agoraManager.destroy()
        // Clean up listeners
        incomingCallListenerJob?.cancel()
        callStatusListenerJob?.cancel()
    }
}
