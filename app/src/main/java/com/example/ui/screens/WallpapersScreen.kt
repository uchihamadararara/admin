package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpapersScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val wallpapers by viewModel.filteredWallpapers.collectAsState()
    val rawWallpapers by viewModel.wallpapers.collectAsState()
    val searchQuery by viewModel.wallpaperSearch.collectAsState()
    val filterStatus by viewModel.filterStatus.collectAsState()
    val filterAccess by viewModel.filterAccess.collectAsState()
    val filterContentType by viewModel.filterContentType.collectAsState()
    val filterExperienceType by viewModel.filterExperienceType.collectAsState()

    var wallpaperToDelete by remember { mutableStateOf<Wallpaper?>(null) }
    var filterMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Action & Search Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.wallpaperSearch.value = it },
                placeholder = { Text("Search by title or tags...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.wallpaperSearch.value = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = TextSecondary)
                        }
                    }
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalGold,
                    unfocusedBorderColor = AmoledCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            if (viewModel.canManageWallpapers()) {
                Button(
                    onClick = { viewModel.startCreateWallpaper() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalGold,
                        contentColor = AmoledBackground
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Filter Pills Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Filter Dropdown Pill
            FilterPillButton(
                label = filterStatus?.name ?: "Status: All",
                isSelected = filterStatus != null,
                onClick = {
                    viewModel.filterStatus.value = when (filterStatus) {
                        null -> WallpaperStatus.PUBLISHED
                        WallpaperStatus.PUBLISHED -> WallpaperStatus.DRAFT
                        WallpaperStatus.DRAFT -> WallpaperStatus.INACTIVE
                        WallpaperStatus.INACTIVE -> WallpaperStatus.ARCHIVED
                        WallpaperStatus.ARCHIVED -> null
                    }
                }
            )

            // Access Filter Pill
            FilterPillButton(
                label = filterAccess?.name ?: "Access: All",
                isSelected = filterAccess != null,
                onClick = {
                    viewModel.filterAccess.value = when (filterAccess) {
                        null -> AccessType.FREE
                        AccessType.FREE -> AccessType.PREMIUM
                        AccessType.PREMIUM -> null
                    }
                }
            )

            // Content Type Filter Pill
            FilterPillButton(
                label = filterContentType?.name ?: "Type: All",
                isSelected = filterContentType != null,
                onClick = {
                    viewModel.filterContentType.value = when (filterContentType) {
                        null -> ContentType.STATIC
                        ContentType.STATIC -> ContentType.LIVE
                        ContentType.LIVE -> null
                    }
                }
            )

            // Experience Type Filter Pill (if Live)
            FilterPillButton(
                label = filterExperienceType?.name ?: "Exp: All",
                isSelected = filterExperienceType != null,
                onClick = {
                    viewModel.filterExperienceType.value = when (filterExperienceType) {
                        null -> LiveExperienceType.NORMAL
                        LiveExperienceType.NORMAL -> LiveExperienceType.TRANSITION
                        LiveExperienceType.TRANSITION -> null
                    }
                }
            )
        }

        // Wallpaper List / Empty States
        if (rawWallpapers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Wallpaper,
                title = "No Wallpapers Found",
                description = "Your Firestore database currently has 0 wallpaper records. Create your first static or live wallpaper below.",
                actionLabel = if (viewModel.canManageWallpapers()) "+ Create First Wallpaper" else null,
                onAction = { viewModel.startCreateWallpaper() }
            )
        } else if (wallpapers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.FilterListOff,
                title = "No Matches for Selected Filters",
                description = "Try resetting your search query or adjusting the status, access, and content type filters.",
                actionLabel = "Reset All Filters",
                onAction = {
                    viewModel.wallpaperSearch.value = ""
                    viewModel.filterStatus.value = null
                    viewModel.filterAccess.value = null
                    viewModel.filterContentType.value = null
                    viewModel.filterExperienceType.value = null
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(wallpapers, key = { it.id }) { wp ->
                    WallpaperAdminCard(
                        wallpaper = wp,
                        canEdit = viewModel.canManageWallpapers(),
                        onEdit = { viewModel.startEditWallpaper(wp) },
                        onTogglePublish = {
                            val newStatus = if (wp.status == WallpaperStatus.PUBLISHED) WallpaperStatus.DRAFT else WallpaperStatus.PUBLISHED
                            viewModel.setWallpaperStatus(wp.id, newStatus)
                        },
                        onArchive = { viewModel.setWallpaperStatus(wp.id, WallpaperStatus.ARCHIVED) },
                        onDelete = { wallpaperToDelete = wp }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    wallpaperToDelete?.let { targetWp ->
        DestructiveConfirmDialog(
            title = "Delete Wallpaper?",
            message = "Are you sure you want to delete '${targetWp.title}'? This will permanently remove the document from Cloud Firestore.",
            confirmText = "Delete Permanently",
            onConfirm = {
                viewModel.deleteWallpaper(targetWp.id, targetWp.title)
                wallpaperToDelete = null
            },
            onDismiss = { wallpaperToDelete = null }
        )
    }
}

@Composable
private fun FilterPillButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) RoyalGoldContainer else AmoledSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) RoyalGold.copy(alpha = 0.6f) else AmoledCardBorder,
                RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) RoyalGoldText else TextSecondary
        )
    }
}

