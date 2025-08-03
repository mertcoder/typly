package com.typly.app.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Represents the data model for a chat conversation document in Firestore.
 *
 * This class defines the structure of a chat session, including its participants,
 * the last message for preview purposes, and real-time typing status.
 *
 * @property id The unique identifier for the chat document, typically generated from participant IDs.
 * @property participants A list of user IDs participating in this chat.
 * @property lastMessage The most recent [Message] object sent in the chat. Used for display in chat list previews.
 * @property createdAt A server timestamp indicating when the chat was first created.
 * @property updatedAt A server timestamp indicating the last time there was activity (e.g., a new message).
 * @property typingStatus A map where keys are user IDs and values are booleans, indicating if a user is currently typing.
 */
@Serializable
data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val lastMessage: Message? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val typingStatus: Map<String, Boolean> = emptyMap(),
) : java.io.Serializable {
    /**
     * No-argument constructor required for Firestore data deserialization.
     */
    constructor() : this(
        id = "",
        participants = emptyList(),
        lastMessage = null,
        createdAt = 0L,
        updatedAt = 0L,
        typingStatus = emptyMap()
    )
}
