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
 * A styled text component specifically designed for onboarding screen headers.
 * 
 * Renders large, prominent header text with consistent styling including Poppins SemiBold font,
 * centered alignment, and a purple shadowed color scheme. Used as the main title text
 * on onboarding screens to highlight key features or welcome messages.
 * 
 * @param text The header text content to display
 */
@Composable
fun OnboardingHeaderText(
    text: String,
) {
    Text(
        text = text,
        fontFamily = FontFamily(Font(R.font.poppins_semibold)),
        fontSize = 28.sp,
        color = colorResource(R.color.purple_shadowed),
        fontWeight = FontWeight.SemiBold,
        textAlign = TextAlign.Center,
        lineHeight = 35.sp
    )
}
