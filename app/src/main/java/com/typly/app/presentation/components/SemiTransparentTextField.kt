package com.typly.app.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A semi-transparent outlined text field with error handling.
 * 
 * Provides a styled text input field with transparent background, outlined borders,
 * and error state support. Features customizable placeholder text and optional
 * error message display below the field. Uses consistent theming with white text
 * and semi-transparent borders.
 * 
 * @param value Current text value of the field
 * @param onValueChange Callback triggered when text value changes
 * @param placeholder Placeholder text to display when field is empty
 * @param isError Whether the field is in error state
 * @param errorMessage Optional error message to display below the field
 */
@Composable
fun SemiTransparentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFFA5A5E0) // Light purple
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
            focusedBorderColor = Color.White.copy(alpha = 0.15f),
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            cursorColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        singleLine = true,
        isError = isError
    )
    if(isError && !errorMessage.isNullOrEmpty()){
        Text(
            text = errorMessage ,
            color = Color.Red,
            modifier = Modifier.padding(top = 4.dp, start = 5.dp).fillMaxWidth(),
            textAlign =  TextAlign.Start,
            fontSize = 15.sp,
        )
    }

}
