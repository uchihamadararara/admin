package com.example.ui.admin.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaAsset
import com.example.ui.admin.AdminViewModel
import com.example.ui.theme.*

@Composable
fun MediaLibrarySection(viewModel: AdminViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var isUploadDialogOpen by remember { mutableStateOf(false) }
    var uploadName by remember { mutableStateOf("") }
    var uploadUrl by remember { mutableStateOf("") }
    var uploadType by remember { mutableStateOf("video/mp4") }

    val clipboardManager = LocalClipboardManager.current
    val filteredAssets = viewModel.mediaAssetsList.filter {
        searchQuery.isEmpty() || it.name.contains(searchQuery, ignoreCase = true) || it.key.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Media Library (R2)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Cloudflare R2 storage assets & presigned delivery",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                Button(
                    onClick = { isUploadDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("upload_media_button")
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Upload", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // R2 Bucket Status Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("R2 BUCKET: livewallpaper-cdn-prod", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        Text("${viewModel.mediaAssetsList.size} items • Public CDN: https://cdn.livewallpaper.app", fontSize = 11.sp, color = TextSecondary)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search media by filename or key...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        // Empty State
        if (filteredAssets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No media assets found", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "No media assets currently in R2 storage. Tap 'Upload' to ingest video/image assets.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Media Asset Items
        items(filteredAssets) { asset ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (asset.mimeType.startsWith("video")) Icons.Default.Videocam else Icons.Default.Image,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(asset.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text(asset.key, fontSize = 10.sp, color = TextSecondary)
                            }
                        }
                        IconButton(onClick = { viewModel.deleteMediaAsset(asset.id) }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusDanger, modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${asset.sizeBytes / (1024 * 1024)} MB • ${asset.width ?: 1080}x${asset.height ?: 2400} • ${asset.durationSeconds ?: 0.0}s",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(asset.url))
                                viewModel.showToast("Copied public CDN URL to clipboard")
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy URL", fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }

    // Upload Asset Dialog
    if (isUploadDialogOpen) {
        AlertDialog(
            onDismissRequest = { isUploadDialogOpen = false },
            title = { Text("Upload Asset to Cloudflare R2", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = uploadName,
                        onValueChange = { uploadName = it },
                        label = { Text("Asset Filename (e.g. hero_loop.mp4)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uploadUrl,
                        onValueChange = { uploadUrl = it },
                        label = { Text("CDN Storage URL / Path") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (uploadName.isNotEmpty()) {
                            val newAsset = MediaAsset(
                                id = "med_" + System.currentTimeMillis().toString().takeLast(4),
                                key = "wallpapers/uploads/$uploadName",
                                name = uploadName,
                                url = uploadUrl.ifEmpty { "https://cdn.livewallpaper.app/wallpapers/uploads/$uploadName" },
                                mimeType = uploadType,
                                sizeBytes = 14200000,
                                width = 1080,
                                height = 2400,
                                durationSeconds = 10.0,
                                hasAudio = true
                            )
                            viewModel.addMediaAsset(newAsset)
                            isUploadDialogOpen = false
                        }
                    }
                ) {
                    Text("Upload Asset")
                }
            },
            dismissButton = {
                TextButton(onClick = { isUploadDialogOpen = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
