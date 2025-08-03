package com.typly.app.util

/**
 * Sealed class representing all possible validation results for user registration.
 * 
 * Provides type-safe validation outcomes for registration form fields including
 * email, password, full name, and nickname validation. Each result represents
 * a specific validation state that can be used to provide targeted user feedback.
 * 
 * Used by registration use cases and UI components to handle validation logic
 * and display appropriate error messages to users.
 */
sealed class RegisterValidationResult {
    /** Validation passed successfully, registration can proceed */
    data object Success : RegisterValidationResult()

    /** Email field is empty */
    data object EmptyEmail: RegisterValidationResult()
    
    /** Password field is empty */
    data object EmptyPassword: RegisterValidationResult()
    
    /** Full name field is empty */
    data object EmptyFullName: RegisterValidationResult()
    
    /** Nickname field is empty */
    data object EmptyNickname: RegisterValidationResult()
    
    /** Email format is invalid */
    data object InvalidEmail : RegisterValidationResult()
    
    /** Password does not meet minimum length requirements */
    data object PasswordTooShort: RegisterValidationResult()
    
    /** Nickname is already taken by another user */
    data object NicknameAlreadyExists: RegisterValidationResult()
    
    /** Nickname format is invalid (contains forbidden characters, etc.) */
    data object InvalidNickname: RegisterValidationResult()

}
