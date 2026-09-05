package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.VideoItem
import com.example.ui.theme.*

@Composable
fun ShortsScreen(
    shorts: List<VideoItem>,
    onLikeShort: (String) -> Unit,
    onOpenComments: (VideoItem) -> Unit,
    onRecordView: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (shorts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(ZincBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "No shorts uploaded yet.", color = ZincTextSecondary)
        }
        return
    }

    val pagerState = rememberPagerState(pageCount = { shorts.size })

    // Auto record view on page change
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage < shorts.size) {
            onRecordView(shorts[pagerState.currentPage].id)
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = modifier
            .fillMaxSize()
            .background(ZincBg)
            .testTag("shorts_vertical_pager")
    ) { page ->
        val currentShort = shorts[page]
        ShortsPageItem(
            short = currentShort,
            onLike = { onLikeShort(currentShort.id) },
            onComments = { onOpenComments(currentShort) }
        )
    }
}

@Composable
fun ShortsPageItem(
    short: VideoItem,
    onLike: () -> Unit,
    onComments: () -> Unit
) {
    var isLiked by remember { mutableStateOf(false) }
    var likesCount by remember { mutableStateOf(short.likes) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(short.gradientColor1),
                        Color(short.gradientColor2),
                        ZincBg
                    )
                )
            )
            .padding(bottom = 64.dp) // Nav bar clearance
    ) {
        // Center animation / ambient play badge
        Surface(
            color = Color.Black.copy(alpha = 0.35f),
            shape = CircleShape,
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Playing",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
            )
        }

        // Top tag
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 16.dp, start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = SleekRed,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "DEKOTUBE SHORTS",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Surface(
                color = Color.Black.copy(alpha = 0.5f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "${formatViews(short.views)} views",
                    color = Color.White,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Right side action rail
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Like button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable {
                    if (!isLiked) {
                        isLiked = true
                        likesCount += 1
                        onLike()
                    }
                }
            ) {
                IconButton(
                    onClick = {
                        if (!isLiked) {
                            isLiked = true
                            likesCount += 1
                            onLike()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .testTag("shorts_like_button")
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = if (isLiked) SleekRedAccent else Color.White
                    )
                }
                Text(
                    text = formatViews(likesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Comments button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable(onClick = onComments)
            ) {
                IconButton(
                    onClick = onComments,
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .testTag("shorts_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comments",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Comments",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Share button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = { /* Share */ },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Share",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }

            // Sound disk
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ZincElevated)
                    .border(1.dp, ZincBorderSubtle, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = "Sound",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom channel info & title
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, end = 80.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(short.gradientColor1))
                        .border(1.dp, ZincBorderSubtle, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = short.uploaderName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "@${short.uploaderName.replace(" ", "")}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.clickable { }
                ) {
                    Text(
                        text = "Subscribe",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                    )
                }
            }

            Text(
                text = short.title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 18.sp
            )

            // Audio track pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Audiotrack,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Original Sound - ${short.uploaderName}",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

