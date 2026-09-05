package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DekotubeRepository
import com.example.model.VideoItem
import com.example.ui.screens.*
import com.example.ui.theme.*

enum class DekotubeTab {
    HOME, SHORTS, UPLOAD, ADMIN, MONETIZATION
}

class MainActivity : ComponentActivity() {
    private val repository = DekotubeRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DekotubeApp(repository = repository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DekotubeApp(repository: DekotubeRepository) {
    var currentTab by remember { mutableStateOf(DekotubeTab.HOME) }
    var activeWatchVideo by remember { mutableStateOf<VideoItem?>(null) }
    var showAuthDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val currentUser by repository.currentUser.collectAsState()
    val allUsers by repository.users.collectAsState()
    val allVideos by repository.videos.collectAsState()
    val allRules by repository.rules.collectAsState()
    val allComments by repository.comments.collectAsState()

    val filteredVideos = remember(allVideos, searchQuery) {
        if (searchQuery.isBlank()) allVideos
        else allVideos.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.uploaderName.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    val activeComments = remember(activeWatchVideo, allComments) {
        if (activeWatchVideo == null) emptyList()
        else allComments.filter { it.videoId == activeWatchVideo?.id }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(ZincBg),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZincBg)
                    .statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search Dekotube...", fontSize = 13.sp, color = ZincTextSecondary) },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = ZincTextSecondary)
                            },
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchActive = false
                                }) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Search", tint = ZincTextSecondary)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("search_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SleekRedAccent,
                                unfocusedBorderColor = ZincBorder,
                                focusedContainerColor = ZincSurface,
                                unfocusedContainerColor = ZincSurface,
                                focusedTextColor = ZincTextPrimary,
                                unfocusedTextColor = ZincTextPrimary
                            ),
                            shape = RoundedCornerShape(25.dp),
                            singleLine = true
                        )
                    } else {
                        // Brand Logo: Red 600 squircle + dekotube
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .clickable { currentTab = DekotubeTab.HOME }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SleekRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = "dekotube",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp,
                                color = ZincTextPrimary
                            )
                        }

                        // Right icons (Cast, Notifications, Search, Profile avatar)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { /* Cast */ }) {
                                Icon(
                                    imageVector = Icons.Default.Cast,
                                    contentDescription = "Cast",
                                    tint = ZincTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = { /* Notifications */ }) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = ZincTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            IconButton(onClick = { isSearchActive = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = ZincTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            // Profile avatar button (Indigo circular badge)
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (currentUser != null) Color(currentUser!!.avatarColor) else SleekIndigo
                                    )
                                    .clickable { showAuthDialog = true }
                                    .testTag("user_profile_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (currentUser != null) currentUser!!.channelName.take(1).uppercase() else "JD",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZincBg)
                    .navigationBarsPadding()
                    .testTag("bottom_navigation_bar")
            ) {
                HorizontalDivider(thickness = 1.dp, color = ZincBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Home
                    val isHome = currentTab == DekotubeTab.HOME
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { currentTab = DekotubeTab.HOME }
                            .testTag("navigation_home")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Home",
                            tint = if (isHome) Color.White else ZincTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Home",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isHome) Color.White else ZincTextSecondary
                        )
                    }

                    // Shorts
                    val isShorts = currentTab == DekotubeTab.SHORTS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { currentTab = DekotubeTab.SHORTS }
                            .testTag("navigation_shorts")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Shorts",
                            tint = if (isShorts) Color.White else ZincTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Shorts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isShorts) Color.White else ZincTextSecondary
                        )
                    }

                    // Center Add/Upload Circle Button
                    val isUpload = currentTab == DekotubeTab.UPLOAD
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isUpload) SleekRed else ZincElevated)
                                .border(1.dp, ZincBorderLight, CircleShape)
                                .clickable { currentTab = DekotubeTab.UPLOAD }
                                .testTag("navigation_upload"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Upload",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    // Admin
                    val isAdmin = currentTab == DekotubeTab.ADMIN
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { currentTab = DekotubeTab.ADMIN }
                            .testTag("navigation_admin")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Admin",
                            tint = if (isAdmin) Color.White else ZincTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Admin",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isAdmin) Color.White else ZincTextSecondary
                        )
                    }

                    // Earnings
                    val isMonetization = currentTab == DekotubeTab.MONETIZATION
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { currentTab = DekotubeTab.MONETIZATION }
                            .testTag("navigation_monetization")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = "Earnings",
                            tint = if (isMonetization) Color.White else ZincTextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Earnings",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isMonetization) Color.White else ZincTextSecondary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ZincBg)
        ) {
            when (currentTab) {
                DekotubeTab.HOME -> {
                    HomeScreen(
                        videos = filteredVideos,
                        onWatchVideo = { video ->
                            repository.recordView(video.id)
                            activeWatchVideo = video
                        },
                        onNavigateToShorts = { currentTab = DekotubeTab.SHORTS }
                    )
                }

                DekotubeTab.SHORTS -> {
                    ShortsScreen(
                        shorts = allVideos.filter { it.isShort },
                        onLikeShort = { id -> repository.toggleLike(id) },
                        onOpenComments = { short -> activeWatchVideo = short },
                        onRecordView = { id -> repository.recordView(id) }
                    )
                }

                DekotubeTab.UPLOAD -> {
                    UploadScreen(
                        onUploadSuccess = { newVideo ->
                            repository.uploadVideo(
                                title = newVideo.title,
                                description = newVideo.description,
                                category = newVideo.category,
                                isShort = newVideo.isShort,
                                tags = newVideo.tags
                            )
                            currentTab = if (newVideo.isShort) DekotubeTab.SHORTS else DekotubeTab.HOME
                        }
                    )
                }

                DekotubeTab.ADMIN -> {
                    AdminScreen(
                        users = allUsers,
                        videos = allVideos,
                        rules = allRules,
                        onAddRule = { title, cat, sev, desc -> repository.addRule(title, cat, sev, desc) },
                        onToggleRule = { id -> repository.toggleRuleActive(id) },
                        onDeleteRule = { id -> repository.deleteRule(id) },
                        onDeleteVideo = { id -> repository.deleteVideo(id) }
                    )
                }

                DekotubeTab.MONETIZATION -> {
                    MonetizationScreen(repository = repository)
                }
            }
        }
    }

    // Active Watch Dialog
    if (activeWatchVideo != null) {
        WatchDialog(
            video = activeWatchVideo!!,
            comments = activeComments,
            onDismiss = { activeWatchVideo = null },
            onLike = { id -> repository.toggleLike(id) },
            onAddComment = { videoId, text -> repository.addComment(videoId, text) }
        )
    }

    // Auth & Channel Dialog
    if (showAuthDialog) {
        AuthDialog(
            currentUser = currentUser,
            onDismiss = { showAuthDialog = false },
            onSignIn = { username -> repository.signIn(username) },
            onSignUp = { username, email, channel, role -> repository.signUp(username, email, channel, role) },
            onSignOut = { repository.signOut() },
            onSwitchToUser = { user -> repository.switchUser(user) },
            allUsers = allUsers
        )
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Dekotube") }
}
