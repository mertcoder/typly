package com.typly.app.data.remote.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Represents the data model for a single message document within a chat's subcollection in Firestore.
 *
 * @property id The unique identifier for the message document.
 * @property chatId The ID of the parent [Chat] document this message belongs to.
 * @property senderId The ID of the user who sent the message.
 * @property receiverId The ID of the user who is the intended recipient of the message.
 * @property content The main text content of the message. Can be empty if it's a media message.
 * @property media A URL pointing to any attached media (e.g., an image in Firebase Storage). Empty if it's a text message.
 * @property timestamp A server timestamp indicating when the message was sent.
 * @property isRead A boolean flag indicating if the recipient has read the message. Uses [@PropertyName] for Firestore compatibility.
 * @property status The delivery status of the message, represented by the [MessageStatus] enum.
 */
@Serializable
data class Message(
    val id: String = "",
    val chatId: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val content: String = "",
    val media: String = "",
    val timestamp: Long = System.currentTimeMillis(),

    @get:PropertyName("isRead")
    @set:PropertyName("isRead")
    var isRead: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT
) : java.io.Serializable {
    /**
     * No-argument constructor required for Firestore data deserialization.
     */
    constructor() : this(
        id = "",
        chatId = "",
        senderId = "",
        receiverId = "",
        content = "",
        media = "",
        timestamp = 0L,
        isRead = false,
        status = MessageStatus.SENT
    )
}

/**
 * Defines the possible delivery statuses for a [Message].
 */
enum class MessageStatus {
    /** The message is currently being sent from the client. */
    SENDING,
    /** The message has been successfully saved to the server. */
    SENT,
    /** The message has been delivered to the recipient's device. */
    DELIVERED,
    /** The recipient has read the message. */
    READ,
    /** An error occurred while trying to send the message. */
    ERROR
}
