package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: DMs, 1: Groups, 2: Calls
    var inputText by remember { mutableStateOf("") }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceDuration by remember { mutableIntStateOf(0) }

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
                    Box(modifier = Modifier.size(40.dp)) {
                        Image(
                            painter = painterResource(id = R.drawable.fgtime_creator),
                            contentDescription = "Contact Avatar",
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(FGGreenPrimary)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Maya Green 🌿",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Online • Active on FG TIME",
                            style = MaterialTheme.typography.labelSmall,
                            color = FGGreenPrimary
                        )
                    }

                    // Start Audio Call button
                    IconButton(
                        onClick = {
                            viewModel.startCall("Maya Green", R.drawable.fgtime_creator, CallType.AUDIO)
                        },
                        modifier = Modifier.testTag("btn_audio_call")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = FGGreenPrimary)
                    }

                    // Start Video Call button
                    IconButton(
                        onClick = {
                            viewModel.startCall("Maya Green", R.drawable.fgtime_creator, CallType.VIDEO)
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
                        text = { Text("Call Logs") }
                    )
                }
            }
        }

        when (selectedTab) {
            0 -> {
                // One-on-One Messages List
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
                                    painter = painterResource(id = R.drawable.fgtime_creator),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .align(Alignment.Bottom),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = if (isMe) 16.dp else 4.dp,
                                        bottomEnd = if (isMe) 4.dp else 16.dp
                                    ),
                                    color = if (isMe) FGGreenPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    tonalElevation = 2.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (msg.isVoiceNote) {
                                            Icon(
                                                Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = if (isMe) Color.Black else FGGreenPrimary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = msg.text,
                                            color = if (isMe) Color.Black else MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                                Text(
                                    text = msg.timestamp,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // Voice Recording Live Bar (when recording)
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
                                    text = "Recording: ${voiceDuration}s",
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row {
                                TextButton(onClick = { isRecordingVoice = false }) {
                                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Button(
                                    onClick = {
                                        viewModel.sendChatMessage("", isVoice = true, durationSec = voiceDuration)
                                        isRecordingVoice = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary)
                                ) {
                                    Text("Send Voice", color = Color.Black)
                                }
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
                            placeholder = { Text("Send a message...") },
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
                            modifier = Modifier
                                .size(44.dp)
                                .background(FGGreenPrimary, CircleShape)
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(uiState.groupChats, key = { it.id }) { group ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTab = 0 }
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
                                        text = "${group.memberCount} members • Admin: @muksidul",
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
                            }
                        }
                    }
                }
            }

            2 -> {
                // Call History Logs Tab
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.callHistory, key = { it.id }) { call ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(
                                    painter = painterResource(id = call.contactAvatarResId),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(42.dp)
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                                            contentDescription = null,
                                            tint = if (call.wasMissed) Color(0xFFD32F2F) else FGGreenPrimary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${call.timestamp} • ${if (call.wasMissed) "Missed" else "${call.durationSec}s"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.startCall(call.contactName, call.contactAvatarResId, call.type)
                                }) {
                                    Icon(Icons.Default.Call, contentDescription = "Call Back", tint = FGGreenPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
