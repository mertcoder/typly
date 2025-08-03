package com.typly.app.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.typly.app.R

/**
 * The main application top bar with logo and search functionality.
 * 
 * Displays the Typly logo on the left side and a search/add people icon on the right.
 * Features a glassmorphism design with rounded corners and semi-transparent background.
 * The search icon triggers user search functionality when clicked.
 * 
 * @param searchUser Callback triggered when the search/add people icon is clicked
 */
@Composable
fun TopBar(
   searchUser: () -> Unit
){
    // Glass effect container similar to BottomBar
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 16.dp) // Top spacing
            .clip(RoundedCornerShape(28.dp)) // Rounded corners
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.025f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        // Content row with logo and search icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 70.dp) // Keep original height
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Typly Logo container - constrain the area but allow logo to scale within
            Box(
                modifier = Modifier.size(89.dp), // Fixed container size
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.typlylogo),
                    contentDescription = "Typly Logo",
                    modifier = Modifier.size(89.dp), // Logo can be bigger than container
                    contentScale = ContentScale.Crop // Show logo at actual size, crop if needed
                )
            }
            
            // Search Icon on the right
            IconButton(
                onClick = {

                    //Search User Action
                    searchUser()


                }
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_add_people),
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp),
                    contentScale = ContentScale.Crop // Show logo at actual size, crop if needed
                )
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun LearnTopAppBarPreview(){
    TopBar(searchUser = { /* Preview action */ })
}
