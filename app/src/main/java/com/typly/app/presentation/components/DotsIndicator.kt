package com.typly.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import com.typly.app.R

/**
 * A horizontal row of circular dots indicator typically used for pagination or step indicators.
 * 
 * Displays a series of circular dots where one dot is highlighted to indicate the current
 * position or selected item. The selected dot appears larger and in a different color.
 * Commonly used in onboarding flows, image carousels, or multi-step processes.
 * 
 * @param totalDots The total number of dots to display
 * @param selectedIndex The zero-based index of the currently selected/active dot
 * @param modifier Modifier to be applied to the indicator container
 * @param selectedColor Color of the selected/active dot
 * @param unSelectedColor Color of the unselected/inactive dots
 */
@Composable
fun DotsIndicator(
    totalDots: Int,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    selectedColor: Color = colorResource(R.color.purple_button),
    unSelectedColor: Color =  Color.Gray
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        for (i in 0 until totalDots) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (i == selectedIndex) 12.dp else 8.dp)
                    .background(
                        color = if (i == selectedIndex) selectedColor else unSelectedColor,
                        shape = CircleShape
                    )
            )
        }
    }
}
