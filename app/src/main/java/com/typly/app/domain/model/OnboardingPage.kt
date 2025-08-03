package com.typly.app.domain.model

/**
 * Data class representing a single page in the application's onboarding flow.
 *
 * This class defines the structure for onboarding screens that introduce
 * users to the application's features and functionality. Each page contains
 * textual content and an associated image to create an engaging onboarding experience.
 *
 * @param title the main heading text for the onboarding page
 * @param description detailed explanation or description text for the page content
 * @param image drawable resource ID for the image to be displayed on the page
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Int
)
