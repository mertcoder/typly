package com.typly.app.di

import android.app.Application
import com.typly.app.presentation.call.UserPresenceManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Custom [Application] class for the app, serving as the Hilt dependency injection root.
 *
 * Annotated with [@HiltAndroidApp] to enable field injection in application-level classes
 * and to provide dependencies to other components throughout the app. This class is the
 * main entry point of the application process.
 */
@HiltAndroidApp
class MyApp : Application() {

    /**
     * Injected singleton instance of [UserPresenceManager] to handle the app-wide
     * user online/offline presence system.
     */
    @Inject
    lateinit var userPresenceManager: UserPresenceManager

    /**
     * Called when the application is starting, before any other objects have been created.
     *
     * This method is used here to perform application-level initializations, such as
     * starting the user presence system.
     */
    override fun onCreate() {
        super.onCreate()
        // Initialize the user presence system when the app process is created.
        userPresenceManager.initialize()
    }

    /**
     * This method is for use in emulated process environments only. It will never be called
     * on a production Android device, where processes are typically killed by the system
     * without warning. It is used here to properly clean up resources during development.
     */
    override fun onTerminate() {
        super.onTerminate()
        // Clean up the presence manager resources when the application is terminated.
        userPresenceManager.destroy()
    }

}
