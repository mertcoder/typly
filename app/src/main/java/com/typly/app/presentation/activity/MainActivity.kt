package com.typly.app.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.typly.app.presentation.navigation.AppNavigation
import com.typly.app.ui.theme.TyplyTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * The main and single activity for the application, serving as the entry point.
 *
 * This activity is annotated with [@AndroidEntryPoint] to enable Hilt for dependency injection.
 * Its primary responsibility is to host the Jetpack Compose UI content by setting up the
 * theme and the main navigation graph via the [AppNavigation] composable.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     * Note: Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enables the app to draw content behind the system bars for a modern, immersive UI.
        enableEdgeToEdge()
        setContent {
            // Applies the application's theme to the entire UI.
            TyplyTheme {
                // Sets up the main navigation graph for the application.
                AppNavigation()
            }
        }
    }
}
