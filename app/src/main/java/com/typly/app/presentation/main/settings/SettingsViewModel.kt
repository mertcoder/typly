package com.typly.app.presentation.main.settings

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.typly.app.BuildConfig
import com.typly.app.data.cache.UserProfileCache
import com.typly.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * Font size options for the application UI
 */
enum class FontSize { Small, Medium, Large }

/**
 * ViewModel for managing application settings and user preferences.
 * 
 * Handles persistent storage of user settings via SharedPreferences,
 * cache management, and provides reactive state for UI components.
 * 
 * @property authRepository Repository for authentication operations
 * @property context Application context for accessing system services
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
): ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
        private const val PREFS_NAME = "app_settings"
    }

    private val sharedPrefs: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Settings States
    
    /** StateFlow for push notifications preference */
    private val _notificationsEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("notifications_enabled", true)
    )
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    /** StateFlow for read receipts sharing preference */
    private val _readReceiptsEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("read_receipts_enabled", true)
    )
    val readReceiptsEnabled: StateFlow<Boolean> = _readReceiptsEnabled.asStateFlow()

    /** StateFlow for last seen visibility preference */
    private val _lastSeenEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("last_seen_enabled", true)
    )
    val lastSeenEnabled: StateFlow<Boolean> = _lastSeenEnabled.asStateFlow()

    /** StateFlow for automatic media download preference */
    private val _autoDownloadEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("auto_download_enabled", false)
    )
    val autoDownloadEnabled: StateFlow<Boolean> = _autoDownloadEnabled.asStateFlow()

    /** StateFlow for do not disturb mode preference */
    private val _doNotDisturbEnabled = MutableStateFlow(
        sharedPrefs.getBoolean("dnd_enabled", false)
    )
    val doNotDisturbEnabled: StateFlow<Boolean> = _doNotDisturbEnabled.asStateFlow()

    /** StateFlow for UI font size preference */
    private val _fontSize = MutableStateFlow(
        FontSize.valueOf(sharedPrefs.getString("font_size", "Medium") ?: "Medium")
    )
    val fontSize: StateFlow<FontSize> = _fontSize.asStateFlow()

    /**
     * Returns the current application version information
     * @return Formatted version string with name and build number
     */
    fun getAppVersion(): String {
        return "Typly v${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"
    }

    /**
     * Updates push notifications preference
     * @param enabled Whether push notifications should be enabled
     */
    fun updateNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("notifications_enabled", enabled).apply()
        Log.d(TAG, "Notifications setting updated: $enabled")
    }

    /**
     * Updates read receipts sharing preference
     * @param enabled Whether read receipts should be shared with other users
     */
    fun updateReadReceipts(enabled: Boolean) {
        _readReceiptsEnabled.value = enabled
        sharedPrefs.edit().putBoolean("read_receipts_enabled", enabled).apply()
        Log.d(TAG, "Read receipts setting updated: $enabled")
    }

    /**
     * Updates last seen visibility preference
     * @param enabled Whether last seen status should be visible to other users
     */
    fun updateLastSeen(enabled: Boolean) {
        _lastSeenEnabled.value = enabled
        sharedPrefs.edit().putBoolean("last_seen_enabled", enabled).apply()
        Log.d(TAG, "Last seen setting updated: $enabled")
    }

    /**
     * Updates automatic media download preference
     * @param enabled Whether media should be automatically downloaded
     */
    fun updateAutoDownload(enabled: Boolean) {
        _autoDownloadEnabled.value = enabled
        sharedPrefs.edit().putBoolean("auto_download_enabled", enabled).apply()
        Log.d(TAG, "Auto download setting updated: $enabled")
    }

    /**
     * Updates do not disturb mode preference
     * @param enabled Whether do not disturb mode should be active
     */
    fun updateDoNotDisturb(enabled: Boolean) {
        _doNotDisturbEnabled.value = enabled
        sharedPrefs.edit().putBoolean("dnd_enabled", enabled).apply()
        Log.d(TAG, "Do not disturb setting updated: $enabled")
    }

    /**
     * Updates UI font size preference
     * @param fontSize The new font size to be applied
     */
    fun updateFontSize(fontSize: FontSize) {
        _fontSize.value = fontSize
        sharedPrefs.edit().putString("font_size", fontSize.name).apply()
        Log.d(TAG, "Font size setting updated: ${fontSize.name}")
    }

    /**
     * Clears application cache including system cache and user profile cache
     * @return True if cache was cleared successfully, false otherwise
     */
    fun clearCache(): Boolean {
        return try {
            val cacheDir = context.cacheDir
            cacheDir.deleteRecursively()
            
            // Also clear UserProfileCache
            UserProfileCache.clearCache()
            
            Log.d(TAG, "Cache cleared successfully")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to clear cache: ${e.message}")
            false
        }
    }

    /**
     * Calculates and returns the current cache size as a formatted string
     * @return Formatted cache size string (e.g., "12.5 MB") or "Unknown" if calculation fails
     */
    fun getCacheSize(): String {
        return try {
            val cacheDir = context.cacheDir
            val sizeInBytes = getFolderSize(cacheDir)
            formatFileSize(sizeInBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to calculate cache size: ${e.message}")
            "Unknown"
        }
    }

    /**
     * Recursively calculates the total size of a folder and its contents
     * @param folder The folder to calculate size for
     * @return Total size in bytes
     */
    private fun getFolderSize(folder: File): Long {
        if (!folder.exists()) return 0
        if (folder.isFile) return folder.length()
        
        return folder.listFiles()?.sumOf { getFolderSize(it) } ?: 0
    }

    /**
     * Formats file size from bytes to human-readable format
     * @param sizeInBytes Size in bytes to format
     * @return Formatted size string (e.g., "12.5 MB", "1.2 GB")
     */
    private fun formatFileSize(sizeInBytes: Long): String {
        val kb = sizeInBytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0

        return when {
            gb >= 1 -> String.format("%.1f GB", gb)
            mb >= 1 -> String.format("%.1f MB", mb)
            kb >= 1 -> String.format("%.1f KB", kb)
            else -> "$sizeInBytes B"
        }
    }

    /**
     * Signs out the current user and clears all cached data
     * Performs logout operation and clears user profile cache
     */
    fun signOut(){
        viewModelScope.launch {
            authRepository.logout()
            UserProfileCache.clearCache()
            Log.d(TAG, "User signed out successfully. Profile cache cleared.")
        }
    }
}
