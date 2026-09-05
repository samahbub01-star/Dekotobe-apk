package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.CommentItem
import com.example.model.VideoItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun WatchDialog(
    video: VideoItem,
    comments: List<CommentItem>,
    onDismiss: () -> Unit,
    onLike: (String) -> Unit,
    onAddComment: (String, String) -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var currentProgress by remember { mutableFloatStateOf(0.15f) }
    var hasLiked by remember { mutableStateOf(false) }
    var likeCount by remember { mutableLongStateOf(video.likes) }
    var newCommentText by remember { mutableStateOf("") }
    var isSubscribed by remember { mutableStateOf(false) }
    var showDescriptionFull by remember { mutableStateOf(false) }

    // Simulated playback loop
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            currentProgress = (currentProgress + 0.015f) % 1f
        }
    }

    val estVideoRev = remember(video.views) {
        val rpm = if (video.isShort) 0.12 else 3.50
        (video.views / 1000.0 * rpm * 0.55)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = ZincBg,
            modifier = Modifier
                .fillMaxSize()
                .testTag("watch_video_dialog")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Player Area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(video.gradientColor1), Color(video.gradientColor2))
                            )
                        )
                ) {
                    // Close button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    // Play/Pause center overlay
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(56.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Bottom scrub bar
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(Color.Black.copy(alpha = 0.75f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Slider(
                            value = currentProgress,
                            onValueChange = { currentProgress = it },
                            colors = SliderDefaults.colors(
                                thumbColor = SleekRed,
                                activeTrackColor = SleekRed,
                                inactiveTrackColor = ZincBorder
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isPlaying) "02:18 / ${video.duration}" else "Paused",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                            Text(
                                text = "1080p 60fps HD",
                                fontSize = 11.sp,
                                color = ZincTextSecondary
                            )
                        }
                    }
                }

                // Scrollable details and comments
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    contentPadding = PaddingValues(bottom = 30.dp)
                ) {
                    // Title
                    item {
                        Text(
                            text = video.title,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZincTextPrimary,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatViews(video.views)} views • ${video.timeAgo} • #${video.category.lowercase()}",
                            fontSize = 12.sp,
                            color = ZincTextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Channel Bar
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(video.gradientColor1))
                                        .border(1.dp, ZincBorderSubtle, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = video.uploaderName.take(1).uppercase(),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column {
                                    Text(
                                        text = video.uploaderName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ZincTextPrimary
                                    )
                                    Text(
                                        text = "124K subscribers",
                                        fontSize = 11.sp,
                                        color = ZincTextSecondary
                                    )
                                }
                            }

                            Button(
                                onClick = { isSubscribed = !isSubscribed },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubscribed) ZincElevated else Color.White
                                ),
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (isSubscribed) "Subscribed" else "Subscribe",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSubscribed) ZincTextPrimary else Color.Black
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Action buttons row
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Like
                            Surface(
                                color = ZincSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
                                modifier = Modifier.clickable {
                                    if (!hasLiked) {
                                        hasLiked = true
                                        likeCount += 1
                                        onLike(video.id)
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (hasLiked) Icons.Default.Favorite else Icons.Default.ThumbUp,
                                        contentDescription = "Like",
                                        tint = if (hasLiked) SleekRedAccent else Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = formatViews(likeCount),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Share
                            Surface(
                                color = ZincSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    Text("Share", fontSize = 12.sp, color = Color.White)
                                }
                            }

                            // Estimated Video Revenue badge
                            Surface(
                                color = ZincSurface,
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("💰", fontSize = 12.sp)
                                    Text(
                                        text = "$${String.format(java.util.Locale.US, "%,.2f", estVideoRev)} Est.",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekGreen
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Description Box
                    item {
                        Surface(
                            color = ZincSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showDescriptionFull = !showDescriptionFull }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = video.description,
                                    fontSize = 13.sp,
                                    color = ZincTextPrimary,
                                    maxLines = if (showDescriptionFull) 20 else 2,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (showDescriptionFull) "Show less" else "...more",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ZincTextSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    // Comments Header
                    item {
                        Text(
                            text = "Comments (${comments.size})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZincTextPrimary,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        // Add comment input
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCommentText,
                                onValueChange = { newCommentText = it },
                                placeholder = { Text("Add a comment on Dekotube...", fontSize = 13.sp, color = ZincTextSecondary) },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = SleekRedAccent,
                                    unfocusedBorderColor = ZincBorder,
                                    focusedContainerColor = ZincElevated,
                                    unfocusedContainerColor = ZincElevated,
                                    focusedTextColor = ZincTextPrimary,
                                    unfocusedTextColor = ZincTextPrimary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            Button(
                                onClick = {
                                    if (newCommentText.isNotBlank()) {
                                        onAddComment(video.id, newCommentText.trim())
                                        newCommentText = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = SleekRed),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text("Post", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Comments list
                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(ZincElevated)
                                    .border(1.dp, ZincBorderSubtle, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.author.take(1).uppercase(),
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.author,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ZincTextPrimary
                                    )
                                    Text(
                                        text = comment.timeAgo,
                                        fontSize = 11.sp,
                                        color = ZincTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.text,
                                    fontSize = 13.sp,
                                    color = ZincTextMuted,
                                    lineHeight = 17.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

