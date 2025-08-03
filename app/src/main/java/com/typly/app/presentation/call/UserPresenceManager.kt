package com.typly.app.presentation.call

import android.app.Application
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.typly.app.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the user's online presence status across the entire application lifecycle.
 *
 * This singleton class observes the application's lifecycle using [ProcessLifecycleOwner]
 * to automatically update the user's status to "online" when the app is in the foreground
 * and "offline" when it goes to the background. It interacts directly with Firestore
 * to update the `isOnline` and `lastSeen` fields in the user's document.
 *
 * @property userRepository Repository for user-related data operations.
 * @property firebaseAuth Service for accessing the current authenticated user.
 * @property firestore The Firebase Firestore instance for database operations.
 * @property application The application context.
 */
@Singleton
class UserPresenceManager @Inject constructor(
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val application: Application
) : DefaultLifecycleObserver {

    companion object {
        private const val TAG = "UserPresenceManager"
        private const val FIVE_MINUTES_IN_MS = 5 * 60 * 1000L
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    /**
     * Initializes the presence manager by attaching it as an observer to the application's lifecycle.
     * This should be called once when the application starts.
     */
    fun initialize() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d(TAG, "UserPresenceManager initialized and observing app lifecycle.")
    }

    /**
     * Manually sets the user's status to online.
     * Useful for forcing an online status immediately after login or registration,
     * before the lifecycle `onStart` event is triggered.
     */
    fun setUserOnlineManually() {
        Log.d(TAG, "Manually setting user to online.")
        setUserOnline()
    }

    /**
     * Retrieves a real-time flow of a user's `lastSeen` timestamp from Firestore.
     *
     * @param userId The ID of the user whose last seen time is to be observed.
     * @return A [Flow] that emits the `lastSeen` timestamp as a [Long] whenever it changes.
     */
    fun getUserLastSeen(userId: String): Flow<Long> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Error getting user last seen for $userId", e)
                        trySend(0L)
                        return@addSnapshotListener
                    }
                    val lastSeen = snapshot?.getLong("lastSeen") ?: 0L
                    trySend(lastSeen)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in getUserLastSeen for $userId", e)
            trySend(0L)
        }
        awaitClose { listenerRegistration?.remove() }
    }

    /**
     * Formats a timestamp into a human-readable "time ago" string.
     *
     * @param lastSeenTimestamp The timestamp in milliseconds.
     * @return A formatted string like "Seconds Ago", "5 minutes ago", etc.
     */
    fun formatLastSeen(lastSeenTimestamp: Long): String {
        if (lastSeenTimestamp == 0L) return "Offline"

        val timeDiff = System.currentTimeMillis() - lastSeenTimestamp
        return when {
            timeDiff < 60_000 -> "Seconds Ago"
            timeDiff < 3_600_000 -> "${timeDiff / 60_000} minutes ago"
            timeDiff < 86_400_000 -> "${timeDiff / 3_600_000} hours ago"
            timeDiff < 604_800_000 -> "${timeDiff / 86_400_000} days ago"
            else -> "A long time ago"
        }
    }

    /**
     * Called when the application moves to the foreground.
     */
    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.i(TAG, "App entered foreground, setting user to ONLINE.")
        setUserOnline()
    }

    /**
     * Called when the application moves to the background.
     */
    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.i(TAG, "App entered background, setting user to OFFLINE.")
        setUserOffline()
    }

    /**
     * Observes a user's online status in real-time.
     *
     * A user is considered "online" if their `isOnline` flag in Firestore is true AND their
     * `lastSeen` timestamp is within the last 5 minutes.
     *
     * @param userId The ID of the user to observe.
     * @return A [Flow] that emits `true` if the user is considered online, `false` otherwise.
     */
    fun observeUserOnlineStatus(userId: String): Flow<Boolean> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        try {
            listenerRegistration = firestore.collection("users")
                .document(userId)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "Error observing user online status for $userId", e)
                        trySend(false) // Default to offline on error
                        return@addSnapshotListener
                    }

                    val isOnline = snapshot?.getBoolean("isOnline") ?: false
                    val lastSeen = snapshot?.getLong("lastSeen") ?: 0L

                    // A user is only truly online if their status is set and they were active recently.
                    val isRecentlyActive = (System.currentTimeMillis() - lastSeen) < FIVE_MINUTES_IN_MS
                    val actualOnlineStatus = isOnline && isRecentlyActive

                    trySend(actualOnlineStatus)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in observeUserOnlineStatus for $userId", e)
            trySend(false)
        }
        awaitClose { listenerRegistration?.remove() }
    }

    /**
     * Updates the current user's status to online in Firestore.
     */
    private fun setUserOnline() {
        val currentUser = firebaseAuth.currentUser ?: return
        scope.launch {
            try {
                val updates = mapOf(
                    "isOnline" to true,
                    "lastSeen" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(updates)
                    .addOnSuccessListener { Log.d(TAG, "User ${currentUser.uid} set to ONLINE.") }
                    .addOnFailureListener { e -> Log.w(TAG, "Failed to set user online.", e) }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting user online.", e)
            }
        }
    }

    /**
     * Updates the current user's status to offline in Firestore.
     */
    private fun setUserOffline() {
        val currentUser = firebaseAuth.currentUser ?: return
        scope.launch {
            try {
                val updates = mapOf(
                    "isOnline" to false,
                    "lastSeen" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(updates)
                    .addOnSuccessListener { Log.d(TAG, "User ${currentUser.uid} set to OFFLINE.") }
                    .addOnFailureListener { e -> Log.w(TAG, "Failed to set user offline.", e) }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting user offline.", e)
            }
        }
    }

    /**
     * Cleans up resources by cancelling the coroutine scope and removing the lifecycle observer.
     * Should be called when the application is being destroyed.
     */
    fun destroy() {
        Log.d(TAG, "Destroying UserPresenceManager.")
        scope.cancel()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
    }
}
