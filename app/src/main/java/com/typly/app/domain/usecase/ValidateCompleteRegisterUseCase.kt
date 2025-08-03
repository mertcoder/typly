package com.typly.app.domain.usecase

import com.typly.app.util.RegisterValidationResult

/**
 * A use case for validating user inputs during the profile completion step of registration.
 *
 * This class contains the business rules for validating fields like nickname and full name,
 * ensuring they are not empty and conform to specified formats. It is used after
 * initial account creation but before the user profile is finalized.
 *
 * The `operator fun invoke` allows this class to be used as if it were a function.
 */
class ValidateCompleteRegisterUseCase {

    /**
     * Executes the validation logic for the profile completion form.
     *
     * @param nickname The user's chosen nickname to validate.
     * @param fullName The user's full name to validate.
     * @return A [RegisterValidationResult] indicating the validation outcome.
     * Returns [RegisterValidationResult.Success] if all inputs are valid.
     */
    operator fun invoke(
        nickname: String,
        fullName: String,
    ): RegisterValidationResult {
        return when {
            nickname.isBlank() -> RegisterValidationResult.EmptyNickname
            fullName.isBlank() -> RegisterValidationResult.EmptyFullName
            !validateNickname(nickname) -> RegisterValidationResult.InvalidNickname
            else -> RegisterValidationResult.Success
        }
    }

    /**
     * Validates the format of a given nickname.
     *
     * The rule ensures the nickname starts with a letter and is followed by 2 to 14
     * alphanumeric characters or underscores.
     *
     * @param nickname The nickname string to validate.
     * @return `true` if the nickname format is valid, `false` otherwise.
     */
    private fun validateNickname(nickname: String): Boolean {
        val nicknameRegex = "^[a-zA-Z][a-zA-Z0-9_]{2,14}$".toRegex()
        return nicknameRegex.matches(nickname)
    }
}
