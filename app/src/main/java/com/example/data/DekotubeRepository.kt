package com.example.data

import com.example.model.CommentItem
import com.example.model.MonetizationCalcResult
import com.example.model.PlatformRule
import com.example.model.User
import com.example.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DekotubeRepository {

    private val initialUsers = listOf(
        User(
            id = "usr-admin",
            username = "admin",
            email = "admin@dekotube.io",
            channelName = "Dekotube Official",
            role = "admin",
            avatarColor = 0xFFFF0000,
            subscribers = 145000
        ),
        User(
            id = "usr-creator1",
            username = "alex_tech",
            email = "alex@techreview.com",
            channelName = "Alex Tech & Design",
            role = "creator",
            avatarColor = 0xFF2196F3,
            subscribers = 58200
        ),
        User(
            id = "usr-creator2",
            username = "sarah_code",
            email = "sarah@codelab.dev",
            channelName = "Sarah Codes",
            role = "creator",
            avatarColor = 0xFF9C27B0,
            subscribers = 92400
        )
    )

    private val initialRules = listOf(
        PlatformRule(
            id = "RULE-101",
            title = "Copyright & DMCA Compliance",
            category = "Copyright",
            description = "Only upload video content that you own or have explicit commercial rights to use.",
            severity = "High",
            active = true,
            updatedAt = "Aug 2026"
        ),
        PlatformRule(
            id = "RULE-102",
            title = "Community Safety & Harassment",
            category = "Safety",
            description = "Harassment, hate speech, threats, and self-harm content are strictly prohibited.",
            severity = "Critical",
            active = true,
            updatedAt = "Jul 2026"
        ),
        PlatformRule(
            id = "RULE-103",
            title = "Shorts Format & Originality",
            category = "Format",
            description = "Shorts must be vertical 9:16 format under 60 seconds. Mass reposted content may be deprioritized.",
            severity = "Medium",
            active = true,
            updatedAt = "Aug 2026"
        ),
        PlatformRule(
            id = "RULE-104",
            title = "Advertiser-Friendly Monetization",
            category = "Monetization",
            description = "Videos must maintain advertiser safety standards within the first 15 seconds to receive ad share.",
            severity = "High",
            active = true,
            updatedAt = "Aug 2026"
        ),
        PlatformRule(
            id = "RULE-105",
            title = "Spam, Clickbait & Scams",
            category = "Integrity",
            description = "Deceptive metadata, view manipulation bots, and undisclosed affiliate promotions are banned.",
            severity = "Critical",
            active = true,
            updatedAt = "Jun 2026"
        )
    )

    private val initialVideos = listOf(
        VideoItem(
            id = "vid-1",
            title = "Next-Gen Full Stack Video Streaming Architecture 2026",
            description = "Deep dive into Express, HTTP 206 Partial Streaming, and custom video pipelines.",
            uploaderName = "Alex Tech & Design",
            uploaderId = "usr-creator1",
            duration = "14:28",
            views = 184520,
            likes = 14200,
            isShort = false,
            category = "Technology",
            tags = listOf("fullstack", "streaming", "node", "coding"),
            gradientColor1 = 0xFF1E3C72,
            gradientColor2 = 0xFF2A5298,
            timeAgo = "2 days ago"
        ),
        VideoItem(
            id = "vid-2",
            title = "Crafting Modern YouTube Dark UI with Jetpack Compose",
            description = "Learn how to build responsive media interfaces with high contrast typography and custom cards.",
            uploaderName = "Sarah Codes",
            uploaderId = "usr-creator2",
            duration = "22:15",
            views = 328400,
            likes = 29500,
            isShort = false,
            category = "Design",
            tags = listOf("android", "compose", "ui", "youtube"),
            gradientColor1 = 0xFF8E2DE2,
            gradientColor2 = 0xFF4A00E0,
            timeAgo = "3 days ago"
        ),
        VideoItem(
            id = "vid-3",
            title = "Dekotube Platform Update: Creator Monetization 2.0",
            description = "Announcing our new 55/45 creator revenue share, Shorts creator pool, and channel perks.",
            uploaderName = "Dekotube Official",
            uploaderId = "usr-admin",
            duration = "08:45",
            views = 512000,
            likes = 43100,
            isShort = false,
            category = "News",
            tags = listOf("dekotube", "monetization", "creators"),
            gradientColor1 = 0xFFCC0000,
            gradientColor2 = 0xFF330000,
            timeAgo = "1 week ago"
        ),
        VideoItem(
            id = "vid-4",
            title = "Building High Performance Mobile Games with Kotlin",
            description = "Optimization techniques, frame pacing, and custom shader rendering.",
            uploaderName = "Alex Tech & Design",
            uploaderId = "usr-creator1",
            duration = "18:50",
            views = 96400,
            likes = 8120,
            isShort = false,
            category = "Gaming",
            tags = listOf("gaming", "kotlin", "performance"),
            gradientColor1 = 0xFF0F2027,
            gradientColor2 = 0xFF2C5364,
            timeAgo = "4 days ago"
        ),
        // Shorts
        VideoItem(
            id = "short-1",
            title = "Top 3 Developer Shortcuts You Never Knew! ⚡ #shorts",
            description = "Supercharge your IDE workflow in under 30 seconds.",
            uploaderName = "Sarah Codes",
            uploaderId = "usr-creator2",
            duration = "0:32",
            views = 920400,
            likes = 94100,
            isShort = true,
            category = "Technology",
            tags = listOf("shorts", "coding", "tips"),
            gradientColor1 = 0xFFB224EF,
            gradientColor2 = 0xFF7579FF,
            timeAgo = "Yesterday"
        ),
        VideoItem(
            id = "short-2",
            title = "How Flexbox Actually Aligns Items 🎯 #shorts #css",
            description = "Visual breakdown of justify-content vs align-items.",
            uploaderName = "Alex Tech & Design",
            uploaderId = "usr-creator1",
            duration = "0:45",
            views = 1428000,
            likes = 162000,
            isShort = true,
            category = "Design",
            tags = listOf("shorts", "css", "web"),
            gradientColor1 = 0xFFFF416C,
            gradientColor2 = 0xFFFF4B2B,
            timeAgo = "3 days ago"
        ),
        VideoItem(
            id = "short-3",
            title = "Dekotube Quick Tip: Maximize Shorts Pool RPM 📈",
            description = "Keep audience retention above 85% to trigger higher creator pool payouts.",
            uploaderName = "Dekotube Official",
            uploaderId = "usr-admin",
            duration = "0:58",
            views = 780300,
            likes = 81200,
            isShort = true,
            category = "News",
            tags = listOf("shorts", "monetization", "tips"),
            gradientColor1 = 0xFF11998E,
            gradientColor2 = 0xFF38EF7D,
            timeAgo = "5 days ago"
        )
    )

    private val initialComments = listOf(
        CommentItem("c-1", "vid-1", "Sarah Codes", "The HTTP 206 streaming explanation was fantastic!", "2 hours ago", 45),
        CommentItem("c-2", "vid-1", "Alex Tech & Design", "Thanks Sarah! Glad you found it useful.", "1 hour ago", 12),
        CommentItem("c-3", "vid-2", "MobileDev99", "Compose theme tokens make dark mode so clean to implement.", "5 hours ago", 28)
    )

    // State Flows
    private val _currentUser = MutableStateFlow<User?>(initialUsers[0]) // Default to Admin for easy testing
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow(initialUsers)
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _videos = MutableStateFlow(initialVideos)
    val videos: StateFlow<List<VideoItem>> = _videos.asStateFlow()

    private val _rules = MutableStateFlow(initialRules)
    val rules: StateFlow<List<PlatformRule>> = _rules.asStateFlow()

    private val _comments = MutableStateFlow(initialComments)
    val comments: StateFlow<List<CommentItem>> = _comments.asStateFlow()

    // Auth methods
    fun signIn(usernameOrEmail: String, role: String = "creator"): User {
        val existing = _users.value.find { 
            it.username.equals(usernameOrEmail, ignoreCase = true) || 
            it.email.equals(usernameOrEmail, ignoreCase = true) 
        }
        if (existing != null) {
            _currentUser.value = existing
            return existing
        }
        val newUser = User(
            id = "usr-${System.currentTimeMillis()}",
            username = usernameOrEmail.replace(" ", "_").lowercase(),
            email = "$usernameOrEmail@dekotube.io".lowercase(),
            channelName = usernameOrEmail,
            role = role,
            avatarColor = 0xFF00B0FF,
            subscribers = 10
        )
        _users.update { listOf(newUser) + it }
        _currentUser.value = newUser
        return newUser
    }

    fun signUp(username: String, email: String, channelName: String, role: String = "creator"): User {
        val newUser = User(
            id = "usr-${System.currentTimeMillis()}",
            username = username,
            email = email,
            channelName = channelName.ifBlank { "$username's Channel" },
            role = role,
            avatarColor = 0xFF4CAF50,
            subscribers = 0
        )
        _users.update { listOf(newUser) + it }
        _currentUser.value = newUser
        return newUser
    }

    fun signOut() {
        _currentUser.value = null
    }

    fun switchUser(user: User) {
        _currentUser.value = user
    }

    // Video & Shorts operations
    fun recordView(videoId: String) {
        _videos.update { list ->
            list.map { v ->
                if (v.id == videoId) v.copy(views = v.views + 1) else v
            }
        }
    }

    fun toggleLike(videoId: String) {
        _videos.update { list ->
            list.map { v ->
                if (v.id == videoId) v.copy(likes = v.likes + 1) else v
            }
        }
    }

    fun uploadVideo(
        title: String,
        description: String,
        category: String,
        isShort: Boolean,
        tags: List<String>
    ): VideoItem {
        val user = _currentUser.value ?: initialUsers[0]
        val colors = if (isShort) {
            Pair(0xFFFF416C, 0xFFFF4B2B)
        } else {
            Pair(0xFF1E3C72, 0xFF2A5298)
        }
        val newVideo = VideoItem(
            id = (if (isShort) "short-" else "vid-") + System.currentTimeMillis(),
            title = title,
            description = description,
            uploaderName = user.channelName,
            uploaderId = user.id,
            duration = if (isShort) "0:45" else "12:30",
            views = 1,
            likes = 1,
            isShort = isShort,
            category = category,
            tags = tags,
            gradientColor1 = colors.first,
            gradientColor2 = colors.second,
            timeAgo = "Just now"
        )
        _videos.update { listOf(newVideo) + it }
        return newVideo
    }

    fun deleteVideo(videoId: String) {
        _videos.update { list -> list.filter { it.id != videoId } }
    }

    // Platform Rules operations
    fun addRule(title: String, category: String, severity: String, description: String): PlatformRule {
        val newRule = PlatformRule(
            id = "RULE-${System.currentTimeMillis().toString().takeLast(3)}",
            title = title,
            category = category,
            description = description,
            severity = severity,
            active = true,
            updatedAt = "Today"
        )
        _rules.update { listOf(newRule) + it }
        return newRule
    }

    fun toggleRuleActive(ruleId: String) {
        _rules.update { list ->
            list.map { r ->
                if (r.id == ruleId) r.copy(active = !r.active) else r
            }
        }
    }

    fun deleteRule(ruleId: String) {
        _rules.update { list -> list.filter { it.id != ruleId } }
    }

    // Comments
    fun addComment(videoId: String, text: String): CommentItem {
        val user = _currentUser.value ?: initialUsers[0]
        val newComment = CommentItem(
            id = "c-${System.currentTimeMillis()}",
            videoId = videoId,
            author = user.channelName,
            text = text,
            timeAgo = "Just now",
            likes = 0
        )
        _comments.update { listOf(newComment) + it }
        return newComment
    }

    // Monetization Calculator
    fun calculateMonetization(
        longViews: Long,
        longRpm: Double,
        shortsViews: Long,
        shortsRpm: Double,
        subscribers: Int
    ): MonetizationCalcResult {
        val longFormCreatorRevenue = (longViews / 1000.0) * longRpm * 0.55 // 55% share
        val shortsCreatorRevenue = (shortsViews / 1000.0) * shortsRpm * 0.45 // 45% pool share
        val payingMembers = (subscribers * 0.01).coerceAtLeast(0.0)
        val membershipRevenue = payingMembers * 4.99 * 0.70 // 70% share

        val monthlyEarnings = longFormCreatorRevenue + shortsCreatorRevenue + membershipRevenue
        val yearlyEarnings = monthlyEarnings * 12.0
        val payoutEligible = monthlyEarnings >= 100.0
        val progress = (monthlyEarnings / 100.0 * 100.0).toFloat().coerceIn(0f, 100f)

        return MonetizationCalcResult(
            monthlyEarnings = monthlyEarnings,
            yearlyEarnings = yearlyEarnings,
            longFormEarnings = longFormCreatorRevenue,
            shortsEarnings = shortsCreatorRevenue,
            membershipEarnings = membershipRevenue,
            payoutEligible = payoutEligible,
            progressToPayout = progress
        )
    }
}
