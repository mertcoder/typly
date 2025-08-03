package com.typly.app.presentation.main.home

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.typly.app.R
import com.typly.app.presentation.components.BottomBar
import com.typly.app.presentation.navigation.Screen
import com.typly.app.presentation.main.chats.ChatsScreen
import com.typly.app.presentation.main.conversations.OneToOneChatScreen
import com.typly.app.presentation.main.profile.ProfileScreen
import com.typly.app.presentation.main.search.SearchUserScreen
import com.typly.app.presentation.main.settings.SettingsScreen
import com.typly.app.presentation.call.AudioCallScreen
import com.typly.app.presentation.call.IncomingCallScreen
import com.typly.app.util.security.ChatSecurityManager
import com.typly.app.util.security.SecureTokenManager
import com.typly.app.presentation.call.CallViewModel

/**
 * Main home screen composable that serves as the primary navigation container.
 * 
 * Provides the main application interface with bottom navigation, handles incoming calls,
 * manages notification permissions, and hosts all main app screens including chats,
 * profile, settings, and search. Features call state management and secure chat navigation.
 * 
 * @param onSignedOut Callback triggered when user signs out from the application
 */
@Composable
fun HomeScreen(
    onSignedOut: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val viewModel: HomeScreenViewModel = hiltViewModel()
    val callViewModel: CallViewModel = hiltViewModel()

    // Listen for incoming calls
    val incomingCall by callViewModel.incomingCall.collectAsState(initial = null)
    val isInCall by callViewModel.isInCall.collectAsState(initial = false)


    // NOTIFICATION PERMISSION HANDLING
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("HomeScreen", "Notification permission granted.")
            // You can trigger token retrieval here
        } else {
            Log.d("HomeScreen", "Notification permission denied.")
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // when ifadesi ile daha okunaklı bir kontrol
            when {
                // 1. Durum: İzin zaten verilmiş
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    Log.d("HomeScreen", "Permission already granted.")
                    // You can trigger token retrieval here
                }

                // 2. Durum: Kullanıcı izni daha önce reddetmiş. Açıklama göstermek için en iyi yer.
                (context as? Activity)?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == true -> {
                    Log.d("HomeScreen", "User should be shown why permission is needed.")
                    // Here you can show a Dialog, Snackbar etc. to the user with
                    // "Permission is required to receive chat and call notifications."
                    // For now, we're asking directly again:
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }

                // 3. Durum: İzin daha önce hiç sorulmamış.
                else -> {
                    Log.d("HomeScreen", "Permission requested for the first time.")
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // No permission needed for Android below 13
            Log.d("HomeScreen", "Android below 13, no permission needed.")
            // You can trigger token retrieval here
        }
    }
    // Handle incoming call navigation
    LaunchedEffect(incomingCall) {
        // Only navigate when we have a NEW incoming call (not when clearing it)
        if (incomingCall != null &&
            !isInCall &&
            !currentRoute?.startsWith("incoming_call")!! &&
            !currentRoute?.startsWith("audio_call")!! &&
            !currentRoute?.startsWith("one_to_one_chat")!!) {
            val callerName = callViewModel.getUserNicknameById(incomingCall!!.callerId)?: "Unknown"
            navController.navigate(Screen.IncomingCall.createRoute(incomingCall!!.callId, callerName)) {
                popUpTo(currentRoute) { inclusive = false }
            }
        }
    }



    // Hide BottomBar on chat and call screens
    val shouldShowBottomBar = currentRoute?.startsWith("one_to_one_chat") != true && 
                             currentRoute?.startsWith("audio_call") != true &&
                             currentRoute?.startsWith("incoming_call") != true

    // Full screen box that contains everything
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image that fills the entire screen
        Image(
            painter = painterResource(id = R.drawable.background_home),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.FillBounds
        )
        
        // Scaffold with transparent container
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = { 
                if (shouldShowBottomBar) {
                    BottomBar(navController)
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Chats.route,
                modifier = if (shouldShowBottomBar) Modifier.padding(innerPadding) else Modifier.fillMaxSize(),
                enterTransition = {
                    fadeIn(animationSpec = tween(200))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(200))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(200))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(200))
                }
            ) {
                composable(
                    route = Screen.Chats.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(200))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(200))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(200))
                    }
                ){
                    ChatsScreen(
                        onSearchUserIconClicked = {
                            navController.navigate(Screen.SearchUser.route) {
                                popUpTo(Screen.Chats.route) { inclusive = false }
                            }
                        },
                        onChatSelected = { chatId: String ->
                            // Navigate to one-to-one chat screen with the chat ID
                            val currentUserId =viewModel.getCurrentUserId()
                            val chatSecurityManager = ChatSecurityManager()
                            val token = chatSecurityManager.createChatSession(currentUserId, chatId)
                            if(token != null){
                                // No need for URL encoding with URL-safe Base64
                                navController.navigate("one_to_one_chat/$token") {
                                    popUpTo(Screen.Chats.route) { inclusive = false }
                                }
                            }else{
                                Log.e("HomeScreen", "Failed to create chat session")
                            }
                        })
                }
                composable(Screen.Profile.route) {
                    ProfileScreen()
                }
                composable(
                    Screen.Settings.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(200))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(200))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(200))
                    })
                {
                    SettingsScreen(onSignOut = {
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(Screen.HomeScreen.route) { inclusive = true }
                        }
                    })
                }
                composable(
                    route = Screen.SearchUser.route,
                    enterTransition = {
                        fadeIn(animationSpec = tween(200))
                    },
                    exitTransition = {
                        fadeOut(animationSpec = tween(200))
                    },
                    popEnterTransition = {
                        fadeIn(animationSpec = tween(200))
                    },
                    popExitTransition = {
                        fadeOut(animationSpec = tween(200))
                    }
                ){
                    SearchUserScreen(
                        onUserSelected = { targetUserId: String ->
                            val currentUserId = viewModel.getCurrentUserId()
                            val chatSecurityManager = ChatSecurityManager()
                            val token = chatSecurityManager.createChatSession(currentUserId, targetUserId)
                            if(token != null){
                                // No need for URL encoding with URL-safe Base64
                                navController.navigate(Screen.OneToOneChat.createRoute(token)) {
                                    popUpTo(Screen.SearchUser.route) { inclusive = false }
                                }
                            }else{
                                Log.e("HomeScreen", "Failed to create chat session")
                            }
                        },
                        onBackPressed = { navController.popBackStack()},

                    )
                }
                composable(
                    route = Screen.OneToOneChat.route,
                    arguments = listOf(navArgument("token") { type = NavType.StringType })
                ) { backStackEntry ->
                    val token = backStackEntry.arguments?.getString("token") ?: ""
                    val targetUserId = SecureTokenManager.extractUserIdFromSecureToken(token)
                                Log.d("HomeScreen", "Navigation token: $token")
            Log.d("HomeScreen", "Target User ID from token: $targetUserId")

                    if (targetUserId != null) {
                        OneToOneChatScreen(
                            targetUserId = targetUserId,
                            onBackPressed = { navController.popBackStack() },
                            onAudioCall = {
                                navController.navigate(Screen.AudioCall.createRoute(targetUserId, isReceiver = false)) {
                                    popUpTo(Screen.OneToOneChat.route) { inclusive = false }
                                }
                            },
                            onIncomingCall = { callId, callerName ->
                                navController.navigate(Screen.IncomingCall.createRoute(callId, callerName)) {
                                    popUpTo(Screen.OneToOneChat.route) { inclusive = false }
                                }
                            }
                        )
                    } else {
                        // Token is invalid or user not found - go back to chats
                        LaunchedEffect(Unit) {
                            navController.navigate(Screen.Chats.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                }
                composable(
                    route = Screen.AudioCall.route,
                    arguments = listOf(
                        navArgument("targetUserId") { type = NavType.StringType },
                        navArgument("isReceiver") { 
                            type = NavType.BoolType
                            defaultValue = false 
                        }
                    ),
                    enterTransition = { fadeIn(animationSpec = tween(200)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) { backStackEntry ->
                    val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
                    val isReceiver = backStackEntry.arguments?.getBoolean("isReceiver") ?: false
                    
                    if (targetUserId.isNotEmpty()) {
                        AudioCallScreen(
                            targetUserId = targetUserId,
                            isReceiver = isReceiver,
                            callViewModel = callViewModel,
                            onCallEnd = { 
                                navController.popBackStack() 
                            }
                        )
                    } else {
                        // Invalid targetUserId - go back to chats
                        LaunchedEffect(Unit) {
                            navController.navigate(Screen.Chats.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
                composable(route = Screen.LoginScreen.route){
                    onSignedOut()
                }
                
                // Incoming Call Screen
                composable(
                    route = Screen.IncomingCall.route,
                    arguments = listOf(
                        navArgument("callId") { type = NavType.StringType },
                        navArgument("callerName") { type = NavType.StringType }
                    ),
                    enterTransition = { fadeIn(animationSpec = tween(200)) },
                    exitTransition = { fadeOut(animationSpec = tween(200)) }
                ) { backStackEntry ->
                    val callId = backStackEntry.arguments?.getString("callId") ?: ""
                    val callerName = backStackEntry.arguments?.getString("callerName") ?: "Unknown"
                    
                    val currentCall = callViewModel.incomingCall.collectAsState(initial = null).value
                    val uiState = callViewModel.uiState.collectAsState().value
                    
                    // Be more tolerant - if we have the callId in route, try to show the screen
                    when {
                        currentCall != null && currentCall.callId == callId -> {
                            // Perfect match - use currentCall
                            IncomingCallScreen(
                                call = currentCall,
                                callerName = callerName,
                                callViewModel = callViewModel,
                                onAccept = {
                                    navController.navigate(Screen.AudioCall.createRoute(currentCall.callerId, isReceiver = true)) {
                                        popUpTo(Screen.IncomingCall.route) { inclusive = true }
                                    }
                                },
                                onReject = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        uiState.callId == callId && uiState.incomingCall != null -> {
                            // Use call from uiState
                            IncomingCallScreen(
                                call = uiState.incomingCall,
                                callerName = callerName,
                                callViewModel = callViewModel,
                                onAccept = {
                                    navController.navigate(Screen.AudioCall.createRoute(uiState.incomingCall!!.callerId, isReceiver = true)) {
                                        popUpTo(Screen.IncomingCall.route) { inclusive = true }
                                    }
                                },
                                onReject = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        callId.isNotEmpty() -> {
                            // Don't immediately pop - let IncomingCallScreen handle the missing call
                            
                            // Show a temporary screen or let IncomingCallScreen handle it
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color.Black),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    CircularProgressIndicator(color = Color.White)
                                    Text(
                                        text = "Loading call...",
                                        color = Color.White,
                                        fontSize = 18.sp
                                    )
                                }
                            }
                            
                            // Auto-pop after a reasonable timeout if no call appears
                            LaunchedEffect(callId) {
                                delay(5000) // Wait 5 seconds for call to appear
                                if (callViewModel.uiState.value.incomingCall?.callId != callId) {
                                    navController.popBackStack()
                                }
                            }
                        }
                        else -> {
                            // No callId, go back immediately
                            LaunchedEffect(Unit) {
                                navController.popBackStack()
                            }
                        }
                    }
                }
        }

    }
}
}
