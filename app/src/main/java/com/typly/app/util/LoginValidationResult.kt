package com.typly.app.util

/**
 * Sealed class representing all possible validation results for user login.
 * 
 * Provides type-safe validation outcomes for login form fields including
 * email and password validation. Each result represents a specific validation
 * state that can be used to provide targeted user feedback during the login process.
 * 
 * Used by login use cases and UI components to handle validation logic
 * and display appropriate error messages to users.
 */
sealed class LoginValidationResult {
    /** Validation passed successfully, login can proceed */
    data object Success : LoginValidationResult()

    /** Email format is invalid */
    data object InvalidEmail : LoginValidationResult()
    
    /** Email field is empty */
    data object EmptyEmail: LoginValidationResult()
    
    /** Password field is empty */
    data object EmptyPassword: LoginValidationResult()
}
