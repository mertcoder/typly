package com.typly.app.data.remote.dto

import com.google.firebase.firestore.PropertyName
import kotlinx.serialization.Serializable

/**
 * Represents the data model for a user profile document in the `users` collection in Firestore.
 *
 * @property id The unique user identifier, typically the UID from Firebase Authentication.
 * @property email The user's email address.
 * @property fullName The user's full name as provided during profile setup.
 * @property nickname The user's unique, public-facing username.
 * @property bio A short biography or status message written by the user.
 * @property profileImageUrl A nullable URL pointing to the user's profile image in Firebase Storage.
 * @property fcmToken The Firebase Cloud Messaging registration token used for sending push notifications.
 * @property isAnonymous A flag indicating whether the user is authenticated anonymously.
 * @property createdAt A server timestamp indicating when the user account was created.
 * @property lastActive A server timestamp indicating the user's last known activity time.
 * @property recentChats A list of `chatId`s the user is a participant in, used for populating the chat list.
 * @property isProfileComplete A flag indicating if the user has completed the initial profile setup process.
 * @property isOnline A boolean flag used for real-time presence detection, indicating if the user is currently online.
 */
@Serializable
data class User(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val nickname: String = "",
    val bio: String = "",
    val profileImageUrl: String? = null,
    val fcmToken: String? = null,

    @get:PropertyName("isAnonymous")
    @set:PropertyName("isAnonymous")
    var isAnonymous: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis(),
    val recentChats: List<String> = emptyList(),

    @get:PropertyName("isProfileComplete")
    @set:PropertyName("isProfileComplete")
    var isProfileComplete: Boolean = false,

    @get:PropertyName("isOnline")
    @set:PropertyName("isOnline")
    var isOnline: Boolean = false,

    ) : java.io.Serializable {
    /**
     * No-argument constructor required for Firestore data deserialization.
     */
    constructor() : this(
        id = "",
        email = "",
        fullName = "",
        nickname = "",
        bio = "",
        profileImageUrl = null,
        fcmToken = null,
        isAnonymous = false,
        createdAt = 0L,
        lastActive = 0L,
        recentChats = emptyList(),
        isProfileComplete = false,
        isOnline = false,
    )
}
