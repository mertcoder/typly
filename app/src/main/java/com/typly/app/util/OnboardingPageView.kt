package com.typly.app.util

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.typly.app.presentation.components.OnboardingDescriptionText
import com.typly.app.presentation.components.OnboardingHeaderText
import com.typly.app.domain.model.OnboardingPage

/**
 * Composable function that displays a single onboarding page.
 * 
 * Renders an onboarding page with consistent layout including an image,
 * header text, and description text. Used within a pager or similar component
 * to create a multi-page onboarding experience. Features centered alignment
 * and appropriate spacing for optimal visual presentation.
 * 
 * @param page The onboarding page data containing image, title, and description
 */
@Composable
fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier.padding(bottom = 140.dp),

        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = page.image),
            contentDescription = null,
            modifier = Modifier.height(280.dp),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(32.dp))
        OnboardingHeaderText(page.title)
        Spacer(modifier = Modifier.height(25.dp))
        OnboardingDescriptionText(page.description)
    }
}
