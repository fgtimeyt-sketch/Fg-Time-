package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Language
import com.example.viewmodel.Screen

@Composable
fun SettingsDialog(
    viewModel: FGTimeViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isHindi = uiState.language == Language.HINDI

    var isPrivateAccount by remember { mutableStateOf(false) }
    var isOnlineStatusVisible by remember { mutableStateOf(true) }
    var pushNotifications by remember { mutableStateOf(true) }
    var allowCallsFrom by remember { mutableStateOf("Followers Only") }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = FGGreenPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isHindi) "सेटिंग्स और गोपनीयता केंद्र" else "Settings & Privacy Center", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section: Founder & Admin Portal
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FGGreenPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth().clickable {
                        onDismiss()
                        viewModel.navigateTo(Screen.ADMIN_DASHBOARD)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = FGGreenPrimary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Owner & Admin Portal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Moderation, metrics, and owner controls (Muksidul)", style = MaterialTheme.typography.labelSmall)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FGGreenPrimary)
                    }
                }

                // Section: Appearance & Language
                Text(
                    text = if (isHindi) "दिखावट और भाषा (Display & Language)" else "Display & Language",
                    style = MaterialTheme.typography.titleSmall,
                    color = FGGreenPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isHindi) "डार्क थीम (Dark Mode)" else "Dark Mode")
                    Switch(
                        checked = uiState.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        colors = SwitchDefaults.colors(checkedThumbColor = FGGreenPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (isHindi) "भाषा (Language: हिन्दी)" else "Language (हिन्दी / English)")
                    Button(
                        onClick = { viewModel.toggleLanguage() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isHindi) "English" else "हिन्दी", color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                Divider()

                // Section: Privacy Controls
                Text(
                    text = if (isHindi) "गोपनीयता नियंत्रण (Privacy)" else "Privacy & Permissions",
                    style = MaterialTheme.typography.titleSmall,
                    color = FGGreenPrimary,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(if (isHindi) "निजी खाता (Private Account)" else "Private Account")
                        Text(
                            text = if (isHindi) "केवल स्वीकृत फ़ॉलोअर्स आपकी पोस्ट देख सकते हैं" else "Only approved followers can view your posts and reels",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isPrivateAccount,
                        onCheckedChange = { isPrivateAccount = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = FGGreenPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Show Online Activity Status")
                        Text(
                            text = "Allow connections to see when you are active on FG TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isOnlineStatusVisible,
                        onCheckedChange = { isOnlineStatusVisible = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = FGGreenPrimary)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Incoming Call Security")
                    AssistChip(
                        onClick = {
                            allowCallsFrom = if (allowCallsFrom == "Followers Only") "Everyone" else "Followers Only"
                        },
                        label = { Text(allowCallsFrom) }
                    )
                }

                Divider()

                // Section: Data & Help
                Text(
                    text = "Data, Community & Help",
                    style = MaterialTheme.typography.titleSmall,
                    color = FGGreenPrimary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        showInfoDialog = Pair(
                            "Export FG TIME Data",
                            "Your personal data archive request has been queued. An export ZIP containing your photos, posts, bookmarks, and chat history will be compiled."
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download My Account Data")
                }

                OutlinedButton(
                    onClick = {
                        showInfoDialog = Pair(
                            "FG TIME Community Guidelines",
                            "1. Authentic Growth: We value respectful creativity.\n2. Respect Intellectual Property: Keep remixes collaborative.\n3. Safety: Harassment, hate speech, and spam are strictly prohibited."
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Community Guidelines & Rules")
                }

                Divider()

                // Section: Account Actions
                Text(
                    text = if (isHindi) "खाता क्रियाएं (Account Actions)" else "Account Actions",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )

                // Log out button
                OutlinedButton(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_settings_logout"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isHindi) "लॉग आउट करें (Log Out)" else "Log Out")
                }

                // Delete account button
                Button(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isHindi) "खाता हटाएं (Delete Account)" else "Delete Account",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = FGGreenPrimary)
            }
        }
    )

    // Info Dialog
    showInfoDialog?.let { info ->
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            title = { Text(info.first, fontWeight = FontWeight.Bold) },
            text = { Text(info.second) },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = null }) { Text("OK", color = FGGreenPrimary) }
            }
        )
    }

    // Logout Confirmation Dialog
    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text(if (isHindi) "लॉग आउट की पुष्टि करें" else "Confirm Logout") },
            text = {
                Text(
                    if (isHindi) "क्या आप सचमुच FG TIME से लॉग आउट करना चाहते हैं?"
                    else "Are you sure you want to log out of FG TIME?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onDismiss()
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary)
                ) {
                    Text("Log Out", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Account Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account Confirmation", color = MaterialTheme.colorScheme.error) },
            text = {
                Text(
                    "This action is permanent and will clear all your session data, posts, reels, and bookmarks."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDismiss()
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
