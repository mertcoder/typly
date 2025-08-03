package com.typly.app.presentation.main.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * Main settings screen composable that displays user preferences and app settings.
 * 
 * Provides a comprehensive settings interface including account preferences,
 * notifications, appearance, data management, and account actions.
 * 
 * @param onSignOut Callback triggered when user signs out
 * @param onDeleteAccount Callback for account deletion (TODO: Implementation pending)
 * @param onExportData Callback for data export functionality (TODO: Implementation pending)
 * @param onChangeTheme Callback for theme switching (TODO: Implementation pending)
 */
@Composable
fun SettingsScreen(
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {}, // TODO: Implement account deletion
    onExportData: () -> Unit = {}, // TODO: Implement data export
    onChangeTheme: (Boolean) -> Unit = {} // TODO: Implement theme switching
) {
    var isDarkMode by remember { mutableStateOf(true) }
    var cacheSize by remember { mutableStateOf("Calculating...") }
    var showClearCacheSuccess by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }

    val viewModel = hiltViewModel<SettingsViewModel>()
    
    // Collect states from ViewModel
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val readReceiptsEnabled by viewModel.readReceiptsEnabled.collectAsState()
    val lastSeenEnabled by viewModel.lastSeenEnabled.collectAsState()
    val autoDownloadEnabled by viewModel.autoDownloadEnabled.collectAsState()
    val doNotDisturbEnabled by viewModel.doNotDisturbEnabled.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    
    // Calculate cache size on screen load
    LaunchedEffect(Unit) {
        cacheSize = viewModel.getCacheSize()
    }
    
    // Auto-hide success message
    LaunchedEffect(showClearCacheSuccess) {
        if (showClearCacheSuccess) {
            kotlinx.coroutines.delay(2000)
            showClearCacheSuccess = false
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        
        // Account & Privacy Section
        SettingsCard(
            title = "Account & Privacy",
            icon = Icons.Default.AccountCircle
        ) {
            SettingsItem(
                title = "Profile Settings",
                subtitle = "Edit your profile information",
                icon = Icons.Default.Person,
                onClick = { /* TODO: Navigate to profile edit */ }
            )
            
            SettingsDivider()
            
            SettingsToggleItem(
                title = "Read Receipts",
                subtitle = "Let others know when you've read their messages",
                icon = Icons.Default.Visibility,
                checked = readReceiptsEnabled,
                onCheckedChange = { viewModel.updateReadReceipts(it) }
            )
            
            SettingsDivider()
            
            SettingsToggleItem(
                title = "Last Seen",
                subtitle = "Show when you were last active",
                icon = Icons.Default.Schedule,
                checked = lastSeenEnabled,
                onCheckedChange = { viewModel.updateLastSeen(it) }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "Blocked Users",
                subtitle = "Manage blocked contacts",
                icon = Icons.Default.Block,
                onClick = { /* TODO: Navigate to blocked users */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notifications Section
        SettingsCard(
            title = "Notifications",
            icon = Icons.Default.Notifications
        ) {
            SettingsToggleItem(
                title = "Push Notifications",
                subtitle = "Receive notifications for new messages",
                icon = Icons.Default.NotificationsActive,
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.updateNotifications(it) }
            )
            
            SettingsDivider()
            
            SettingsToggleItem(
                title = "🌙 Do Not Disturb",
                subtitle = if (doNotDisturbEnabled) "Active until 8:00 AM" else "Schedule quiet hours",
                icon = Icons.Default.DoNotDisturb,
                checked = doNotDisturbEnabled,
                onCheckedChange = { viewModel.updateDoNotDisturb(it) }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "Notification Sound",
                subtitle = "Default notification tone",
                icon = Icons.Default.VolumeUp,
                onClick = { /* TODO: Open sound picker */ }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "Message Previews",
                subtitle = "Show message content in notifications",
                icon = Icons.Default.Preview,
                onClick = { /* TODO: Configure message previews */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Appearance Section
        SettingsCard(
            title = "Appearance",
            icon = Icons.Default.Palette
        ) {
            SettingsToggleItem(
                title = "🌙 Dark Mode",
                subtitle = "Use dark theme for better night viewing",
                icon = Icons.Default.DarkMode,
                checked = isDarkMode,
                onCheckedChange = { 
                    isDarkMode = it
                    onChangeTheme(it)
                }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "🎨 Chat Themes",
                subtitle = "Customize your chat appearance",
                icon = Icons.Default.ColorLens,
                onClick = { /* TODO: Open theme selector */ }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "Font Size",
                subtitle = fontSize.name,
                icon = Icons.Default.TextFields,
                onClick = { showFontSizeDialog = true }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Data & Storage Section
        SettingsCard(
            title = "Data & Storage",
            icon = Icons.Default.Storage
        ) {
            SettingsToggleItem(
                title = "📱 Auto-download Media",
                subtitle = "Automatically download photos and videos",
                icon = Icons.Default.CloudDownload,
                checked = autoDownloadEnabled,
                onCheckedChange = { viewModel.updateAutoDownload(it) }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "💾 Storage Usage",
                subtitle = "$cacheSize used • Cache data",
                icon = Icons.Default.Storage,
                onClick = { 
                    // Refresh cache size
                    cacheSize = viewModel.getCacheSize()
                }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "🗂️ Export Data",
                subtitle = "Download your chat history and data",
                icon = Icons.Default.Download,
                onClick = onExportData
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "🧹 Clear Cache",
                subtitle = if (showClearCacheSuccess) "✅ Cache cleared successfully!" else "Free up space by clearing app cache",
                icon = Icons.Default.CleaningServices,
                onClick = { 
                    if (viewModel.clearCache()) {
                        showClearCacheSuccess = true
                        cacheSize = viewModel.getCacheSize()
                    }
                }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // About & Support Section
        SettingsCard(
            title = "About & Support",
            icon = Icons.Default.Info
        ) {
            SettingsItem(
                title = "📱 App Version",
                subtitle = viewModel.getAppVersion(),
                icon = Icons.Default.AppRegistration,
                onClick = { /* Could add update check here */ }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "❓ Help & FAQ",
                subtitle = "Get help and find answers",
                icon = Icons.Default.Help,
                onClick = { /* TODO: Open help center */ }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "🐛 Report Bug",
                subtitle = "Help us improve the app",
                icon = Icons.Default.BugReport,
                onClick = { /* TODO: Open bug report */ }
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "⭐ Rate App",
                subtitle = "Share your experience on app store",
                icon = Icons.Default.Star,
                onClick = { /* TODO: Open app store rating */ }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Actions Section (Dangerous actions)
        SettingsCard(
            title = "Account Actions",
            icon = Icons.Default.Warning,
            isDangerous = true
        ) {
            SettingsItem(
                title = "🚪 Sign Out",
                subtitle = "Sign out from this device",
                icon = Icons.Default.Logout,
                onClick =
                    {
                        onSignOut()
                        viewModel.signOut()
                    },
                isDangerous = true
            )
            
            SettingsDivider()
            
            SettingsItem(
                title = "🗑️ Delete Account",
                subtitle = "Permanently delete your account and data",
                icon = Icons.Default.DeleteForever,
                onClick = onDeleteAccount,
                isDangerous = true
            )
        }
        
        Spacer(modifier = Modifier.height(100.dp)) // Extra space for bottom navigation
    }
    
    // Font Size Dialog
    if (showFontSizeDialog) {
        AlertDialog(
            onDismissRequest = { showFontSizeDialog = false },
            title = { Text("Font Size", color = Color.White) },
            text = {
                Column {
                    FontSize.values().forEach { size ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.updateFontSize(size)
                                    showFontSizeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = fontSize == size,
                                onClick = {
                                    viewModel.updateFontSize(size)
                                    showFontSizeDialog = false
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.White,
                                    unselectedColor = Color.White.copy(alpha = 0.6f)
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = size.name,
                                color = Color.White,
                                fontSize = when(size) {
                                    FontSize.Small -> 14.sp
                                    FontSize.Medium -> 16.sp
                                    FontSize.Large -> 18.sp
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontSizeDialog = false }) {
                    Text("Done", color = Color.White)
                }
            },
            containerColor = Color.Black.copy(alpha = 0.9f)
        )
    }
}

/**
 * Reusable card component for grouping related settings items.
 * 
 * @param title The section title displayed at the top of the card
 * @param icon The icon displayed next to the title
 * @param isDangerous Whether this card contains dangerous actions (changes styling)
 * @param content The content composables to be displayed within the card
 */
@Composable
fun SettingsCard(
    title: String,
    icon: ImageVector,
    isDangerous: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isDangerous) {
                        listOf(
                            Color.Red.copy(alpha = 0.05f),
                            Color.Red.copy(alpha = 0.02f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.025f),
                            Color.White.copy(alpha = 0.05f)
                        )
                    }
                )
            )
            .padding(24.dp)
    ) {
        Column {
            // Section Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (isDangerous) Color.Red.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = if (isDangerous) Color.Red.copy(alpha = 0.9f) else Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            content()
        }
    }
}

/**
 * Individual settings item with click functionality.
 * 
 * @param title Primary text displayed for the setting
 * @param subtitle Optional secondary text providing additional context
 * @param icon Icon displayed to the left of the text
 * @param onClick Callback triggered when the item is clicked
 * @param isDangerous Whether this is a dangerous action (changes styling)
 */
@Composable
fun SettingsItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    onClick: () -> Unit,
    isDangerous: Boolean = false
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDangerous) Color.Red.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = if (isDangerous) Color.Red.copy(alpha = 0.9f) else Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = if (isDangerous) Color.Red.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Navigate",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Settings item with toggle switch functionality.
 * 
 * @param title Primary text displayed for the setting
 * @param subtitle Optional secondary text providing additional context
 * @param icon Icon displayed to the left of the text
 * @param checked Current state of the toggle switch
 * @param onCheckedChange Callback triggered when toggle state changes
 */
@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
        Text(
                text = title,
            color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp
                )
            }
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.White.copy(alpha = 0.3f),
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
            )
        )
    }
}

/**
 * Visual divider component used to separate settings items within a card.
 * Creates a subtle horizontal line with proper spacing.
 */
@Composable
fun SettingsDivider() {
    Spacer(modifier = Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.1f))
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Preview()
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(
        onSignOut = { /* Preview action */ },
        onDeleteAccount = { /* Preview action */ },
        onExportData = { /* Preview action */ },
        onChangeTheme = { /* Preview action */ }
    )
}
