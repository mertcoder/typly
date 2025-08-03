package com.typly.app.domain.usecase

import com.typly.app.util.LoginValidationResult

/**
 * A use case that validates the inputs for the user login form.
 *
 * This class encapsulates the business logic for checking email and password fields
 * on the login screen. It ensures the fields are not blank and the email has a
 * valid format before attempting an authentication request.
 *
 * The `operator fun invoke` allows this class to be used as if it were a function.
 */
class ValidateLoginUseCase {

    /**
     * Executes the validation logic for the user login form.
     *
     * @param email The email address to validate.
     * @param password The password to validate.
     * @return A [LoginValidationResult] indicating the outcome of the validation.
     * Returns [LoginValidationResult.Success] if all inputs are valid.
     */
    operator fun invoke(email: String, password: String): LoginValidationResult {
        return when {
            email.isBlank() -> LoginValidationResult.EmptyEmail
            password.isBlank() -> LoginValidationResult.EmptyPassword
            !validateEmail(email) -> LoginValidationResult.InvalidEmail
            else -> LoginValidationResult.Success
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
