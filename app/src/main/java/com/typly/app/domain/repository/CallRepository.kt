package com.typly.app.domain.repository

import com.typly.app.data.remote.dto.Call
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing voice and video call operations.
 *
 * This interface defines the contract for call-related operations including
 * call initiation, answering, rejection, and termination. It supports both
 * audio and video calls and provides real-time call status monitoring
 * through reactive Flow streams.
 *
 * The repository handles the complex call lifecycle management and integrates
 * with underlying communication services to provide seamless calling experience.
 */
interface CallRepository {

    /**
     * Initiates a new call to a target user.
     *
     * This method starts a new call session with the specified user. It creates
     * a call record in the database, notifies the target user, and returns a
     * call ID that can be used to track the call status throughout its lifecycle.
     *
     * @param targetUserId the unique identifier of the user to call
     * @param callType the type of call to initiate (audio or video)
     * @return Flow emitting CallResult with call ID on success or error information
     */
    suspend fun initiateCall(targetUserId: String, callType: CallType): Flow<CallResult<String>>

    /**
     * Answers an incoming call.
     *
     * This method accepts an incoming call by updating the call status and
     * establishing the communication channel between participants. It handles
     * the necessary setup for both audio and video calls.
     *
     * @param callId the unique identifier of the call to answer
     * @return Flow emitting CallResult indicating success or error information
     */
    suspend fun answerCall(callId: String): Flow<CallResult<Unit>>

    /**
     * Rejects an incoming call.
     *
     * This method declines an incoming call by updating the call status and
     * notifying the caller about the rejection. The call record is updated
     * to reflect the rejected state.
     *
     * @param callId the unique identifier of the call to reject
     * @return Flow emitting CallResult indicating success or error information
     */
    suspend fun rejectCall(callId: String): Flow<CallResult<Unit>>

    /**
     * Terminates an active call.
     *
     * This method ends an ongoing call by closing the communication channel,
     * updating the call status, and performing necessary cleanup operations.
     * It can be called by any participant in the call.
     *
     * @param callId the unique identifier of the call to end
     * @return Flow emitting CallResult indicating success or error information
     */
    suspend fun endCall(callId: String): Flow<CallResult<Unit>>

    /**
     * Listens for incoming calls for a specific user.
     *
     * This method establishes a real-time listener for incoming calls targeted
     * at the specified user. It continuously monitors for new call invitations
     * and emits them as they arrive, enabling the UI to show incoming call screens.
     *
     * @param userId the unique identifier of the user to monitor for incoming calls
     * @return Flow emitting CallResult with incoming Call objects or null when no calls
     */
    suspend fun listenForIncomingCalls(userId: String): Flow<CallResult<Call?>>

    /**
     * Retrieves any pending incoming call for a user.
     *
     * This method checks for any existing incoming call that hasn't been
     * answered or rejected yet. It's useful for handling app state restoration
     * or checking call status on app launch.
     *
     * @param userId the unique identifier of the user to check for incoming calls
     * @return Flow emitting the incoming Call object or null if no pending calls
     */
    suspend fun getIncomingCall(userId: String): Flow<Call?>

    /**
     * Listens to real-time status changes for a specific call.
     *
     * This method monitors the status of a specific call throughout its lifecycle,
     * emitting updates when the call state changes (e.g., ringing, answered, ended).
     * It's essential for keeping the call UI synchronized with the actual call state.
     *
     * @param callId the unique identifier of the call to monitor
     * @return Flow emitting updated Call objects reflecting current status
     */
    suspend fun listenToCallStatus(callId: String): Flow<Call?>
}

/**
 * Sealed class representing the result of call operations.
 *
 * This class provides a type-safe way to handle different states of call operations
 * including call initiation, answering, rejection, and status monitoring.
 * It follows the common Result pattern used throughout the application.
 *
 * @param T the type of data returned on successful call operation
 */
sealed class CallResult<out T> {

    /**
     * Represents a successful call operation.
     *
     * @param data the result data of the successful call operation
     */
    data class Success<T>(val data: T): CallResult<T>()

    /**
     * Represents a failed call operation.
     *
     * @param message descriptive error message explaining why the call operation failed
     */
    data class Error(val message: String): CallResult<Nothing>()

    /**
     * Represents an ongoing call operation.
     *
     * This state indicates that a call request is currently being processed
     * and the result is not yet available.
     */
    object Loading : CallResult<Nothing>()
}

/**
 * Enumeration representing the different types of calls supported by the application.
 *
 * This enum distinguishes between audio-only and video calls, allowing the
 * application to handle different call types with appropriate UI and functionality.
 */
enum class CallType {

    /**
     * Audio-only call type.
     *
     * Represents a voice call without video transmission, using less bandwidth
     * and providing basic voice communication functionality.
     */
    AUDIO,

    /**
     * Video call type.
     *
     * Represents a call with both audio and video transmission, providing
     * full multimedia communication experience.
     */
    VIDEO
}
