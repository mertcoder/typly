package com.typly.app.domain.model

/**
 * Sealed class representing different authentication methods supported by the application.
 *
 * This class encapsulates various ways users can authenticate, providing a type-safe
 * approach to handle different authentication flows. Each method contains the specific
 * data required for that particular authentication type.
 */
sealed class AuthMethod {

    /**
     * Basic email and password authentication method.
     *
     * This authentication method uses traditional email/password credentials
     * for user authentication through Firebase Authentication.
     *
     * @param email the user's email address
     * @param password the user's password
     */
    data class Basic(val email: String, val password: String): AuthMethod()

    /**
     * Google Sign-In authentication method.
     *
     * This authentication method uses Google's OAuth 2.0 flow for user authentication.
     * The ID token is obtained from Google Sign-In SDK and used to authenticate
     * with Firebase Authentication.
     *
     * @param idToken the Google ID token obtained from Google Sign-In
     */
    data class Google(val idToken: String): AuthMethod()
}
