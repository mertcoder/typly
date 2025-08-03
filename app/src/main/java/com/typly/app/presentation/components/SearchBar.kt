package com.typly.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A custom search bar component with glassmorphism design.
 * 
 * Provides a text input field for search functionality with a modern glass effect
 * background, search icon, and keyboard handling. Features automatic state management
 * and customizable hint text. Includes search action handling and keyboard dismissal.
 * 
 * @param modifier Modifier to be applied to the search bar container
 * @param searchText Initial search text value
 * @param onSearchTextChange Callback triggered when search text changes
 * @param onSearchClick Callback triggered when search action is performed
 * @param hint Placeholder text to display when search field is empty
 */
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchText: String = "",
    onSearchTextChange: (String) -> Unit = {},
    onSearchClick: () -> Unit = {},
    hint: String = "Search User"
) {
    var internalSearchText by remember { mutableStateOf(searchText) }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Glass effect container similar to TopBar and BottomBar
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp)) // Rounded corners like other components
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.025f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        // Search field container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp) // Standard search bar height
                .padding(horizontal = 20.dp, vertical = 8.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            // Background text field
            BasicTextField(
                value = internalSearchText,
                onValueChange = { newText ->
                    internalSearchText = newText
                    onSearchTextChange(newText)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 48.dp), // Space for trailing icon
                textStyle = TextStyle(
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearchClick()
                        keyboardController?.hide()
                    }
                ),
                cursorBrush = SolidColor(Color.White.copy(alpha = 0.8f)),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        // Hint text
                        if (internalSearchText.isEmpty()) {
                            Text(
                                text = hint,
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontFamily = MaterialTheme.typography.bodyLarge.fontFamily
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Trailing search icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = {
                        onSearchClick()
                        keyboardController?.hide()
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(20.dp),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchBarPreview() {
    SearchBar(
        onSearchTextChange = { },
        onSearchClick = { }
    )
}
