package com.typly.app.services.fcm

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Singleton class for managing Firebase Cloud Messaging (FCM) tokens.
 * 
 * Handles FCM token retrieval, synchronization with Firestore database,
 * and token lifecycle management. Ensures that the server always has
 * the most current device token for push notifications.
 * 
 * @property firebaseMessaging Firebase Messaging instance for token operations
 * @property auth Firebase Authentication for user identification
 * @property firestore Firestore database for token storage
 */
@Singleton
class TokenManager @Inject constructor(
    private val firebaseMessaging: FirebaseMessaging,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    /**
     * Retrieves the current FCM token for this device.
     * 
     * Asynchronously fetches the Firebase Cloud Messaging token that uniquely
     * identifies this app installation for push notification delivery.
     * 
     * @return The current FCM token string, or empty string if retrieval fails
     */
    suspend fun getCurrentDeviceToken(): String = withContext(Dispatchers.IO) {
        try {
            Tasks.await(firebaseMessaging.token)
        } catch (e: Exception) {
            Log.e("TokenManager", "Error getting device token: ${e.message}", e)
            ""
        }
    }

    /**
     * Synchronizes the current FCM token with the Firestore database.
     * 
     * Compares the current device token with the stored token in Firestore
     * and updates the database if they differ. This ensures the server
     * always has the latest token for push notifications.
     * 
     * @return True if the token was updated in the database, false if no update was needed
     */
    suspend fun syncFcmToken(): Boolean = withContext(Dispatchers.IO) {
        val uid = auth.currentUser?.uid ?: return@withContext false

        val currentToken = getCurrentDeviceToken()

        val userDocRef = firestore.collection("users").document(uid)
        val snapshot = Tasks.await(userDocRef.get())

        val savedToken = snapshot.getString("fcmToken")
        Log.d("TokenManager", "Current token: $currentToken")
        Log.d("TokenManager", "Database token: $savedToken")
        return@withContext if (savedToken != currentToken) {
            userDocRef.update("fcmToken", currentToken).await()
            Log.d("TokenManager", "Token was expired and has been updated.")
            true
        } else {
            Log.d("TokenManager", "Token already in use, no update needed.")
            false
        }
    }


}