@Composable
private fun WallpaperAdminCard(
    wallpaper: Wallpaper,
    canEdit: Boolean,
    onEdit: () -> Unit,
    onTogglePublish: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(width = 64.dp, height = 90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0F111A))
                    .border(1.dp, AmoledCardBorder, RoundedCornerShape(8.dp))
            ) {
                val previewImg = wallpaper.thumbnailUrl.ifBlank {
                    wallpaper.primaryMediaUrl.ifBlank {
                        wallpaper.advancedConfig.homeUrl.ifBlank { wallpaper.previewUrl }
                    }
                }
                if (previewImg.isNotBlank()) {
                    AsyncImage(
                        model = previewImg,
                        contentDescription = wallpaper.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                    )
                }
            }

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = wallpaper.title.ifBlank { "Untitled Wallpaper" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Pill row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AccessTypePill(type = wallpaper.accessType)
                    ContentTypePill(type = wallpaper.contentType, experienceType = wallpaper.liveExperienceType)
                    WallpaperStatusPill(status = wallpaper.status)
                }

                // Slots configured overview
                val slotSummary = when {
                    wallpaper.contentType == ContentType.STATIC -> "Primary Image Configured"
                    wallpaper.liveExperienceType == LiveExperienceType.NORMAL -> {
                        val chargingSlots = listOfNotNull(
                            if (!wallpaper.advancedConfig.chargingEntryUrl.isNullOrBlank()) "Entry" else null,
                            if (!wallpaper.advancedConfig.chargingLoopUrl.isNullOrBlank()) "Loop" else null,
                            if (!wallpaper.advancedConfig.chargingReturnUrl.isNullOrBlank()) "Return" else null
                        )
                        if (chargingSlots.isEmpty()) "Normal Video (No charging media)"
                        else "Normal Video + Charging (${chargingSlots.joinToString()})"
                    }
                    wallpaper.liveExperienceType == LiveExperienceType.TRANSITION -> {
                        val configuredSlots = listOfNotNull(
                            if (wallpaper.advancedConfig.homeUrl.isNotBlank()) "Home" else null,
                            if (wallpaper.advancedConfig.lockUrl.isNotBlank()) "Lock" else null,
                            if (!wallpaper.advancedConfig.homeToLockUrl.isNullOrBlank()) "H→L" else null,
                            if (!wallpaper.advancedConfig.lockToHomeUrl.isNullOrBlank()) "L→H" else null,
                            if (!wallpaper.advancedConfig.chargingLoopUrl.isNullOrBlank()) "Charge" else null
                        )
                        "Transition [${configuredSlots.joinToString()}]"
                    }
                    else -> "No slots configured"
                }

                Text(
                    text = slotSummary,
                    fontSize = 11.sp,
                    color = TextMuted
                )

                // Audio & Metrics
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (wallpaper.hasAudio) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = RoyalGoldText, modifier = Modifier.size(12.dp))
                            Text("Audio", fontSize = 10.sp, color = RoyalGoldText)
                        }
                    }
                    Text(
                        text = "${wallpaper.viewsCount} views • ${wallpaper.appliesCount} applies",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Actions Menu
            if (canEdit) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    IconButton(
                        onClick = onEdit,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AmoledSurfaceVariant,
                            contentColor = RoyalGold
                        ),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = onTogglePublish,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (wallpaper.status == WallpaperStatus.PUBLISHED) RoyalEmeraldContainer else AmoledSurfaceVariant,
                            contentColor = if (wallpaper.status == WallpaperStatus.PUBLISHED) RoyalEmeraldText else TextSecondary
                        ),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (wallpaper.status == WallpaperStatus.PUBLISHED) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Publish",
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onDelete,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = AmoledSurfaceVariant,
                            contentColor = RoyalRose
                        ),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
