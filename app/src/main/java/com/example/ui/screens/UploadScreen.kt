package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.VideoLibrary
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
import com.example.model.VideoItem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UploadScreen(
    onUploadSuccess: (VideoItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Technology") }
    var isShort by remember { mutableStateOf(false) }
    var tagsText by remember { mutableStateOf("dekotube, creator, 2026") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableFloatStateOf(0f) }
    var uploadFinished by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Technology", "Design", "Gaming", "News", "Education", "Entertainment")
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ZincBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .padding(bottom = 80.dp)
            .testTag("upload_screen")
    ) {
        Text(
            text = "Dekotube Creator Studio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ZincTextPrimary
        )
        Text(
            text = "Upload videos & shorts to stream worldwide",
            fontSize = 12.sp,
            color = ZincTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Upload Format Toggle (Standard 16:9 vs Shorts 9:16)
        Text(
            text = "Select Content Format",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ZincTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Standard Video Card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isShort = false },
                color = if (!isShort) ZincElevated else ZincSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (!isShort) SleekRedAccent else ZincBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = "Standard Video",
                        tint = if (!isShort) SleekRedAccent else ZincTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Standard Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ZincTextPrimary
                    )
                    Text(
                        text = "16:9 Landscape",
                        fontSize = 11.sp,
                        color = ZincTextSecondary
                    )
                }
            }

            // Shorts Card
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { isShort = true },
                color = if (isShort) ZincElevated else ZincSurface,
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isShort) SleekRedAccent else ZincBorder
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = "Shorts",
                        tint = if (isShort) SleekRedAccent else ZincTextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Dekotube Short",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = ZincTextPrimary
                    )
                    Text(
                        text = "9:16 Vertical (<60s)",
                        fontSize = 11.sp,
                        color = ZincTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Simulated Multer Upload Zone
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(ZincSurface)
                .border(1.dp, ZincBorder, RoundedCornerShape(20.dp))
                .padding(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = "Upload",
                    tint = SleekRedAccent,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isShort) "Attach Short Video (MP4 / WebM)" else "Attach Master Video (MP4 / WebM)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ZincTextPrimary
                )
                Text(
                    text = "Processed via Multer multipart media pipeline",
                    fontSize = 12.sp,
                    color = ZincTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = {
                title = it
                errorMessage = null
            },
            label = { Text("Video Title") },
            placeholder = { Text(if (isShort) "e.g. 3 Quick Developer Tips ⚡ #shorts" else "e.g. Masterclass in Scalable Architecture 2026") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_title_input"),
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

        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description & Notes") },
            placeholder = { Text("Add timestamps, links, and details for viewers") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
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

        Spacer(modifier = Modifier.height(14.dp))

        // Category Selection
        Text(
            text = "Category",
            fontSize = 12.sp,
            color = ZincTextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = category == cat
                Surface(
                    color = if (isSelected) Color.White else ZincElevated,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable { category = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.Black else ZincTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tags Input
        OutlinedTextField(
            value = tagsText,
            onValueChange = { tagsText = it },
            label = { Text("Tags (comma-separated)") },
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

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                color = SleekRedAccent,
                fontSize = 13.sp
            )
        }

        // Progress Bar when uploading
        AnimatedVisibility(visible = isUploading) {
            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Uploading & processing...", fontSize = 12.sp, color = ZincTextSecondary)
                    Text("${(uploadProgress * 100).toInt()}%", fontSize = 12.sp, color = SleekRedAccent, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { uploadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = SleekRed,
                    trackColor = ZincElevated
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
            onClick = {
                if (title.isBlank()) {
                    errorMessage = "Please provide an engaging title"
                    return@Button
                }
                isUploading = true
                coroutineScope.launch {
                    for (p in 1..10) {
                        delay(120)
                        uploadProgress = p / 10f
                    }
                    isUploading = false
                    uploadFinished = true
                    val parsedTags = tagsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val item = com.example.model.VideoItem(
                        id = (if (isShort) "short-" else "vid-") + System.currentTimeMillis(),
                        title = title.trim(),
                        description = description.trim(),
                        uploaderName = "Current Creator",
                        uploaderId = "usr-curr",
                        duration = if (isShort) "0:45" else "11:20",
                        views = 1,
                        likes = 1,
                        isShort = isShort,
                        category = category,
                        tags = parsedTags,
                        gradientColor1 = if (isShort) 0xFFFF416C else 0xFF1E3C72,
                        gradientColor2 = if (isShort) 0xFFFF4B2B else 0xFF2A5298,
                        timeAgo = "Just now"
                    )
                    onUploadSuccess(item)
                }
            },
            enabled = !isUploading,
            colors = ButtonDefaults.buttonColors(containerColor = SleekRed),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("submit_upload_button")
        ) {
            Text(
                text = if (isShort) "Publish Dekotube Short" else "Publish Master Video",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

