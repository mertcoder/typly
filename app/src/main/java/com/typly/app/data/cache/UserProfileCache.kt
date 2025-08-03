package com.typly.app.data.cache

import com.typly.app.data.remote.dto.User
import android.util.Log

/**
 * A simple, in-memory singleton cache for storing a single user profile.
 *
 * This object provides a basic mechanism to temporarily store a [User] object
 * to reduce redundant reads from the data source. The cache has a time-based
 * expiration policy.
 *
 * Note: This implementation is not thread-safe and is intended for simple,
 * single-threaded access patterns. For multi-threaded environments, consider
 * using thread-safe constructs like `AtomicReference` or locks.
 *
 * @see User
 */
object UserProfileCache {

    private const val TAG = "UserProfileCache"

    private var user: User? = null
    private var lastUpdate: Long = 0L
    private const val CACHE_DURATION = 5 * 60 * 1_000L // 5 minutes in milliseconds

    /**
     * Saves or updates a user profile in the cache and resets the expiration timer.
     *
     * @param user The [User] object to be cached.
     */
    fun saveUserToCache(user: User) {
        this.user = user
        lastUpdate = System.currentTimeMillis()
        Log.d(TAG, "User profile for '${user.nickname}' saved to cache.")
    }

    /**
     * Retrieves the cached user profile if it exists and is still valid.
     *
     * If the cache has expired or is empty, it automatically clears the cache
     * and returns null.
     *
     * @return The cached [User] object, or `null` if the cache is invalid or empty.
     */
    fun getUserFromCache(): User? {
        return if (user != null && isCacheValid()) {
            Log.d(TAG, "User profile for '${user?.nickname}' retrieved from cache.")
            user
        } else {
            if (user != null) {
                Log.i(TAG, "Cache expired for user '${user?.nickname}'. Clearing cache.")
            }
            // Automatically clear the cache if it's expired or the user is null.
            clearCache()
            null
        }
    }

    /**
     * Manually invalidates and clears all data from the cache.
     *
     * This resets the user object to null and the update timestamp to zero.
     */
    fun clearCache() {
        Log.i(TAG, "User profile cache cleared.")
        user = null
        lastUpdate = 0L
    }

    /**
     * Checks if the cached data is still valid based on the [CACHE_DURATION].
     *
     * @return `true` if the time elapsed since the last update is within the
     * cache duration, `false` otherwise.
     */
    private fun isCacheValid(): Boolean {
        val currentTime = System.currentTimeMillis()
        return (currentTime - lastUpdate) <= CACHE_DURATION
    }

    /**
     * Returns the timestamp of the last cache update.
     *
     * Useful for debugging or implementing more complex cache validation logic.
     *
     * @return A [Long] representing the last update time in milliseconds since the epoch.
     */
    fun lastUpdateTime(): Long = lastUpdate

}
