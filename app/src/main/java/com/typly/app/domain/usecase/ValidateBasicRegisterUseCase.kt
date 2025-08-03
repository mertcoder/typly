package com.typly.app.domain.usecase

import com.typly.app.util.RegisterValidationResult

/**
 * A use case responsible for validating inputs for the basic user registration process.
 *
 * This class encapsulates the business logic for checking the validity of an email
 * and password during the initial account creation step. It ensures that inputs are
 * not blank and meet the required format and length constraints before proceeding
 * with an authentication request.
 *
 * The `operator fun invoke` allows this class to be used as if it were a function.
 */
class ValidateBasicRegisterUseCase {

    /**
     * Executes the validation logic for basic user registration.
     *
     * @param email The email address to validate.
     * @param password The password to validate.
     * @return A [RegisterValidationResult] indicating the outcome of the validation.
     * Returns [RegisterValidationResult.Success] if all inputs are valid.
     */
    operator fun invoke(email: String, password: String): RegisterValidationResult {
        return when {
            email.isBlank() -> RegisterValidationResult.EmptyEmail
            password.isBlank() -> RegisterValidationResult.EmptyPassword
            !validateEmail(email) -> RegisterValidationResult.InvalidEmail
            password.length < 6 -> RegisterValidationResult.PasswordTooShort
            else -> RegisterValidationResult.Success
        }
    }

    /**
     * Validates if the given email string is in a valid email format.
     *
     * This function uses a regular expression to check if the email string conforms
     * to a standard email pattern.
     *
     * @param email The email string to validate.
     * @return `true` if the email is valid, `false` otherwise.
     */
    private fun validateEmail(email: String): Boolean {
        val emailRegex = Regex(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$"
        )
        return emailRegex.matches(email)
    }
}
