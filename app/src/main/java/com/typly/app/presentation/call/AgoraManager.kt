package com.typly.app.presentation.call

import android.content.Context
import android.media.AudioManager
import android.util.Log
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages all interactions with the Agora Real-Time Communication (RTC) SDK.
 *
 * This singleton class acts as a wrapper around the [RtcEngine], simplifying its lifecycle
 * management, configuration, and event handling. It is specifically optimized for voice calls,
 * with a strong focus on echo cancellation through proper [AudioManager] and Agora audio profile
 * configuration.
 *
 * @property context The application context, required for initializing the RTC engine and AudioManager.
 */
@Singleton
class AgoraManager @Inject constructor(
    private val context: Context
) {
    private var rtcEngine: RtcEngine? = null
    private val APP_ID = "aab8b8f5a8cd4469a63042fcfafe7063" // Public Agora test App ID
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var connectionListener: AgoraConnectionListener? = null

    companion object {
        private const val TAG = "AgoraManager"
    }

    /**
     * A simplified listener interface to communicate key call events to the UI layer (ViewModel).
     * This decouples the ViewModel from the verbose [IRtcEngineEventHandler].
     */
    interface AgoraConnectionListener {
        /** Called when the local user successfully joins the channel. */
        fun onCallConnected()
        /** Called when a remote user joins the channel. */
        fun onUserJoined(uid: Int)
        /** Called when a remote user leaves the channel. */
        fun onUserLeft(uid: Int)
        /** Called when the connection to the channel is lost. */
        fun onConnectionLost()
    }

    /**
     * Sets the listener that will receive call connection events.
     * @param listener An implementation of [AgoraConnectionListener].
     */
    fun setConnectionListener(listener: AgoraConnectionListener) {
        connectionListener = listener
    }

    /**
     * Configures the Android [AudioManager] for an optimal voice call experience.
     *
     * This is a critical step for **echo cancellation**. It sets the audio mode to
     * `MODE_IN_COMMUNICATION` and ensures the speakerphone is disabled, routing
     * audio to the earpiece by default.
     */
    private fun configureAudioManager() {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            audioManager.isSpeakerphoneOn = false

            // Set a moderate stream volume to further prevent feedback echo.
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL)
            val targetVolume = (maxVolume * 0.6).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, targetVolume, 0)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to configure AudioManager", e)
        }
    }

    /**
     * Resets the [AudioManager] to its default state after a call has ended.
     */
    private fun resetAudioManager() {
        try {
            audioManager.mode = AudioManager.MODE_NORMAL
            audioManager.isSpeakerphoneOn = false // Ensure speakerphone is off by default
        } catch (e: Exception) {
            Log.w(TAG, "Failed to reset AudioManager", e)
        }
    }

    /**
     * Sanitizes a given channel name to be compliant with Agora's naming requirements.
     * Agora channels must be 1-64 characters, use a limited character set, and not have
     * leading/trailing spaces.
     *
     * @param channelName The raw channel name to sanitize.
     * @return A compliant channel name string.
     */
    private fun sanitizeChannelName(channelName: String): String {
        var sanitized = channelName
            .trim()
            .replace("[^A-Za-z0-9_-]".toRegex(), "") // Allow only alphanumeric, underscore, hyphen
            .take(64)

        if (sanitized.isEmpty() || sanitized.first().isDigit()) {
            sanitized = "call_$sanitized".take(64)
        }

        return sanitized
    }

    /**
     * Generates a consistent, positive 32-bit integer UID from a string user ID.
     * Agora requires UIDs to be positive integers. This function uses a hash code
     * to create a deterministic UID for a given user.
     *
     * @param userId The string-based user ID from your system.
     * @return A positive [Int] suitable for use as an Agora UID.
     */
    private fun generateUID(userId: String): Int {
        val hash = userId.hashCode()
        val positiveHash = if (hash == Int.MIN_VALUE) Int.MAX_VALUE else kotlin.math.abs(hash)
        // Ensure UID is a positive integer and not 0.
        return (positiveHash % 1_000_000_000) + 1_000_000
    }

    /**
     * Initializes the Agora [RtcEngine].
     *
     * This function destroys any existing engine instance and creates a new one with
     * configurations optimized for voice chat, including profiles for echo cancellation
     * and routing audio to the earpiece.
     *
     * @return A configured [RtcEngine] instance, or `null` on failure.
     */
    fun initializeEngine(): RtcEngine? {
        if (rtcEngine != null) {
            destroy()
        }

        try {
            val config = RtcEngineConfig().apply {
                mContext = context
                mAppId = APP_ID
                mEventHandler = rtcEventHandler
                mChannelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
                mAudioScenario = Constants.AUDIO_SCENARIO_DEFAULT
            }

            rtcEngine = RtcEngine.create(config)
            rtcEngine?.apply {
                // Core configurations for a voice call setup.
                setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION)
                enableAudio()
                disableVideo()

                // CRITICAL: Use an audio profile designed for voice chat to enhance echo cancellation.
                setAudioProfile(
                    Constants.AUDIO_PROFILE_DEFAULT,
                    Constants.AUDIO_SCENARIO_CHATROOM
                )

                // Further audio processing settings to minimize echo.
                try {
                    setVoiceBeautifierPreset(Constants.VOICE_BEAUTIFIER_OFF)
                    setAudioEffectPreset(Constants.AUDIO_EFFECT_OFF)
                    adjustRecordingSignalVolume(80) // Reduce recording volume to prevent feedback.
                } catch (e: Exception) {
                    Log.w(TAG, "Audio processing features not available.", e)
                }

                // CRITICAL: Default audio to the earpiece instead of the speaker to prevent echo.
                setDefaultAudioRoutetoSpeakerphone(false)

                enableAudioVolumeIndication(2000, 3, false)
            }
            Log.i(TAG, "Agora RtcEngine initialized successfully.")
            return rtcEngine
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize Agora RtcEngine", e)
            return null
        }
    }

    /**
     * The main event handler for receiving callbacks from the Agora RTC Engine.
     */
    private val rtcEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            Log.i(TAG, "Successfully joined channel: $channel with uid: $uid")
            connectionListener?.onCallConnected()
        }

        override fun onUserJoined(uid: Int, elapsed: Int) {
            Log.i(TAG, "Remote user joined: $uid")
            connectionListener?.onUserJoined(uid)
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            Log.i(TAG, "Remote user left: $uid, reason: $reason")
            connectionListener?.onUserLeft(uid)
        }

        override fun onConnectionLost() {
            Log.w(TAG, "Connection lost from Agora channel")
            connectionListener?.onConnectionLost()
        }

        override fun onConnectionStateChanged(state: Int, reason: Int) {
            Log.d(TAG, "Connection state changed to: $state, reason: $reason")
        }

        /**
         * A critical callback for managing echo. If the audio route changes to speaker,
         * we attempt to force it back to the earpiece.
         */
        override fun onAudioRouteChanged(routing: Int) {
            Log.d(TAG, "Audio route changed to: $routing")
            // ROUTE_SPEAKERPHONE = 3. This can cause echo.
            if (routing == 3) {
                Log.w(TAG, "Audio route switched to speakerphone, attempting to revert to earpiece to prevent echo.")
                try {
                    rtcEngine?.setDefaultAudioRoutetoSpeakerphone(false)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to force audio route to earpiece.", e)
                }
            }
        }

        override fun onAudioVolumeIndication(speakers: Array<out AudioVolumeInfo>?, totalVolume: Int) {
            // This callback can be frequent. Log only when there is significant activity.
            if (totalVolume > 50) {
                Log.v(TAG, "Audio active - Total volume: $totalVolume")
            }
        }

        override fun onError(err: Int) {
            Log.e(TAG, "An error occurred in the RTC engine: $err")
            // Handle critical errors that might require reconnecting.
            if (err in listOf(
                    Constants.ERR_INVALID_TOKEN,
                    Constants.ERR_TOKEN_EXPIRED,
                    Constants.ERR_JOIN_CHANNEL_REJECTED
                )) {
                connectionListener?.onConnectionLost()
            }
        }
    }

    /**
     * Joins a user to a specified audio call channel.
     *
     * This function orchestrates the entire process of joining a call:
     * 1. Sanitizes the channel name and generates a UID.
     * 2. Configures the system's AudioManager for the call.
     * 3. Initializes the RtcEngine if it's not already running.
     * 4. Applies multiple audio configurations to prevent echo.
     * 5. Validates inputs and finally joins the channel.
     *
     * @param channelName The name of the channel to join.
     * @param userId The local user's ID, used to generate a consistent integer UID.
     */
    fun joinAudioCall(channelName: String, userId: String? = null) {
        val sanitizedChannelName = sanitizeChannelName(channelName)
        val uid = if (userId != null) generateUID(userId) else 0 // 0 lets Agora assign a UID

        Log.i(TAG, "Attempting to join channel '$sanitizedChannelName' with UID: $uid")

        // 1. Configure Android AudioManager FIRST for echo cancellation.
        configureAudioManager()

        if (rtcEngine == null) {
            initializeEngine()
            if (rtcEngine == null) {
                Log.e(TAG, "Engine initialization failed. Aborting join call.")
                resetAudioManager()
                return
            }
        }

        rtcEngine?.apply {
            // 2. Ensure we are not already in a channel. Leave first if needed.
            try {
                leaveChannel()
                Thread.sleep(100) // Give a moment for leave to complete.
            } catch (e: Exception) {
                Log.w(TAG, "Exception while leaving previous channel, proceeding anyway.", e)
            }

            // 3. Apply final audio configurations before joining.
            setDefaultAudioRoutetoSpeakerphone(false) // Re-affirm earpiece route.
            muteLocalAudioStream(false) // Ensure user is not muted by default.

            // 4. Join the channel. A token is not used in this test setup.
            val joinResult = joinChannel(null, sanitizedChannelName, null, uid)

            if (joinResult != 0) {
                Log.e(TAG, "JoinChannel failed with error code: $joinResult")
                resetAudioManager()
            } else {
                Log.i(TAG, "JoinChannel call successful. Waiting for onJoinChannelSuccess callback.")
            }
        } ?: run {
            Log.e(TAG, "RtcEngine is null! Cannot join channel.")
            resetAudioManager()
        }
    }

    /**
     * Leaves the current audio channel and resets the audio manager.
     */
    fun leaveCall() {
        Log.i(TAG, "Leaving audio channel.")
        rtcEngine?.leaveChannel()
        resetAudioManager()
    }

    /**
     * Mutes or unmutes the local user's audio stream.
     * @param muted `true` to mute the audio, `false` to unmute.
     */
    fun muteAudio(muted: Boolean) {
        Log.i(TAG, "Setting local audio mute status to: $muted")
        val result = rtcEngine?.muteLocalAudioStream(muted)
        if (result != 0) {
            Log.e(TAG, "Failed to set mute status. Error code: $result")
        }
    }

    /**
     * Destroys the [RtcEngine] instance to release all resources.
     *
     * This should be called when the application is closing or when the call feature
     * is no longer needed to free up memory and hardware resources.
     */
    fun destroy() {
        Log.w(TAG, "Destroying Agora RtcEngine instance.")
        if (rtcEngine != null) {
            RtcEngine.destroy() // Static method to clean up.
            rtcEngine = null
        }
        resetAudioManager()
    }
}
