package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.ui.theme.FGPurpleAccent
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Screen

@Composable
fun ProfileScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.currentUser
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditDialog by remember { mutableStateOf(false) }

    var editedName by remember { mutableStateOf(user.name) }
    var editedBio by remember { mutableStateOf(user.bio) }

    val myPosts = uiState.posts.filter { it.author.id == user.id || it.author.username == user.username }
    val savedPosts = uiState.posts.filter { it.isSaved }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 80.dp)
            .testTag("profile_screen")
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // Profile Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Avatar & Stats Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar with glowing ring
                        Box(
                            modifier = Modifier
                                .size(84.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(
                                        if (user.isOwner)
                                            listOf(Color(0xFFFFD700), FGGreenPrimary, Color(0xFFFFD700))
                                        else
                                            listOf(FGGreenPrimary, FGGreenSecondary, Color.Transparent, FGGreenPrimary)
                                    )
                                )
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = user.avatarResId),
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        // Stats
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            ProfileStat(title = "Posts", value = "${user.postsCount}")
                            ProfileStat(title = "Followers", value = "${user.followers}")
                            ProfileStat(title = "Following", value = "${user.following}")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Name, Verified Emblem & Bio
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        if (user.isOwner) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = FGGreenPrimary
                            ) {
                                Text(
                                    text = "CREATOR & OWNER",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Verified Leaf",
                                tint = FGGreenPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.labelMedium,
                        color = FGGreenSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons (Edit Profile, Share Profile)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                editedName = user.name
                                editedBio = user.bio
                                showEditDialog = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_edit_profile"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Edit Profile", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { /* Share */ },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FGGreenPrimary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Share Profile", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Admin Dashboard & AI studio shortcut buttons
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(Screen.AI_STUDIO) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FGPurpleAccent.copy(alpha = 0.2f),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Creator Studio", style = MaterialTheme.typography.labelMedium)
                        }

                        Button(
                            onClick = { viewModel.navigateTo(Screen.ADMIN_DASHBOARD) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FGGreenPrimary.copy(alpha = 0.2f),
                                contentColor = FGGreenPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Admin Portal", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Tabs Row (Grid, Saved, Reels)
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = FGGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        icon = { Icon(Icons.Default.GridOn, contentDescription = "Posts") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        icon = { Icon(Icons.Default.BookmarkBorder, contentDescription = "Saved") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Default.PlayCircleOutline, contentDescription = "Reels") }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tab Content
            item {
                when (selectedTab) {
                    0 -> {
                        // My Posts Grid
                        val postsToShow = if (myPosts.isNotEmpty()) myPosts else uiState.posts
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            postsToShow.chunked(3).forEach { rowPosts ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowPosts.forEach { post ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(115.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                        ) {
                                            Image(
                                                painter = painterResource(id = post.imageResId),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        // Saved Bookmarks
                        if (savedPosts.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No saved posts yet. Tap the bookmark icon on posts!",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                savedPosts.chunked(3).forEach { rowPosts ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        rowPosts.forEach { post ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(115.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                            ) {
                                                Image(
                                                    painter = painterResource(id = post.imageResId),
                                                    contentDescription = null,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    2 -> {
                        // Reels Tab
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            uiState.reels.chunked(2).forEach { rowReels ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    rowReels.forEach { r ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        ) {
                                            Image(
                                                painter = painterResource(id = r.coverResId),
                                                contentDescription = null,
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                                        )
                                                    )
                                            )
                                            Text(
                                                text = "${r.likesCount} plays",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .padding(6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editedBio,
                        onValueChange = { editedBio = it },
                        label = { Text("Bio") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.performLogin(editedName, user.username, user.contact, user.dateOfBirth)
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary)
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ProfileStat(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
