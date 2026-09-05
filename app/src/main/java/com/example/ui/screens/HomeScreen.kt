package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoItem
import com.example.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun HomeScreen(
    videos: List<VideoItem>,
    onWatchVideo: (VideoItem) -> Unit,
    onNavigateToShorts: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Technology", "Design", "Gaming", "News", "Tutorials")

    val standardVideos = remember(videos, selectedCategory) {
        val filtered = videos.filter { !it.isShort }
        if (selectedCategory == "All") filtered else filtered.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    val shorts = remember(videos) {
        videos.filter { it.isShort }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ZincBg)
            .testTag("home_screen_feed"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Category Chips (Sleek Interface: White active / Zinc-800 inactive)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    Surface(
                        color = if (isSelected) Color.White else ZincElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (isSelected) Color.Black else Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Shorts Shelf (shown when on "All" category)
        if (selectedCategory == "All" && shorts.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(vertical = 10.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Shorts",
                                tint = SleekRedAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "Shorts",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZincTextPrimary
                            )
                        }
                        TextButton(onClick = onNavigateToShorts) {
                            Text("View All", color = SleekRedAccent, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(shorts) { short ->
                            ShortPreviewCard(short = short, onClick = { onWatchVideo(short) })
                        }
                    }
                }
            }
        }

        // Standard Video Feed (Sleek Interface cards)
        items(standardVideos) { video ->
            VideoCard(video = video, onClick = { onWatchVideo(video) })
        }
    }
}

@Composable
fun VideoCard(video: VideoItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("video_card_${video.id}")
    ) {
        // Thumbnail: rounded-2xl bg-zinc-800 overflow-hidden with gradient overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(video.gradientColor1), Color(video.gradientColor2))
                    )
                )
        ) {
            // Subtle gradient darkening towards bottom
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )

            // Play watermark
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.35f),
                modifier = Modifier
                    .size(52.dp)
                    .align(Alignment.Center)
            )

            // Featured / Category pill on top-left: trending_up + label
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = SleekRedAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = if (video.category.isNotBlank()) video.category.uppercase() else "FEATURED",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }

            // Duration Pill bottom-right: bg-black/80 px-1.5 py-0.5 rounded text-[10px] font-bold
            Surface(
                color = Color.Black.copy(alpha = 0.8f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = video.duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        // Details Row: Avatar + Title/Channel + MoreVert
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Channel Avatar (40dp with border-zinc-600)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(video.gradientColor1))
                    .border(1.dp, ZincBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = video.uploaderName.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            // Text Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = ZincTextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val formattedViews = formatViews(video.views)
                Text(
                    text = "${video.uploaderName} • $formattedViews views • ${video.timeAgo}",
                    color = ZincTextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // More Options icon
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = ZincTextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ShortPreviewCard(short: VideoItem, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(140.dp)
            .height(230.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(short.gradientColor1), Color(short.gradientColor2))
                )
            )
            .clickable(onClick = onClick)
            .testTag("short_preview_${short.id}")
            .padding(10.dp)
    ) {
        Surface(
            color = SleekRed,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Text(
                text = "SHORTS",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }

        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = short.title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${formatViews(short.views)} views",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 11.sp
            )
        }
    }
}

fun formatViews(views: Long): String {
    return when {
        views >= 1_000_000 -> String.format(Locale.US, "%.1fM", views / 1_000_000.0)
        views >= 1_000 -> String.format(Locale.US, "%.1fK", views / 1_000.0)
        else -> NumberFormat.getNumberInstance(Locale.US).format(views)
    }
}

