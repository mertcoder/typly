package com.typly.app.util

/**
 * Global application state manager for tracking active app contexts.
 * 
 * Provides thread-safe storage for application-wide state that needs to be
 * accessed across different components. Currently tracks the active chat ID
 * to enable smart notification filtering when user is viewing a specific chat.
 * 
 * Uses @Volatile annotation to ensure thread-safe access across multiple threads.
 */
object AppState {
    /**
     * The ID of the currently active chat, if any.
     * 
     * Used to prevent showing push notifications for the chat that the user
     * is currently viewing. Set to null when no chat is active.
     */
    @Volatile
    var activeChatId: String? = null
}
