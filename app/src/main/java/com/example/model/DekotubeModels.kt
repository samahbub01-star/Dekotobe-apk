package com.example.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val channelName: String,
    val role: String, // "admin" or "creator"
    val avatarColor: Long,
    val subscribers: Int = 0
)

data class VideoItem(
    val id: String,
    val title: String,
    val description: String,
    val uploaderName: String,
    val uploaderId: String,
    val duration: String,
    var views: Long,
    var likes: Long,
    val isShort: Boolean,
    val category: String,
    val tags: List<String>,
    val gradientColor1: Long,
    val gradientColor2: Long,
    val timeAgo: String = "1 day ago"
)

data class PlatformRule(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val severity: String, // "Critical", "High", "Medium"
    var active: Boolean,
    val updatedAt: String
)

data class CommentItem(
    val id: String,
    val videoId: String,
    val author: String,
    val text: String,
    val timeAgo: String,
    val likes: Int = 0
)

data class MonetizationCalcResult(
    val monthlyEarnings: Double,
    val yearlyEarnings: Double,
    val longFormEarnings: Double,
    val shortsEarnings: Double,
    val membershipEarnings: Double,
    val payoutEligible: Boolean,
    val progressToPayout: Float
)
