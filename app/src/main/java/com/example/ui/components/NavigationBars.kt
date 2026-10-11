package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FGGreenDark
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.ui.theme.FGPurpleAccent
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Language
import com.example.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FGTimeTopBar(
    viewModel: FGTimeViewModel,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState = viewModel.uiState.value
    val isDark = uiState.isDarkMode
    val unreadNotifs = uiState.notifications.count { !it.isRead }

    TopAppBar(
        modifier = modifier.testTag("fgtime_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = if (isDark) Color(0xFF0C1D15) else Color(0xFFFFFFFF),
            titleContentColor = if (isDark) Color.White else FGGreenDark
        ),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { viewModel.navigateTo(Screen.FEED) }
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(FGGreenPrimary.copy(alpha = 0.2f))
                        .border(1.5.dp, FGGreenPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.fgtime_leaf_logo),
                        contentDescription = "FG Leaf Emblem",
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "FG TIME",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = if (isDark) Color.White else FGGreenDark
                    )
                }
            }
        },
        actions = {
            // Language quick toggle
            IconButton(
                onClick = { viewModel.toggleLanguage() },
                modifier = Modifier.testTag("btn_top_lang")
            ) {
                Text(
                    text = if (uiState.language == Language.HINDI) "हिं" else "EN",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black,
                    color = FGGreenPrimary
                )
            }

            // Notifications
            BadgedBox(
                badge = {
                    if (unreadNotifs > 0) {
                        Badge(
                            containerColor = FGGreenPrimary,
                            contentColor = Color.Black
                        ) {
                            Text("$unreadNotifs")
                        }
                    }
                }
            ) {
                IconButton(
                    onClick = { viewModel.navigateTo(Screen.NOTIFICATIONS) },
                    modifier = Modifier.testTag("btn_top_notifications")
                ) {
                    Icon(
                        imageVector = if (uiState.currentScreen == Screen.NOTIFICATIONS)
                            Icons.Filled.Notifications
                        else
                            Icons.Outlined.Notifications,
                        contentDescription = "Notifications",
                        tint = if (uiState.currentScreen == Screen.NOTIFICATIONS) FGGreenPrimary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Direct Messages
            IconButton(
                onClick = { viewModel.navigateTo(Screen.DIRECT_MESSAGES) },
                modifier = Modifier.testTag("btn_top_messages")
            ) {
                Icon(
                    imageVector = if (uiState.currentScreen == Screen.DIRECT_MESSAGES)
                        Icons.Filled.ChatBubble
                    else
                        Icons.Outlined.ChatBubbleOutline,
                    contentDescription = "Messages",
                    tint = if (uiState.currentScreen == Screen.DIRECT_MESSAGES) FGGreenPrimary else MaterialTheme.colorScheme.onSurface
                )
            }

            // Settings Menu
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier.testTag("btn_top_settings")
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun FGTimeBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .testTag("fgtime_bottom_bar")
            .navigationBarsPadding(),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        // Feed (Home)
        NavigationBarItem(
            selected = currentScreen == Screen.FEED,
            onClick = { onNavigate(Screen.FEED) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.FEED) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home"
                )
            },
            label = { Text("Feed") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FGGreenPrimary,
                selectedTextColor = FGGreenPrimary,
                indicatorColor = FGGreenPrimary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_home")
        )

        // Explore (Search)
        NavigationBarItem(
            selected = currentScreen == Screen.EXPLORE,
            onClick = { onNavigate(Screen.EXPLORE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.EXPLORE) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Search"
                )
            },
            label = { Text("Explore") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FGGreenPrimary,
                selectedTextColor = FGGreenPrimary,
                indicatorColor = FGGreenPrimary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_explore")
        )

        // Remix Studio (Special Highlighted Center Action)
        NavigationBarItem(
            selected = currentScreen == Screen.REMIX_STUDIO,
            onClick = { onNavigate(Screen.REMIX_STUDIO) },
            icon = {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(FGGreenPrimary, FGPurpleAccent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = "Remix Studio",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            label = { Text("Studio", fontWeight = FontWeight.Bold) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FGGreenPrimary,
                selectedTextColor = FGGreenPrimary,
                indicatorColor = FGGreenPrimary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_remix_studio")
        )

        // Reels
        NavigationBarItem(
            selected = currentScreen == Screen.REELS,
            onClick = { onNavigate(Screen.REELS) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.REELS) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircleOutline,
                    contentDescription = "Reels"
                )
            },
            label = { Text("Reels") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FGGreenPrimary,
                selectedTextColor = FGGreenPrimary,
                indicatorColor = FGGreenPrimary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_reels")
        )

        // Profile
        NavigationBarItem(
            selected = currentScreen == Screen.PROFILE,
            onClick = { onNavigate(Screen.PROFILE) },
            icon = {
                Icon(
                    imageVector = if (currentScreen == Screen.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                    contentDescription = "Profile"
                )
            },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = FGGreenPrimary,
                selectedTextColor = FGGreenPrimary,
                indicatorColor = FGGreenPrimary.copy(alpha = 0.2f)
            ),
            modifier = Modifier.testTag("nav_profile")
        )
    }
}
