package com.typly.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.typly.app.R
import com.typly.app.presentation.components.DotsIndicator
import com.typly.app.domain.model.OnboardingPage
import com.typly.app.util.OnboardingPageView

/**
 * A composable screen that displays a multi-page onboarding experience for new users.
 *
 * This screen uses a [HorizontalPager] to allow users to swipe through a series of
 * informational pages. A "Get Started" button is displayed on the final page to allow
 * the user to proceed into the app.
 *
 * @param onFinished A lambda callback that is invoked when the user clicks the "Get Started"
 * button, signaling the completion of the onboarding process.
 */
@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    // Defines the content for each page of the onboarding flow.
    val pages = listOf(
        OnboardingPage(
            title = "Rich, Modern Messaging Experience",
            description = "Share messages, photos, and more in vibrant conversations.",
            image = R.drawable.onboarding_experience_test
        ),
        OnboardingPage(
            title = "Secure, Private Conversations",
            description = "Protect your privacy with end-to-end encryption and anonymous login options.",
            image = R.drawable.onboarding_secure
        ),
        OnboardingPage(
            title = "Quick and Easy Setup",
            description = "Start messaging right away with a simple setup process.",
            image = R.drawable.onboarding_fast
        )
    )

    // State for managing the HorizontalPager, such as the current page.
    val pagerState = rememberPagerState(
        pageCount = { pages.size },
        initialPage = 0
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image for the entire screen.
        Image(
            painter = painterResource(id = R.drawable.blurrybg_dark),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // The pager that displays the onboarding pages.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(0.8f)
            ) { page ->
                OnboardingPageView(page = pages[page])
            }

            // Display the "Get Started" button only on the last page.
            if (pagerState.currentPage == pages.lastIndex) {
                Button(
                    onClick = { onFinished() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 66.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_button))
                ) {
                    Text("Get Started", color = Color.White)
                }
            }

            // The dots indicator that shows the current page.
            DotsIndicator(
                totalDots = pages.size,
                selectedIndex = pagerState.currentPage,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
