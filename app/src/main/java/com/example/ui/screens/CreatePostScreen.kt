package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
fun CreatePostScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var mediaType by remember { mutableStateOf("PHOTO") } // PHOTO or VIDEO
    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("FG Creative Loft, Green City") }
    var selectedMediaIndex by remember { mutableIntStateOf(0) }

    val photoMedia = listOf(
        Pair("Botanical Studio", R.drawable.fgtime_creator),
        Pair("FG Leaf Logo Art", R.drawable.fgtime_leaf_logo),
        Pair("FG Time Banner", R.drawable.fgtime_banner)
    )

    val videoMedia = listOf(
        Pair("Greenhouse Neon Reel", R.drawable.fgtime_reel),
        Pair("Botanical Creator Clip", R.drawable.fgtime_creator),
        Pair("FG Emerald Waves", R.drawable.fgtime_banner)
    )

    val currentMediaList = if (mediaType == "PHOTO") photoMedia else videoMedia

    val popularTags = listOf("#FGTime", "#GreenEnergy", "#CreativeFlow", "#SustainableVibes", "#EcoArt", "#FounderNote")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .padding(bottom = 80.dp)
            .testTag("create_post_screen")
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(Screen.FEED) },
                modifier = Modifier.testTag("btn_close_create_post")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel")
            }
            Text(
                text = if (mediaType == "PHOTO") "New Photo Post" else "New Video Post",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        val chosenRes = currentMediaList[selectedMediaIndex.coerceIn(0, currentMediaList.size - 1)].second
                        viewModel.createNewPost(
                            content = caption,
                            tagsStr = popularTags.take(3).joinToString(" "),
                            imageRes = chosenRes,
                            mediaType = mediaType,
                            location = location
                        )
                    }
                },
                enabled = caption.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FGGreenPrimary,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_publish_post")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Post Type Switcher (Photo vs Video)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(4.dp)
        ) {
            Button(
                onClick = {
                    mediaType = "PHOTO"
                    selectedMediaIndex = 0
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mediaType == "PHOTO") FGGreenPrimary else Color.Transparent,
                    contentColor = if (mediaType == "PHOTO") Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Photo Post", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = {
                    mediaType = "VIDEO"
                    selectedMediaIndex = 0
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (mediaType == "VIDEO") FGGreenPrimary else Color.Transparent,
                    contentColor = if (mediaType == "VIDEO") Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Video Post", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Video Remix Studio Shortcut Banner (Feature 4: Video Editing before posting)
        if (mediaType == "VIDEO") {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Brush.horizontalGradient(listOf(FGGreenPrimary, FGPurpleAccent)), RoundedCornerShape(16.dp))
                    .clickable { viewModel.navigateTo(Screen.REMIX_STUDIO) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Brush.linearGradient(listOf(FGGreenPrimary, FGPurpleAccent))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.ContentCut, contentDescription = null, tint = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Trim & Add Music in Remix Studio ✂️🎵",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = FGGreenPrimary
                        )
                        Text(
                            text = "Cut duration, adjust audio mix & select licensed lo-fi stems before sharing.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = FGGreenPrimary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
        }

        // Selected Media Preview Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val previewRes = currentMediaList[selectedMediaIndex.coerceIn(0, currentMediaList.size - 1)].second
                Image(
                    painter = painterResource(id = previewRes),
                    contentDescription = "Selected Media",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Video play overlay if VIDEO
                if (mediaType == "VIDEO") {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = FGGreenPrimary, modifier = Modifier.size(32.dp))
                    }
                }

                // Media Label Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (mediaType == "VIDEO") Icons.Default.Movie else Icons.Default.Image,
                            contentDescription = null,
                            tint = FGGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentMediaList[selectedMediaIndex.coerceIn(0, currentMediaList.size - 1)].first,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Media Selector Carousel
        Text(
            text = if (mediaType == "PHOTO") "Choose Photo Asset" else "Choose Video Clip",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(currentMediaList.indices.toList()) { idx ->
                val isSelected = selectedMediaIndex == idx
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) FGGreenPrimary else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { selectedMediaIndex = idx }
                ) {
                    Image(
                        painter = painterResource(id = currentMediaList[idx].second),
                        contentDescription = currentMediaList[idx].first,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Caption Input
        OutlinedTextField(
            value = caption,
            onValueChange = { caption = it },
            placeholder = { Text("Write an authentic caption... What are you growing or building today on FG TIME? 🌿") },
            modifier = Modifier
                .fillMaxWidth()
                .height(115.dp)
                .testTag("input_post_caption"),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Popular Hashtag Chips
        Text(
            text = "Trending Hashtags",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(popularTags) { tag ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = FGGreenPrimary.copy(alpha = 0.15f),
                    modifier = Modifier.clickable {
                        if (!caption.contains(tag)) {
                            caption = if (caption.isBlank()) tag else "$caption $tag"
                        }
                    }
                ) {
                    Text(
                        text = tag,
                        color = FGGreenPrimary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Location Tag
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location Tag") },
            leadingIcon = {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = FGGreenPrimary)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
