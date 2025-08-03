package com.typly.app.presentation.call

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for managing and exposing user presence data to the UI.
 *
 * This class acts as a facade over the [UserPresenceManager], allowing UI components
 * to observe the online status of one or more users without directly interacting
 * with the manager.
 *
 * @property presenceManager The singleton manager that handles the underlying presence logic.
 */
@HiltViewModel
class UserPresenceViewModel @Inject constructor(
    val presenceManager: UserPresenceManager
) : ViewModel() {

    /**
     * A [StateFlow] that holds a map of user IDs to their online status (`true` or `false`).
     * The UI can collect this flow to reactively update when any observed user's status changes.
     */
    private val _userOnlineStates = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val userOnlineStates: StateFlow<Map<String, Boolean>> = _userOnlineStates.asStateFlow()

    private val _isObserving = MutableStateFlow(false)
    val isObserving: StateFlow<Boolean> = _isObserving.asStateFlow()

    /**
     * Returns a real-time [Flow] for a single user's online status.
     * Delegates directly to the [presenceManager].
     *
     * @param userId The ID of the user to observe.
     * @return A [Flow] that emits `true` if the user is online, `false` otherwise.
     */
    fun observeUserOnlineStatus(userId: String): Flow<Boolean> {
        return presenceManager.observeUserOnlineStatus(userId)
    }

    /**
     * Starts observing the online status for a list of user IDs.
     *
     * Launches a collector coroutine for each user and aggregates their statuses
     * into the [userOnlineStates] map.
     *
     * @param userIds A list of user IDs to start observing.
     */
    fun startObservingUsers(userIds: List<String>) {
        if (_isObserving.value) return

        _isObserving.value = true
        viewModelScope.launch {
            userIds.forEach { userId ->
                launch {
                    presenceManager.observeUserOnlineStatus(userId)
                        .collect { isOnline ->
                            val currentStates = _userOnlineStates.value.toMutableMap()
                            currentStates[userId] = isOnline
                            _userOnlineStates.value = currentStates
                        }
                }
            }
        }
    }

    /**
     * Stops observing all users and clears the state map.
     */
    fun stopObservingUsers() {
        _isObserving.value = false
        _userOnlineStates.value = emptyMap()
    }

    /**
     * Synchronously retrieves the last known online status for a specific user from the state map.
     *
     * @param userId The ID of the user.
     * @return `true` if the user is known to be online, `false` otherwise.
     */
    fun getUserOnlineStatus(userId: String): Boolean {
        return _userOnlineStates.value[userId] ?: false
    }

    /**
     * Initializes the underlying presence manager.
     */
    fun initializePresence() {
        presenceManager.initialize()
    }

    /**
     * Cleans up resources when the ViewModel is destroyed.
     * This stops observing users and destroys the presence manager to prevent leaks.
     */
    override fun onCleared() {
        super.onCleared()
        stopObservingUsers()
        presenceManager.destroy()
    }
}
