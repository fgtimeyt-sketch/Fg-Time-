package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.CallType
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Screen
import kotlinx.coroutines.delay

@Composable
fun DirectMessagesScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Direct Chat, 1: Groups, 2: Call History
    var inputText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceDuration by remember { mutableIntStateOf(0) }

    // Create Group Dialog state
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var newGroupName by remember { mutableStateOf("") }
    var newGroupMemberCount by remember { mutableIntStateOf(3) }

    // Available contacts to start/switch direct chats
    val directContacts = listOf(
        Triple("Maya Green 🌿", R.drawable.fgtime_creator, "u_maya"),
        Triple("Rohan Verma 🎧", R.drawable.fgtime_leaf_logo, "u_rohan"),
        Triple("Muksidul (Founder) 👑", R.drawable.fgtime_leaf_logo, "u_owner_muksidul"),
        Triple("Priya Patel 🌱", R.drawable.fgtime_banner, "u3")
    )

    // Voice record timer
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceDuration = 0
            while (isRecordingVoice) {
                delay(1000)
                voiceDuration++
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("direct_messages_screen")
    ) {
        // Chat Header
        Surface(
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.navigateTo(Screen.FEED) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                    Box(modifier = Modifier.size(42.dp)) {
                        Image(
                            painter = painterResource(id = uiState.activeChatAvatar),
                            contentDescription = "Contact Avatar",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        if (uiState.activeChatIsOnline) {
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(FGGreenPrimary)
                                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .align(Alignment.BottomEnd)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.activeChatTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (uiState.activeChatIsGroup)
                                "Group Chat • Persistent Database"
                            else if (uiState.activeChatIsOnline)
                                "Online • Active on FG TIME 🌿"
                            else
                                "Offline • Seen recently",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.activeChatIsOnline) FGGreenPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Audio Call button
                    IconButton(
                        onClick = {
                            viewModel.startCall(uiState.activeChatTitle, uiState.activeChatAvatar, CallType.AUDIO)
                        },
                        modifier = Modifier.testTag("btn_audio_call")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = FGGreenPrimary)
                    }

                    // Video Call button
                    IconButton(
                        onClick = {
                            viewModel.startCall(uiState.activeChatTitle, uiState.activeChatAvatar, CallType.VIDEO)
                        },
                        modifier = Modifier.testTag("btn_video_call")
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = FGGreenPrimary)
                    }
                }

                // Chat sub-tabs (Messages, Groups, Call Log)
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = FGGreenPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Direct Chat") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Groups (${uiState.groupChats.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Call Logs (${uiState.callHistory.size})") }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // Quick Contact Switcher Row
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(directContacts) { (name, avatar, id) ->
                            val isCurrent = uiState.activeChatRecipientId == id && !uiState.activeChatIsGroup
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCurrent) FGGreenPrimary else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    viewModel.selectChat(
                                        chatId = "dm_$id",
                                        title = name,
                                        avatarRes = avatar,
                                        isOnline = true,
                                        recipientId = id,
                                        isGroup = false
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = avatar),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = name.split(" ").first(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCurrent) Color.Black else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Messages List (Direct or Selected Group)
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.chatMessages, key = { it.id }) { msg ->
                        val isMe = msg.isMe
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isMe) {
                                Image(
                                    painter = painterResource(id = uiState.activeChatAvatar),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.Bottom),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) FGGreenPrimary else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    if (uiState.activeChatIsGroup && !isMe && msg.senderName.isNotBlank()) {
                                        Text(
                                            text = msg.senderName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FGGreenSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }

                                    if (msg.isVoiceNote) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.PlayCircle,
                                                contentDescription = "Play",
                                                tint = if (isMe) Color.Black else FGGreenPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Voice message (${msg.voiceDurationSec}s)",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isMe) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = msg.text,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = if (isMe) Color.Black else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = msg.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isMe) Color.Black.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = "Delivered",
                                                tint = Color.Black.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Voice Recording Status Banner
                AnimatedVisibility(visible = isRecordingVoice) {
                    Surface(
                        color = Color(0xFFD32F2F).copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Mic, contentDescription = null, tint = Color(0xFFD32F2F))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Recording Voice Note... ${voiceDuration}s",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                            Button(
                                onClick = {
                                    viewModel.sendChatMessage(
                                        text = "Voice message (${voiceDuration}s)",
                                        isVoiceNote = true,
                                        voiceDuration = voiceDuration
                                    )
                                    isRecordingVoice = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary)
                            ) {
                                Text("Send", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Chat Input Row
                Surface(
                    tonalElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = { Text("Message ${uiState.activeChatTitle}...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_chat_message"),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Voice message button
                        IconButton(
                            onClick = { isRecordingVoice = !isRecordingVoice },
                            modifier = Modifier.testTag("btn_record_voice")
                        ) {
                            Icon(
                                imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Message",
                                tint = if (isRecordingVoice) Color(0xFFD32F2F) else FGGreenPrimary
                            )
                        }

                        // Send button
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendChatMessage(inputText)
                                    inputText = ""
                                }
                            },
                            enabled = inputText.isNotBlank(),
                            modifier = Modifier
                                .size(44.dp)
                                .background(if (inputText.isNotBlank()) FGGreenPrimary else Color.Gray.copy(alpha = 0.3f), CircleShape)
                                .testTag("btn_send_chat")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }

            1 -> {
                // Group Chats Tab
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp)
                ) {
                    // Create New Group Action Button
                    Button(
                        onClick = { showCreateGroupDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FGGreenPrimary,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.GroupAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create New Group 👥+", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.groupChats, key = { it.id }) { group ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectChat(
                                            chatId = group.id,
                                            title = group.name,
                                            avatarRes = group.iconResId,
                                            isOnline = true,
                                            recipientId = group.id,
                                            isGroup = true
                                        )
                                        selectedTab = 0
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = group.iconResId),
                                        contentDescription = group.name,
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = group.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${group.memberCount} members • Admin: @${group.adminUserId}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FGGreenSecondary
                                        )
                                        Text(
                                            text = group.lastMessage,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FGGreenPrimary)
                                }
                            }
                        }
                    }
                }
            }

            2 -> {
                // Call History Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (uiState.callHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PhoneMissed, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Text("No call logs yet. Start an audio or video call with friends!", color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        items(uiState.callHistory, key = { it.id }) { call ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = call.contactAvatarResId),
                                        contentDescription = call.contactName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = call.contactName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${if (call.type == CallType.VIDEO) "Video" else "Audio"} Call • Duration: ${call.durationSec}s",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = FGGreenPrimary
                                        )
                                    }
                                    Text(
                                        text = call.timestamp,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // CREATE GROUP DIALOG
    if (showCreateGroupDialog) {
        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = {
                Text("Create New Group 👥", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Connect multiple creators in an authentic green conversation space.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newGroupName,
                        onValueChange = { newGroupName = it },
                        label = { Text("Group Name") },
                        placeholder = { Text("e.g. Eco Filmmakers Hub 🌿") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Initial Members: $newGroupMemberCount",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = newGroupMemberCount.toFloat(),
                        onValueChange = { newGroupMemberCount = it.toInt() },
                        valueRange = 2f..20f,
                        steps = 17,
                        colors = SliderDefaults.colors(
                            thumbColor = FGGreenPrimary,
                            activeTrackColor = FGGreenPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newGroupName.isNotBlank()) {
                            viewModel.createGroupChat(newGroupName, newGroupMemberCount)
                            newGroupName = ""
                            showCreateGroupDialog = false
                            selectedTab = 0
                        }
                    },
                    enabled = newGroupName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary, contentColor = Color.Black)
                ) {
                    Text("Create Group", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
