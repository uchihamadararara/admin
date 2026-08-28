package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun MediaLibraryScreen(
    repository: AdminRepository
) {
    val mediaAssets by repository.mediaAssets.collectAsState()
    val currentAdmin by repository.currentAdmin.collectAsState()

    var showUploadModal by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var alertMessage by remember { mutableStateOf<String?>(null) }

    val canUpload = currentAdmin.role in listOf(AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.CONTENT_MANAGER)
    val canDelete = currentAdmin.role in listOf(AdminRole.SUPER_ADMIN, AdminRole.ADMIN)

    val filteredAssets = mediaAssets.filter { asset ->
        when (selectedFilter) {
            "VIDEO" -> asset.assetType == "VIDEO"
            "IMAGE" -> asset.assetType == "IMAGE"
            "CHARGING" -> asset.assetType == "CHARGING_ANIMATION"
            "AUDIO" -> asset.hasAudio
            "UNLINKED" -> asset.linkedWallpaperId == null
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CLOUDFLARE R2 MEDIA STORAGE",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Secure server-side media dispatcher (R2 secrets never sent to client)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canUpload) {
                Button(
                    onClick = { showUploadModal = true },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianCanvas)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Upload R2 Asset", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        alertMessage?.let { msg ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = StatusWarningBgDark),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = StatusWarningDark)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, style = MaterialTheme.typography.bodySmall, color = StatusWarningDark)
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { alertMessage = null }) {
                        Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        // Filters
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All Assets", "VIDEO" to "Videos", "IMAGE" to "Images", "CHARGING" to "Charging FX", "AUDIO" to "Audio Tracks", "UNLINKED" to "Unlinked / Orphaned").forEach { (k, v) ->
                FilterChip(
                    selected = selectedFilter == k,
                    onClick = { selectedFilter = k },
                    label = { Text(v, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        // Media List
        if (filteredAssets.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "No Media Assets Found",
                    description = if (selectedFilter != "ALL") "No media assets found matching the '$selectedFilter' filter." else "No media files uploaded to R2 storage yet. Click 'Upload R2 Asset' to upload new media."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredAssets, key = { it.id }) { asset ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = asset.filename,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                StatusBadge(text = asset.assetType, type = StatusBadgeType.INFO)
                                if (asset.hasAudio) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    StatusBadge(text = "AUDIO", type = StatusBadgeType.GOLD)
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Key: ${asset.r2ObjectKey} · Size: %.2f MB".format(asset.sizeBytes / (1024.0 * 1024.0)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (asset.linkedWallpaperId != null) {
                                Text(
                                    text = "Linked to Wallpaper ID: ${asset.linkedWallpaperId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusSuccessDark
                                )
                            } else {
                                Text(
                                    text = "Unlinked Asset (Safe to remove if unused)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusWarningDark
                                )
                            }
                        }

                        if (canDelete) {
                            IconButton(
                                onClick = {
                                    val deleted = repository.deleteMediaAsset(asset.id)
                                    if (!deleted) {
                                        alertMessage = "Cannot delete '${asset.filename}' because it is linked to an active wallpaper. Unlink it first."
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusDangerDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

    if (showUploadModal) {
        var filename by remember { mutableStateOf("quantum_aurora_60fps.mp4") }
        var assetType by remember { mutableStateOf("VIDEO") }
        var hasAudio by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showUploadModal = false },
            title = { Text("Upload R2 Media via Secure Server Dispatcher") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Cloudflare R2 Presigned Upload URL will be requested from Edge Function 'admin-media-upload'.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = filename,
                        onValueChange = { filename = it },
                        label = { Text("Filename") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasAudio, onCheckedChange = { hasAudio = it })
                        Text("Contains Audio Track", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newAsset = MediaAsset(
                            r2ObjectKey = "media/${assetType.lowercase()}/$filename",
                            filename = filename,
                            mimeType = if (filename.endsWith(".mp4")) "video/mp4" else "image/jpeg",
                            sizeBytes = 24500000L,
                            assetType = assetType,
                            hasAudio = hasAudio
                        )
                        repository.registerMediaAsset(newAsset)
                        showUploadModal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianCanvas)
                ) {
                    Text("Register & Dispatch")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUploadModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
