package com.typly.app.domain.model

/**
 * Enumeration representing the different types of login methods available in the application.
 *
 * This enum is used to distinguish between different authentication flows and
 * helps in handling login-specific logic throughout the application.
 */
enum class LoginType {

    /**
     * Basic email and password authentication.
     *
     * Represents traditional email/password login flow using Firebase Authentication.
     */
    BASIC,

    /**
     * Google Sign-In authentication.
     *
     * Represents OAuth 2.0 authentication flow using Google Sign-In SDK
     * integrated with Firebase Authentication.
     */
    GOOGLE
}
