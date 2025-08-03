package com.typly.app.services.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.typly.app.R
import com.typly.app.util.AppState
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

/**
 * Firebase Cloud Messaging service for handling push notifications and token updates.
 * 
 * Extends FirebaseMessagingService to handle incoming push notifications,
 * token refresh events, and notification display logic. Implements smart
 * notification filtering to avoid showing notifications when user is actively
 * viewing the relevant chat.
 */
@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new FCM token is generated for the device.
     * 
     * This occurs on app install, restore, or when the token is refreshed.
     * The new token should be sent to your server to ensure continued
     * push notification delivery.
     * 
     * @param token The new FCM registration token
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // You can call a function here to send the new token to your server.
        Log.d("FCM", "New token received: $token")
    }

    /**
     * Called when a message is received from Firebase Cloud Messaging.
     * 
     * Processes incoming push notifications and decides whether to display
     * them based on the current app state. Implements smart filtering to
     * avoid showing notifications for the currently active chat.
     * 
     * @param remoteMessage The received message containing notification data and payload
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Get data from the remote message
        val title = remoteMessage.notification?.title ?: "New Message"
        val body = remoteMessage.notification?.body ?: "You have a new message."

        // Get chatId from the data payload sent by the Cloud Function
        val chatId = remoteMessage.data["chatId"]

        // Check if the user is already in this chat
        if (chatId != null && chatId == AppState.activeChatId) {
            // If the user is in the current chat, do not show the notification.
            Log.d("FCM", "User is already on the chat screen. Notification will not be shown. Chat ID: $chatId")
            return // Stop execution here
        }

        // If the user is not in the chat, show the notification.
        Log.d("FCM", "Showing new notification: Title=$title, ChatID=$chatId")
        showNotification(title, body)
    }

    /**
     * Displays a push notification to the user.
     * 
     * Creates and shows a notification with the provided title and message.
     * Handles notification channel creation for Android 8+ and sets appropriate
     * notification properties for visibility and user interaction.
     * 
     * @param title The notification title
     * @param message The notification body text
     */
    private fun showNotification(title: String, message: String) {
        val channelId = "message_channel"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel for Android 8+ (Oreo)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Message Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build the notification without any click action (Intent)
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.typlylogo)
            .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for older versions
            .setAutoCancel(true)
            .build()

        // Display the notification
        notificationManager.notify(Random.nextInt(), notification)
    }
}
