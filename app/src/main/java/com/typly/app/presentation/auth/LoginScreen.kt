package com.typly.app.presentation.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.typly.app.BuildConfig
import com.typly.app.R
import com.typly.app.presentation.components.TransparentTextField
import com.typly.app.domain.model.AuthResult
import com.typly.app.domain.model.LoginType
import com.typly.app.util.LoginValidationResult
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Main login screen composable that provides user authentication interface.
 * 
 * This screen offers multiple authentication methods:
 * - Email/password authentication with input validation
 * - Google Sign-In integration using Credential Manager API
 * - Anonymous login option
 * - Navigation to registration screen
 * 
 * The screen features:
 * - Real-time input validation with error display
 * - Loading states for authentication operations
 * - Responsive UI with background image and transparent form
 * - Error handling for authentication failures
 * - Automatic navigation based on authentication results
 * 
 * Authentication Flow:
 * 1. User enters credentials or chooses alternative method
 * 2. Input validation occurs in real-time
 * 3. Authentication request is sent to Firebase
 * 4. Loading indicators show progress
 * 5. Success leads to navigation, errors show user feedback
 * 6. Google users may need to complete profile setup
 * 
 * @param viewModel The LoginViewModel that handles authentication logic and state management
 * @param onLoginSuccess Callback invoked when authentication succeeds and user should navigate to main app
 * @param onRegisterClick Callback invoked when user wants to navigate to registration screen
 * @param onCreateGoogleProfile Callback invoked when Google user needs to complete profile setup
 * 
 * @see LoginViewModel for authentication business logic
 * @see TransparentTextField for custom input field component
 * @see AuthResult for possible authentication outcomes
 * @see LoginType for supported authentication methods
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onCreateGoogleProfile:()->Unit) {

    /**
     * User's email input state.
     * Supports real-time validation and error display.
     */
    var email by remember { mutableStateOf("") }
    
    /**
     * User's password input state.
     * Masked input with validation support.
     */
    var password by remember { mutableStateOf("") }
    
    /**
     * Current input validation state from the ViewModel.
     * Contains validation errors or success state for email/password inputs.
     * 
     * @see LoginValidationResult for possible validation states
     */
    val loginState by viewModel.loginState.collectAsState()
    
    /**
     * Current authentication state from the ViewModel.
     * Contains the result of authentication attempts and login method used.
     * 
     * @see LoginAuthStateUser for authentication state structure
     */
    val authState by viewModel.authState.collectAsState()
    
    /**
     * Loading state indicator for authentication operations.
     * Used to show progress indicators and disable UI during operations.
     */
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    /**
     * Google OAuth 2.0 server client ID for authentication.
     * Retrieved from BuildConfig to keep sensitive data secure.
     * Required for Google Sign-In credential validation.
     */
    val SERVER_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID

    /**
     * Side effect that handles authentication results and navigation.
     * 
     * This effect observes authentication state changes and:
     * - Navigates to main app on successful basic authentication
     * - Navigates to profile completion for new Google users
     * - Navigates to main app for existing Google users
     * - Resets auth state after successful navigation
     * 
     * The effect differentiates between authentication methods (BASIC vs GOOGLE)
     * and handles profile completion requirements for Google users.
     */
    LaunchedEffect(authState) {
        authState?.let { result ->
            if(result.loginType== LoginType.BASIC){
                when(result.firebaseUser){
                    is AuthResult.Success -> {
                        onLoginSuccess()
                        viewModel.resetAuthState()
                    }
                    else -> {
                    }
                }
            }else{
                when(result.userForGoogleUser){
                    is AuthResult.Success->{
                        if(result.userForGoogleUser.data?.isProfileComplete!=true){
                            onCreateGoogleProfile()
                        }else{
                            onLoginSuccess()
                            viewModel.resetAuthState()
                        }
                    }
                    else->{}
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        /**
         * Background image layer.
         * Uses a dark, blurred background image that covers the entire screen.
         * Provides visual depth and modern aesthetic to the login interface.
         */
        Image(
            painter = painterResource(id = R.drawable.blurrybg_dark),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        /**
         * Main content column containing logo and form.
         * Vertically centers content with appropriate spacing.
         */
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            /**
             * App logo section.
             * Displays the Typly brand logo at the top of the screen.
             */
            Spacer(modifier = Modifier.height(30.dp)) // bu kısmı artırarak içeriği aşağı iter
            Image(
                painter = painterResource(id = R.drawable.typlylogo),
                contentDescription = "Typly Logo",
                modifier = Modifier
                    .height(170.dp)
            )

            /**
             * Main form container with transparent background.
             * Contains all input fields, buttons, and form-related content.
             * Uses rounded corners and semi-transparent background for modern appearance.
             */
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .heightIn(min = 410.dp)
                    .background(Color(0xEE0D0E20), shape = RoundedCornerShape(28.dp))
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                /**
                 * Email input field with validation.
                 * 
                 * Features:
                 * - Real-time validation feedback
                 * - Error state visualization
                 * - Transparent design matching form aesthetic
                 * - Contextual error messages for different validation failures
                 */
                TransparentTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "Email address",
                    isError = loginState is LoginValidationResult.EmptyEmail ||
                            loginState is LoginValidationResult.InvalidEmail,
                    errorMessage = when (loginState) {
                        is LoginValidationResult.EmptyEmail -> "Email can not be blank!"
                        is LoginValidationResult.InvalidEmail -> "Please enter a valid email address."
                        else -> null
                    }
                )

                Spacer(Modifier.height(20.dp))

                /**
                 * Password input field with validation.
                 * 
                 * Features:
                 * - Masked input for security
                 * - Validation for empty password
                 * - Error state visualization
                 * - Consistent styling with email field
                 */
                TransparentTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    isPassword = true,
                    isError = loginState is LoginValidationResult.EmptyPassword,
                    errorMessage = if (loginState is LoginValidationResult.EmptyPassword)
                        "Password can not be blank!" else null

                )

                Spacer(Modifier.height(28.dp))

                /**
                 * Primary login button for email/password authentication.
                 * 
                 * Features:
                 * - Triggers email/password validation and authentication
                 * - Shows loading indicator during authentication
                 * - Disabled state during loading to prevent duplicate requests
                 * - Purple brand color matching app theme
                 * - Full-width design for easy interaction
                 */
                Button(
                    onClick = {
                        viewModel.login(email, password)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.purple_button)),
                    enabled = !isLoading
                ) {
                    if (isLoading && authState?.firebaseUser is AuthResult.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Log in", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(28.dp))

                /**
                 * Google Sign-In button using Credential Manager API.
                 * 
                 * This button initiates Google authentication flow:
                 * 1. Creates credential manager instance
                 * 2. Configures Google ID token options with server client ID
                 * 3. Requests credentials from Google
                 * 4. Extracts ID token from credential result
                 * 5. Passes token to ViewModel for Firebase authentication
                 * 
                 * Features:
                 * - Modern Credential Manager API (replaces deprecated Google Sign-In SDK)
                 * - Automatic credential selection when available
                 * - Comprehensive error handling for credential failures
                 * - Loading state support
                 * - Google branding with icon
                 * 
                 * @throws GetCredentialException when credential request fails
                 */
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
                                    Log.d("LoginScreen", "Google ID token received: $idToken")
                                    viewModel.considerGoogleAuth(idToken)
                                }else{
                                    Log.e("LoginScreen", "Error getting ID token, credential type: ${credential.type}")
                                }

                            }catch (e: GetCredentialException){
                                Log.e("LoginScreen", "Error in catch block while getting ID token: ${e.message}", e)
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

                /**
                 * Authentication error display section.
                 * 
                 * Shows user-friendly error messages when authentication fails.
                 * Currently displays generic "Incorrect email or password" message
                 * for basic authentication errors to maintain security.
                 */
                authState?.let { result ->
                    if (result.firebaseUser is  AuthResult.Error) {
                        Text(
                            text = "Incorrect email or password.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                /**
                 * Registration navigation section.
                 * 
                 * Provides clear call-to-action for users who need to create an account.
                 * Uses contrasting colors to make the registration option prominent.
                 * Triggers navigation to registration screen when clicked.
                 */
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Don't have an account?",
                        color = colorResource(R.color.white_shadowed),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Register",
                        color = colorResource(R.color.purple_shadowed),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable {
                            onRegisterClick()
                        }
                    )
                }
            }
        }

        /**
         * Anonymous login option section.
         * 
         * Positioned at the bottom of the screen as an alternative authentication method.
         * Features:
         * - Quick access for users who want to try the app without registration
         * - Visual indicator (arrow) suggesting swipe gesture
         * - Currently triggers loginAnonymously() method (not implemented)
         * - Subtle styling to not compete with primary authentication methods
         * 
         * Note: Anonymous authentication is not yet implemented in the ViewModel.
         */
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
                    viewModel.loginAnonymously()
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
