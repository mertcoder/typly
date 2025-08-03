package com.typly.app.presentation.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.typly.app.R

/**
 * A styled text component specifically designed for onboarding screen descriptions.
 * 
 * Renders descriptive text with consistent styling including Poppins Regular font,
 * centered alignment, and a purple-grey color scheme. Used to provide explanatory
 * content below onboarding headers to describe features or guide users.
 * 
 * @param text The description text content to display
 */
@Composable
fun OnboardingDescriptionText(
    text: String,
) {
    Text(
        text = text,
        fontFamily = FontFamily(Font(R.font.poppins_regular)),
        fontSize = 16.sp,
        color = colorResource(R.color.purple_grey_description),
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center
    )
}
