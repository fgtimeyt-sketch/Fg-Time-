package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.model.MusicTrack
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.ui.theme.FGPurpleAccent
import com.example.viewmodel.FGTimeViewModel

@Composable
fun RemixStudioScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    // Available Video Clips to remix
    val videoClips = listOf(
        Pair("Greenhouse Neon", R.drawable.fgtime_reel),
        Pair("Creative Botanical", R.drawable.fgtime_creator),
        Pair("FG Horizon", R.drawable.fgtime_banner)
    )

    var selectedVideoIndex by remember { mutableStateOf(0) }
    var playbackPosition by remember { mutableFloatStateOf(4f) }

    // Waveform animation
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    val currentTrack = uiState.remixTracks.find { it.id == uiState.selectedRemixTrackId }
        ?: uiState.remixTracks.firstOrNull()

    val clipDuration = (uiState.studioTrimEndSec - uiState.studioTrimStartSec).coerceAtLeast(1f)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 80.dp)
            .testTag("remix_studio_screen")
    ) {
        // Studio Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.linearGradient(listOf(FGGreenPrimary, FGPurpleAccent))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = Color.Black)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "FG REMIX STUDIO",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Trim, sync music, and create viral reels 🎬",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. VIDEO PREVIEW PLAYER CANVAS
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = videoClips[selectedVideoIndex].second),
                    contentDescription = "Selected Video Preview",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Dark gradient overlay for controls
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                            )
                        )
                )

                // Play / Pause central button
                IconButton(
                    onClick = { viewModel.toggleStudioPlayback() },
                    modifier = Modifier
                        .size(60.dp)
                        .align(Alignment.Center)
                        .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                        .border(1.5.dp, FGGreenPrimary, CircleShape)
                        .testTag("btn_studio_play_pause")
                ) {
                    Icon(
                        imageVector = if (uiState.isStudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = FGGreenPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // Bottom player indicator & music wave badge
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (uiState.isStudioPlaying) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.Bottom,
                                modifier = Modifier.height(16.dp)
                            ) {
                                repeat(5) { i ->
                                    val factor = if (i % 2 == 0) waveScale else (1.4f - waveScale)
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .fillMaxHeight(factor.coerceIn(0.2f, 1f))
                                            .background(FGGreenPrimary, RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = "♪ ${currentTrack?.title ?: "Select Soundtrack"}",
                            color = Color.White,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Time: ${playbackPosition.toInt()}s / ${clipDuration.toInt()}s",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. VIDEO CLIP SELECTION CAROUSEL
        Text(
            text = "Select Video Source",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(videoClips.indices.toList()) { idx ->
                val isSelected = selectedVideoIndex == idx
                Card(
                    modifier = Modifier
                        .width(110.dp)
                        .height(75.dp)
                        .clickable { selectedVideoIndex = idx }
                        .testTag("btn_select_video_$idx"),
                    shape = RoundedCornerShape(12.dp),
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(FGGreenPrimary, FGGreenSecondary))
                    ) else null
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = videoClips[idx].second),
                            contentDescription = videoClips[idx].first,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = if (isSelected) 0.1f else 0.45f))
                        )
                        Text(
                            text = videoClips[idx].first,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. TRIM TIMELINE CONTROLS
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trim Range Controls",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Badge(
                        containerColor = FGGreenPrimary.copy(alpha = 0.2f),
                        contentColor = FGGreenPrimary
                    ) {
                        Text("${clipDuration.toInt()} sec clip")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Start Trim Slider
                Text(
                    text = "Start Offset: ${uiState.studioTrimStartSec.toInt()}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = uiState.studioTrimStartSec,
                    onValueChange = { newStart ->
                        if (newStart < uiState.studioTrimEndSec - 2f) {
                            viewModel.updateStudioTrim(newStart, uiState.studioTrimEndSec)
                        }
                    },
                    valueRange = 0f..20f,
                    colors = SliderDefaults.colors(
                        thumbColor = FGGreenPrimary,
                        activeTrackColor = FGGreenPrimary
                    ),
                    modifier = Modifier.testTag("slider_trim_start")
                )

                // End Trim Slider
                Text(
                    text = "End Offset: ${uiState.studioTrimEndSec.toInt()}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = uiState.studioTrimEndSec,
                    onValueChange = { newEnd ->
                        if (newEnd > uiState.studioTrimStartSec + 2f) {
                            viewModel.updateStudioTrim(uiState.studioTrimStartSec, newEnd)
                        }
                    },
                    valueRange = 5f..30f,
                    colors = SliderDefaults.colors(
                        thumbColor = FGPurpleAccent,
                        activeTrackColor = FGPurpleAccent
                    ),
                    modifier = Modifier.testTag("slider_trim_end")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. MUSIC SOUNDTRACK SELECTION
        Text(
            text = "Select Background Music",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            uiState.remixTracks.forEach { track ->
                val isSelected = track.id == uiState.selectedRemixTrackId
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.selectRemixTrack(track.id) }
                        .testTag("track_item_${track.id}"),
                    color = if (isSelected) FGGreenPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                    border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(FGGreenPrimary, FGGreenSecondary))
                    ) else null,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.selectRemixTrack(track.id)
                                viewModel.toggleStudioPlayback()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(if (isSelected) FGGreenPrimary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = if (isSelected && uiState.isStudioPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = "Play Track",
                                tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${track.artist} • ${track.genre} • ${track.bpm} BPM",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${track.durationSeconds}s",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = FGGreenPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. VOLUME MIXER CONTROL
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (uiState.studioVolume == 0f) Icons.Default.VolumeMute else Icons.Default.VolumeUp,
                    contentDescription = "Volume",
                    tint = FGGreenPrimary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Master Volume: ${(uiState.studioVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )
                    Slider(
                        value = uiState.studioVolume,
                        onValueChange = { viewModel.setStudioVolume(it) },
                        colors = SliderDefaults.colors(
                            thumbColor = FGGreenPrimary,
                            activeTrackColor = FGGreenPrimary
                        ),
                        modifier = Modifier.testTag("slider_studio_volume")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 6. EXPORT REMIX ACTION BUTTON
        Button(
            onClick = { viewModel.exportRemix() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("btn_export_remix"),
            colors = ButtonDefaults.buttonColors(
                containerColor = FGGreenPrimary,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.MovieFilter, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Export Remix Video",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }

    // Export Progress & Completion Dialog
    if (uiState.studioExportProgress != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearStudioExportModal() },
            title = {
                Text(
                    text = if (uiState.studioExportProgress == 100) "Remix Ready! 🎬" else "Rendering Remix...",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.studioExportProgress!! < 100) {
                        CircularProgressIndicator(
                            color = FGGreenPrimary,
                            strokeWidth = 4.dp,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Encoding video clip & syncing ${currentTrack?.title}...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uiState.studioExportProgress!! / 100f },
                            color = FGGreenPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = FGGreenPrimary,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = uiState.studioExportSuccessMessage ?: "Video generated successfully!",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                if (uiState.studioExportProgress == 100) {
                    Button(
                        onClick = { viewModel.clearStudioExportModal() },
                        colors = ButtonDefaults.buttonColors(containerColor = FGGreenPrimary)
                    ) {
                        Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}
