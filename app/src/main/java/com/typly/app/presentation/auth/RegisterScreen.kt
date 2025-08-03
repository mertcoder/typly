package com.typly.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.typly.app.R
import com.typly.app.presentation.components.TransparentTextField
import com.typly.app.domain.model.AuthMethod
import com.typly.app.domain.model.AuthResult
import com.typly.app.util.RegisterValidationResult
import com.typly.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Composable function for the user registration screen.
 *
 * This screen provides a comprehensive registration interface supporting both traditional
 * email/password registration and Google Sign-In authentication. It features form validation,
 * error handling, loading states, and seamless navigation between different authentication flows.
 *
 * The screen includes:
 * - Email and password input fields with real-time validation
 * - Password confirmation field
 * - Google Sign-In integration using Credential Manager API
 * - Anonymous login option
 * - Navigation to login screen
 * - Responsive UI with loading indicators and error messages
 *
 * @param registerViewModel ViewModel managing registration state and business logic
 * @param onRegisterSuccess Callback executed when registration is successful but profile is incomplete
 * @param onAlreadyAuthenticated Callback executed when user is already fully registered and authenticated
 * @param onLoginClick Callback executed when user wants to navigate to login screen
 */
@Composable
fun RegisterScreen(
    registerViewModel: RegisterViewModel,
    onRegisterSuccess: () -> Unit,
    onAlreadyAuthenticated:()->Unit,
    onLoginClick: () -> Unit
) {
    // Local state for form inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // State collection from ViewModel
    val registerState by registerViewModel.registerState.collectAsState()
    val authState by registerViewModel.basicAuthState.collectAsState()
    val isLoading by registerViewModel.isLoading.collectAsState()

    // Google Sign-In configuration
    val SERVER_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID

    // Coroutine scope for async operations
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /**
     * LaunchedEffect for handling authentication state changes.
     *
     * Monitors the authentication result and navigates appropriately:
     * - If user has complete profile: navigate to main app
     * - If user profile is incomplete: navigate to profile setup
     * - Resets auth state after handling to prevent repeated navigation
     */
    LaunchedEffect(authState) {
        authState?.let { result ->
            when (result) {
                is AuthResult.Success -> {
                    if(result.data.isProfileComplete){
                        onAlreadyAuthenticated()
                    }else{
                        onRegisterSuccess()
                    }
                    registerViewModel.resetAuthState()
                }
                is AuthResult.Error -> {
                    // Error handling is done in UI below
                }
                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()){
        // Background image
        Image(
            painter = painterResource(id = R.drawable.blurrybg_dark),
            contentScale = ContentScale.Crop,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            // App logo section
            Spacer(modifier = Modifier.height(30.dp))
            Image(
                painter = painterResource(id = R.drawable.typlylogo),
                contentDescription = "Typly Logo",
                modifier = Modifier.height(170.dp)
            )

            // Registration form container
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(min = 410.dp)
                    .background(Color(0xEE0D0E20), shape = RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Email input field with validation
                TransparentTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email address",
                    isError = registerState is RegisterValidationResult.EmptyEmail ||
                            registerState is RegisterValidationResult.InvalidEmail,
                    errorMessage = when(registerState) {
                        is RegisterValidationResult.EmptyEmail -> "Email can not be blank!"
                        is RegisterValidationResult.InvalidEmail -> "Please enter a valid email address."
                        else -> null
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Password input field with validation
                TransparentTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    isPassword = true,
                    isError = registerState is RegisterValidationResult.EmptyPassword ||
                            registerState is RegisterValidationResult.PasswordTooShort,
                    errorMessage = when(registerState) {
                        is RegisterValidationResult.EmptyPassword -> "Password can not be blank!"
                        is RegisterValidationResult.PasswordTooShort -> "Password must be at least 6 characters long."
                        else -> null
                    }
                )

                Spacer(Modifier.height(20.dp))

                // Password confirmation field
                // Note: Currently uses same password variable - should be separate confirmPassword state
                TransparentTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password, Again",
                    isPassword = true,
                    isError = registerState is RegisterValidationResult.EmptyPassword ||
                            registerState is RegisterValidationResult.PasswordTooShort,
                    errorMessage = when(registerState) {
                        is RegisterValidationResult.EmptyPassword -> "Password can not be blank!"
                        is RegisterValidationResult.PasswordTooShort -> "Password must be at least 6 characters long."
                        else -> null
                    }
                )

                Spacer(Modifier.height(28.dp))

                // Primary registration button
                Button(
                    onClick = {
                        registerViewModel.register(AuthMethod.Basic(email,password))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_button)),
                    enabled = !isLoading
                ) {
                    if (isLoading && authState is AuthResult.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Register", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Display authentication error messages
                authState?.let { result ->
                    if (result is AuthResult.Error) {
                        Text(
                            text = result.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Google Sign-In button
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            try{
                                val credentialManager = CredentialManager.create(context)
                                val googleIdTokenOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(SERVER_CLIENT_ID)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdTokenOption)
                                    .build()
                                val result = credentialManager.getCredential(
                                    request = request, context=context
                                )
                                val credential = result.credential
                                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                                    val idToken = googleIdTokenCredential.idToken
                                    Log.d("RegisterScreen", "Google ID token received: $idToken")
                                    registerViewModel.register(AuthMethod.Basic(email,password))
                                    registerViewModel.register(method = AuthMethod.Google(idToken))
                                }else{
                                    Log.e("RegisterScreen", "Error getting ID token, credential type: ${credential.type}")
                                }

                            }catch (e: GetCredentialException){
                                Log.e("RegisterScreen", "Error in catch block while getting ID token: ${e.message}", e)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Continue with Google", color = Color.White)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Navigation to login screen
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Do you have an account?",
                        color = colorResource(R.color.white_shadowed),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Log in",
                        color = colorResource(R.color.purple_shadowed),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onLoginClick()
                        }
                    )
                }
            }
        }

        // Anonymous login option at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
                .align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Log in anonymously",
                color = colorResource(R.color.purple_shadowed),
                fontSize = 14.sp,
                modifier = Modifier.clickable {
                    // Anonymous login handled on login screen
                    onLoginClick()
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_arrowup),
                contentDescription = "Swipe up",
                modifier = Modifier.size(24.dp),
                tint = colorResource(R.color.purple_shadowed)
            )
        }
    }
}
