package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.PlatformRule
import com.example.model.User
import com.example.model.VideoItem
import com.example.ui.theme.*

@Composable
fun AdminScreen(
    users: List<User>,
    videos: List<VideoItem>,
    rules: List<PlatformRule>,
    onAddRule: (String, String, String, String) -> Unit,
    onToggleRule: (String) -> Unit,
    onDeleteRule: (String) -> Unit,
    onDeleteVideo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddRuleDialog by remember { mutableStateOf(false) }

    val totalUsers = users.size
    val totalVideos = videos.filter { !it.isShort }.size
    val totalShorts = videos.filter { it.isShort }.size
    val totalViews = videos.sumOf { it.views }
    val estRevenue = (totalViews / 1000.0 * 3.50 * 0.45) // Platform 45% revenue cut

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(ZincBg)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("admin_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Sleek Interface Header: bolt icon + Admin Dashboard + MANAGE RULES
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = SleekRedAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Admin Dashboard",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZincTextPrimary
                    )
                }

                Text(
                    text = "MANAGE RULES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekBlue,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .clickable { showAddRuleDialog = true }
                        .padding(vertical = 4.dp)
                        .testTag("admin_add_rule_button")
                )
            }
        }

        // Sleek Metric Cards Grid: rounded-3xl p-4 bg-zinc-900 border border-zinc-800
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AdminMetricCard(
                        title = "TOTAL USERS",
                        value = "$totalUsers",
                        subtext = "12%",
                        trendIcon = Icons.Default.ArrowUpward,
                        trendTint = SleekGreen,
                        modifier = Modifier.weight(1f)
                    )
                    AdminMetricCard(
                        title = "MONETIZATION",
                        value = "$${String.format(java.util.Locale.US, "%,.0f", estRevenue)}",
                        subtext = "Est.",
                        trendIcon = Icons.Default.Payments,
                        trendTint = ZincTextMuted,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Upload Queue Banner Card (rounded-3xl bg-zinc-900 with white upload pill)
                Surface(
                    color = ZincSurface,
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "UPLOAD QUEUE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = ZincTextMuted
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "${totalVideos + totalShorts} Videos Live",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFD4D4D8)
                            )
                        }

                        Surface(
                            color = ZincActionBg,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { showAddRuleDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "ADD RULE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Platform Rules Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Platform Rules & Governance (${rules.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZincTextPrimary
                )
                Text(
                    text = "${rules.count { it.active }} Active",
                    fontSize = 12.sp,
                    color = SleekGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Rules List
        items(rules) { rule ->
            PlatformRuleCard(
                rule = rule,
                onToggle = { onToggleRule(rule.id) },
                onDelete = { onDeleteRule(rule.id) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Moderation Section
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Video Moderation (${videos.size} total)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ZincTextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        items(videos.take(6)) { video ->
            Surface(
                color = ZincSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = video.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ZincTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "${video.uploaderName} • ${if (video.isShort) "Short" else "Video"} • ${formatViews(video.views)} views",
                            fontSize = 11.sp,
                            color = ZincTextSecondary
                        )
                    }
                    IconButton(
                        onClick = { onDeleteVideo(video.id) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Remove Video",
                            tint = SleekRedAccent
                        )
                    }
                }
            }
        }
    }

    // Add Rule Dialog
    if (showAddRuleDialog) {
        AddRuleDialog(
            onDismiss = { showAddRuleDialog = false },
            onConfirm = { title, category, severity, desc ->
                onAddRule(title, category, severity, desc)
                showAddRuleDialog = false
            }
        )
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    subtext: String,
    trendIcon: androidx.compose.ui.graphics.vector.ImageVector,
    trendTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ZincSurface,
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ZincTextMuted
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = value,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZincTextPrimary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = trendIcon,
                    contentDescription = null,
                    tint = trendTint,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = subtext,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = trendTint
                )
            }
        }
    }
}

@Composable
fun PlatformRuleCard(
    rule: PlatformRule,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = ZincSurface,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = rule.id,
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        color = ZincTextSecondary
                    )
                    val badgeColor = when (rule.severity) {
                        "Critical" -> SleekRedAccent
                        "High" -> SleekOrange
                        else -> SleekBlue
                    }
                    Surface(
                        color = badgeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = rule.severity.uppercase(),
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = rule.active,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = SleekGreen,
                            uncheckedTrackColor = ZincBorderLight,
                            uncheckedThumbColor = ZincTextSecondary
                        ),
                        modifier = Modifier.height(28.dp)
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete Rule",
                            tint = ZincTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = rule.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (rule.active) ZincTextPrimary else ZincTextSecondary
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = rule.description,
                fontSize = 12.sp,
                color = ZincTextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
fun AddRuleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Copyright") }
    var severity by remember { mutableStateOf("High") }
    var description by remember { mutableStateOf("") }

    val categories = listOf("Copyright", "Safety", "Format", "Monetization", "Integrity")
    val severities = listOf("Critical", "High", "Medium")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = ZincSurface,
            shape = RoundedCornerShape(24.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ZincBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Add Platform Rule",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZincTextPrimary
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Rule Title") },
                    placeholder = { Text("e.g. AI Content Disclosure") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekRedAccent,
                        unfocusedBorderColor = ZincBorder,
                        focusedContainerColor = ZincElevated,
                        unfocusedContainerColor = ZincElevated,
                        focusedTextColor = ZincTextPrimary,
                        unfocusedTextColor = ZincTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category chips
                Text("Category", fontSize = 12.sp, color = ZincTextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        val isSel = category == cat
                        Surface(
                            color = if (isSel) Color.White else ZincElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { category = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSel) Color.Black else ZincTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Severity chips
                Text("Severity", fontSize = 12.sp, color = ZincTextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    severities.forEach { sev ->
                        val isSel = severity == sev
                        Surface(
                            color = if (isSel) Color.White else ZincElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { severity = sev }
                        ) {
                            Text(
                                text = sev,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSel) Color.Black else ZincTextPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Rule Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SleekRedAccent,
                        unfocusedBorderColor = ZincBorder,
                        focusedContainerColor = ZincElevated,
                        unfocusedContainerColor = ZincElevated,
                        focusedTextColor = ZincTextPrimary,
                        unfocusedTextColor = ZincTextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = ZincTextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isNotBlank() && description.isNotBlank()) {
                                onConfirm(title.trim(), category, severity, description.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SleekRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Create Rule", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

