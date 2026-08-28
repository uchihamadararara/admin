package com.example.ui.screens

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.MediaAsset
import com.example.ui.components.DestructiveConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.AdminViewModel

@Composable
fun MediaLibraryScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val mediaAssets by viewModel.mediaAssets.collectAsState()
    val clipboardManager = LocalClipboardManager.current

    var isAddingNew by remember { mutableStateOf(false) }
    var assetToDelete by remember { mutableStateOf<MediaAsset?>(null) }

    var newTitle by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }
    var newMimeType by remember { mutableStateOf("video/mp4") }
    var newAudioEnabled by remember { mutableStateOf(false) }
    var newCodec by remember { mutableStateOf("aac") }
    var newFps by remember { mutableStateOf("60") }
    var newWidth by remember { mutableStateOf("1080") }
    var newHeight by remember { mutableStateOf("2400") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Media Asset Registry",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Cloudflare R2 Public HTTPS References (${mediaAssets.size} assets)",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (viewModel.canManageWallpapers()) {
                Button(
                    onClick = { isAddingNew = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = AmoledBackground
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Register URL", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Cloudflare R2 Architecture Note
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(AmoledSurfaceVariant)
                .border(1.dp, AmoledCardBorder, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CloudQueue, contentDescription = null, tint = RoyalGold, modifier = Modifier.size(20.dp))
                Text(
                    text = "ℹ️ R2 Storage Architecture: High-bitrate video/image assets reside on Cloudflare R2 public buckets. Direct uploads are performed out-of-band to protect secrets; register validated public URLs here.",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }

        // Asset List
        if (mediaAssets.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PermMedia,
                title = "No Media References Registered",
                description = "Register your first Cloudflare R2 public HTTPS video or image URL to easily reference it when configuring wallpapers.",
                actionLabel = if (viewModel.canManageWallpapers()) "+ Register Media URL" else null,
                onAction = { isAddingNew = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mediaAssets, key = { it.id }) { asset ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail / Preview
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF0F111A)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (asset.mimeType.startsWith("image")) {
                                    AsyncImage(
                                        model = asset.url,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = null,
                                        tint = RoyalGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            // Asset Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = asset.title.ifBlank { "Media Asset" },
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    StatusPill(
                                        text = asset.mimeType.substringAfter("/").uppercase(),
                                        backgroundColor = RoyalIndigoContainer,
                                        textColor = RoyalIndigoText
                                    )
                                }

                                Text(
                                    text = asset.url,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextMuted,
                                    maxLines = 1
                                )

                                Text(
                                    text = "${asset.width ?: 1080}x${asset.height ?: 2400} • ${asset.fps ?: 60}fps • ${if (asset.hasAudio) "Audio (${asset.audioCodec ?: "aac"})" else "No Audio"}",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            // Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(asset.url))
                                        viewModel.showToast("URL copied to clipboard")
                                    },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = RoyalGold)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy URL", modifier = Modifier.size(16.dp))
                                }

                                if (viewModel.canManageWallpapers()) {
                                    IconButton(
                                        onClick = { assetToDelete = asset },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = RoyalRose)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Reference", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Media Reference Dialog
    if (isAddingNew) {
        AlertDialog(
            onDismissRequest = { isAddingNew = false },
            title = {
                Text("Register R2 Public Media Asset", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Asset Title (e.g. Cyber City Home Loop)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("Public HTTPS URL *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newMimeType,
                            onValueChange = { newMimeType = it },
                            label = { Text("MIME Type") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newFps,
                            onValueChange = { newFps = it },
                            label = { Text("FPS") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Has Embedded Audio", fontSize = 12.sp, color = TextPrimary)
                        Switch(
                            checked = newAudioEnabled,
                            onCheckedChange = { newAudioEnabled = it }
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val asset = MediaAsset(
                            title = newTitle,
                            url = newUrl,
                            mimeType = newMimeType,
                            hasAudio = newAudioEnabled,
                            audioCodec = if (newAudioEnabled) newCodec else null,
                            fps = newFps.toIntOrNull() ?: 60,
                            width = newWidth.toIntOrNull() ?: 1080,
                            height = newHeight.toIntOrNull() ?: 2400
                        )
                        viewModel.saveMediaAsset(asset) { success, _ ->
                            if (success) {
                                isAddingNew = false
                                newTitle = ""
                                newUrl = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground)
                ) {
                    Text("Register Reference", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isAddingNew = false }) {
                    Text("Cancel")
                }
            },
            containerColor = AmoledSurface
        )
    }

    // Delete Confirmation
    assetToDelete?.let { asset ->
        DestructiveConfirmDialog(
            title = "Delete Media Reference?",
            message = "This removes the metadata record from Firestore. Existing wallpapers configured with this URL will keep their string reference.",
            confirmText = "Delete Reference",
            onConfirm = {
                viewModel.deleteMediaAsset(asset.id, asset.url)
                assetToDelete = null
            },
            onDismiss = { assetToDelete = null }
        )
    }
}
