import com.google.firebase.auth.FirebaseUser

/**
 * Sealed class representing different navigation states during application startup.
 *
 * This class manages the complex decision-making process that occurs during app launch,
 * determining where to navigate the user based on their authentication status,
 * profile completion state, and other factors. It provides a type-safe way to handle
 * the various startup scenarios.
 */
sealed class SplashNavigationState {

    /**
     * Represents the initial loading state during splash screen.
     *
     * This state is active while the application is checking authentication status,
     * profile completion, and other initialization tasks.
     */
    data object Loading : SplashNavigationState()

    /**
     * Indicates that the user needs to authenticate.
     *
     * This state directs the navigation flow to the login/registration screens
     * when no valid authentication is found or when authentication has expired.
     */
    data object Authenticate : SplashNavigationState()

    /**
     * Indicates that the user needs to complete their profile setup.
     *
     * This state is used when a user is authenticated but hasn't completed
     * their profile information (nickname, bio, profile picture, etc.).
     */
    data object CompleteProfile : SplashNavigationState()

    /**
     * Indicates that the user is fully authenticated and ready to use the app.
     *
     * This state directs navigation to the main application screens when
     * the user has valid authentication and a complete profile.
     *
     * @param user the authenticated FirebaseUser object, may be null in edge cases
     */
    data class AlreadyAuthenticated(val user: FirebaseUser?) : SplashNavigationState()

    /**
     * Represents an error state during the splash navigation decision process.
     *
     * This state is used when unexpected errors occur during authentication
     * checking or profile validation, requiring error handling or fallback navigation.
     *
     * @param message descriptive error message explaining what went wrong during startup
     */
    data class Error(val message: String) : SplashNavigationState()
}
