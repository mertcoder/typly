package com.typly.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.typly.app.R

/**
 * A completely transparent text field with optional password masking and error handling.
 * 
 * Provides a text input field with fully transparent background and borders,
 * suitable for overlay designs. Supports password input with visual transformation,
 * error state indication, and custom error message display. Uses white text
 * with transparent styling throughout.
 * 
 * @param value Current text value of the field
 * @param onValueChange Callback triggered when text value changes
 * @param placeholder Placeholder text to display when field is empty
 * @param isPassword Whether to mask input as password (dots)
 * @param isError Whether the field is in error state
 * @param errorMessage Optional error message to display below the field
 */
@Composable
fun TransparentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color(0xFFCCCCCC)) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            cursorColor = Color.White,
            focusedIndicatorColor = if (isError) Color.Red else Color.Transparent,
            unfocusedIndicatorColor = if (isError) Color.Red else Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            disabledTextColor = Color.White,
            errorTextColor = Color.Red
        ),
        isError = isError
    )
    if (isError && !errorMessage.isNullOrEmpty()) {
        Text(
            text = errorMessage,
            color = colorResource(R.color.purple_grey_description),
            modifier = Modifier.padding(top = 4.dp, start = 5.dp).fillMaxWidth(),
            textAlign =  TextAlign.Start,
            fontSize = 15.sp,
        )
    }
}
