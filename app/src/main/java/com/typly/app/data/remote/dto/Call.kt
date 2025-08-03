package com.typly.app.data.remote.dto

import com.typly.app.domain.repository.CallType
import kotlinx.serialization.Serializable

/**
 * Represents a single call session within the application.
 *
 * This data class serves as a model for call documents stored in Firestore.
 * It contains all the necessary information to track the state and participants of a call.
 * It is marked as [Serializable] for potential use with Kotlinx Serialization and
 * implements [java.io.Serializable] to be passed between components, like navigation arguments.
 *
 * @property callId The unique identifier for the call session.
 * @property callerId The unique ID of the user who initiated the call.
 * @property recieverId The unique ID of the user who is receiving the call.
 * @property callType The type of the call (e.g., [CallType.AUDIO] or [CallType.VIDEO]).
 * @property status The current state of the call, represented by [CallStatus].
 * @property timeStamp The timestamp (in milliseconds) when the call state was last updated.
 * @property duration The total duration of the call in seconds. This is nullable as it only applies to active/ended calls.
 */
@Serializable
data class Call(
    val callId: String = "",
    val callerId: String = "",
    val recieverId: String = "",
    val callType: CallType = CallType.AUDIO,
    val status: CallStatus = CallStatus.PENDING,
    val timeStamp : Long = System.currentTimeMillis(),
    val duration: Int?=0

): java.io.Serializable {
    /**
     * A no-argument constructor required by Firebase Firestore for deserialization
     * of documents into [Call] objects.
     */
    constructor() : this(
        callId = "",
        callerId = "",
        recieverId = "",
        callType = CallType.AUDIO,
        status = CallStatus.PENDING,
        timeStamp = 0L,
        duration = 0
    )
}

/**
 * Defines the possible states in the lifecycle of a call.
 */
enum class CallStatus {
    /**
     * The initial state before a call is initiated. Not typically stored in Firestore.
     */
    IDLE,

    /**
     * The call has been initiated by the caller and is waiting for the receiver to answer.
     */
    PENDING,

    /**
     * The call has been answered and is currently in progress.
     */
    ACTIVE,

    /**
     * The receiver has declined the incoming call.
     */
    REJECTED,

    /**
     * The call was successfully completed and terminated by one of the participants.
     */
    ENDED,

    /**
     * The receiver did not answer the call within a certain time frame.
     */
    MISSED
}
