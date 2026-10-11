package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.FGGreenPrimary
import com.example.ui.theme.FGGreenSecondary
import com.example.viewmodel.FGTimeViewModel
import com.example.viewmodel.Screen

@Composable
fun CreatePostScreen(
    viewModel: FGTimeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    var caption by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("FG Creative Loft, Green City") }
    var selectedMediaIndex by remember { mutableStateOf(0) }

    val sampleMedia = listOf(
        Pair("Botanical Studio", R.drawable.fgtime_creator),
        Pair("Greenhouse Vibes", R.drawable.fgtime_reel),
        Pair("FG Leaf Logo Art", R.drawable.fgtime_leaf_logo),
        Pair("FG Time Banner", R.drawable.fgtime_banner)
    )

    val popularTags = listOf("#FGTime", "#GreenEnergy", "#CreativeFlow", "#SustainableVibes", "#EcoArt")

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
                text = "New Post",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    if (caption.isNotBlank()) {
                        viewModel.createNewPost(
                            content = caption,
                            tagsStr = popularTags.take(3).joinToString(" "),
                            imageRes = sampleMedia[selectedMediaIndex].second
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
                Text("Share", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Media Preview Frame
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = sampleMedia[selectedMediaIndex].second),
                    contentDescription = "Selected Media",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Filter badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Black.copy(alpha = 0.6f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterVintage,
                            contentDescription = null,
                            tint = FGGreenPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sampleMedia[selectedMediaIndex].first,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Media Selector
        Text(
            text = "Select Photo / Visual",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(sampleMedia.indices.toList()) { idx ->
                val isSelected = selectedMediaIndex == idx
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) FGGreenPrimary else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedMediaIndex = idx }
                ) {
                    Image(
                        painter = painterResource(id = sampleMedia[idx].second),
                        contentDescription = sampleMedia[idx].first,
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
            placeholder = { Text("Write a caption... What are you growing or building today? 🌿") },
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .testTag("input_post_caption"),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Popular Hashtag Chips
        Text(
            text = "Trending Tags",
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
            label = { Text("Location") },
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
