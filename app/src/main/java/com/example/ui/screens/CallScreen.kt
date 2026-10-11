package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.model.CallState
import com.example.model.CallType
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.viewmodel.FGTimeViewModel

@Composable
fun CallScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val call = uiState.activeCall ?: return

    val durationMin = uiState.callDurationSeconds / 60
    val durationSec = uiState.callDurationSeconds % 60
    val durationFormatted = "%02d:%02d".format(durationMin, durationSec)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09140E))
            .testTag("call_screen")
    ) {
        // Video background (if video call and camera enabled)
        if (call.type == CallType.VIDEO && uiState.isCallCameraEnabled) {
            Image(
                painter = painterResource(id = R.drawable.fgtime_banner),
                contentDescription = "Video Feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Soft dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            )
        }

        // Top Call Status Bar
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (call.type == CallType.VIDEO) Icons.Default.Videocam else Icons.Default.Call,
                        contentDescription = null,
                        tint = FGGreenPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (call.type == CallType.VIDEO) "FG TIME HD Video Call" else "FG TIME Voice Call",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = call.contactName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            val statusText = when (uiState.callState) {
                CallState.OUTGOING -> "Ringing..."
                CallState.INCOMING -> "Incoming call..."
                CallState.CONNECTED -> durationFormatted
                CallState.ENDED -> "Call ended"
                CallState.IDLE -> ""
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = FGGreenSecondary,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Central Avatar (for Audio calls or camera off)
        if (call.type == CallType.AUDIO || !uiState.isCallCameraEnabled) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.sweepGradient(
                                listOf(FGGreenPrimary, FGGreenSecondary, Color.Transparent, FGGreenPrimary)
                            )
                        )
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = call.contactAvatarResId),
                        contentDescription = call.contactName,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "End-to-End Encrypted Real-Time Session",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Self View PiP Window for Video Calls
        if (call.type == CallType.VIDEO && uiState.callState == CallState.CONNECTED) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 80.dp, end = 16.dp)
                    .size(width = 90.dp, height = 130.dp),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(FGGreenPrimary, FGGreenSecondary)))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.fgtime_creator),
                    contentDescription = "My Camera Feed",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Bottom Controls Bar
        Surface(
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.Black.copy(alpha = 0.85f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.callState == CallState.INCOMING) {
                    // Incoming Call Accept / Decline Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        IconButton(
                            onClick = { viewModel.endCall() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFE53935), CircleShape)
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White)
                        }

                        IconButton(
                            onClick = { viewModel.acceptIncomingCall() },
                            modifier = Modifier
                                .size(64.dp)
                                .background(FGGreenPrimary, CircleShape)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "Accept", tint = Color.Black)
                        }
                    }
                } else {
                    // Active or Outgoing Call Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Mic
                        IconButton(
                            onClick = { viewModel.toggleCallMute() },
                            modifier = Modifier
                                .size(52.dp)
                                .background(
                                    if (uiState.isCallMuted) Color.White else Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = if (uiState.isCallMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = "Mute",
                                tint = if (uiState.isCallMuted) Color.Black else Color.White
                            )
                        }

                        // Video Toggle (if Video Call)
                        if (call.type == CallType.VIDEO) {
                            IconButton(
                                onClick = { viewModel.toggleCallCamera() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(
                                        if (!uiState.isCallCameraEnabled) Color.White else Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    )
                            ) {
                                Icon(
                                    imageVector = if (uiState.isCallCameraEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                                    contentDescription = "Camera Toggle",
                                    tint = if (!uiState.isCallCameraEnabled) Color.Black else Color.White
                                )
                            }

                            // Flip Camera
                            IconButton(
                                onClick = { viewModel.flipCallCamera() },
                                modifier = Modifier
                                    .size(52.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FlipCameraAndroid,
                                    contentDescription = "Flip Camera",
                                    tint = Color.White
                                )
                            }
                        }

                        // End Call
                        IconButton(
                            onClick = { viewModel.endCall() },
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFE53935), CircleShape)
                                .testTag("btn_end_call")
                        ) {
                            Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}
