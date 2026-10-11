package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FGTimeBottomBar
import com.example.ui.components.FGTimeTopBar
import com.example.ui.components.SettingsDialog
import com.example.ui.screens.*
import com.example.ui.theme.FGTimeTheme
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: FGTimeViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            var showSettingsDialog by remember { mutableStateOf(false) }

            // Trigger snackbars on state message changes
            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearSnackbar()
                }
            }

            FGTimeTheme(darkTheme = uiState.isDarkMode) {
                // If user is not logged in or explicitly on Welcome screen, show WelcomeAuthScreen
                if (!uiState.isLoggedIn || uiState.currentScreen == Screen.WELCOME_AUTH) {
                    WelcomeAuthScreen(viewModel = viewModel)
                } else {
                    // Back handler: return to Feed if on another screen
                    BackHandler(enabled = uiState.currentScreen != Screen.FEED) {
                        viewModel.navigateTo(Screen.FEED)
                    }

                    val showTopBar = uiState.currentScreen !in listOf(
                        Screen.STORY_VIEWER,
                        Screen.REELS,
                        Screen.DIRECT_MESSAGES,
                        Screen.CALL_SCREEN,
                        Screen.AI_STUDIO,
                        Screen.ADMIN_DASHBOARD
                    )

                    val showBottomBar = uiState.currentScreen !in listOf(
                        Screen.STORY_VIEWER,
                        Screen.DIRECT_MESSAGES,
                        Screen.CALL_SCREEN,
                        Screen.AI_STUDIO,
                        Screen.ADMIN_DASHBOARD
                    )

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            if (showTopBar) {
                                FGTimeTopBar(
                                    viewModel = viewModel,
                                    onOpenSettings = { showSettingsDialog = true }
                                )
                            }
                        },
                        bottomBar = {
                            if (showBottomBar) {
                                FGTimeBottomBar(
                                    currentScreen = uiState.currentScreen,
                                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                                )
                            }
                        },
                        snackbarHost = {
                            SnackbarHost(hostState = snackbarHostState)
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (uiState.currentScreen) {
                                Screen.FEED -> FeedScreen(viewModel = viewModel)
                                Screen.EXPLORE -> ExploreScreen(viewModel = viewModel)
                                Screen.CREATE_POST -> CreatePostScreen(viewModel = viewModel)
                                Screen.REELS -> ReelsScreen(viewModel = viewModel)
                                Screen.REMIX_STUDIO -> RemixStudioScreen(viewModel = viewModel)
                                Screen.NOTIFICATIONS -> NotificationsScreen(viewModel = viewModel)
                                Screen.PROFILE -> ProfileScreen(viewModel = viewModel)
                                Screen.DIRECT_MESSAGES -> DirectMessagesScreen(viewModel = viewModel)
                                Screen.STORY_VIEWER -> StoryViewerScreen(viewModel = viewModel)
                                Screen.CALL_SCREEN -> CallScreen(viewModel = viewModel)
                                Screen.AI_STUDIO -> AIStudioScreen(viewModel = viewModel)
                                Screen.ADMIN_DASHBOARD -> AdminDashboardScreen(viewModel = viewModel)
                                Screen.WELCOME_AUTH -> WelcomeAuthScreen(viewModel = viewModel)
                            }
                        }
                    }

                    if (showSettingsDialog) {
                        SettingsDialog(
                            viewModel = viewModel,
                            onDismiss = { showSettingsDialog = false }
                        )
                    }
                }
            }
        }
    }
}
