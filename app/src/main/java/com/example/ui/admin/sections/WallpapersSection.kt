package com.example.ui.admin.sections

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.admin.AdminViewModel
import com.example.ui.theme.*

@Composable
fun WallpapersSection(viewModel: AdminViewModel) {
    when {
        viewModel.isPreviewSimulatorOpen && viewModel.previewingWallpaper != null -> {
            WallpaperSimulatorView(viewModel = viewModel, wallpaper = viewModel.previewingWallpaper!!)
        }
        viewModel.isEditorOpen -> {
            WallpaperEditorView(viewModel = viewModel, initialWallpaper = viewModel.editingWallpaper)
        }
        else -> {
            WallpaperListView(viewModel = viewModel)
        }
    }
}

// ==========================================
// 1. WALLPAPERS CATALOG LIST VIEW
// ==========================================
@Composable
fun WallpaperListView(viewModel: AdminViewModel) {
    val filteredList = viewModel.wallpapersList.filter { wp ->
        val matchesSearch = viewModel.wallpaperSearchQuery.isEmpty() ||
                wp.title.contains(viewModel.wallpaperSearchQuery, ignoreCase = true) ||
                wp.tags.any { it.contains(viewModel.wallpaperSearchQuery, ignoreCase = true) } ||
                wp.id.contains(viewModel.wallpaperSearchQuery, ignoreCase = true)

        val matchesType = when (viewModel.selectedTypeFilter) {
            "STATIC" -> wp.contentType == ContentType.STATIC
            "NORMAL" -> wp.contentType == ContentType.LIVE && wp.liveExperienceType == LiveExperienceType.NORMAL
            "TRANSITION" -> wp.contentType == ContentType.LIVE && wp.liveExperienceType == LiveExperienceType.TRANSITION
            else -> true
        }

        val matchesStatus = when (viewModel.selectedStatusFilter) {
            "PUBLISHED" -> wp.status == WallpaperStatus.PUBLISHED
            "DRAFT" -> wp.status == WallpaperStatus.DRAFT
            "INACTIVE" -> wp.status == WallpaperStatus.INACTIVE
            "ARCHIVED" -> wp.status == WallpaperStatus.ARCHIVED
            else -> true
        }

        val matchesTier = when (viewModel.selectedTierFilter) {
            "FREE" -> !wp.isPremium
            "PREMIUM" -> wp.isPremium
            else -> true
        }

        matchesSearch && matchesType && matchesStatus && matchesTier
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
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = "Wallpapers Catalog",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Live loops, transition bundles, and metadata",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        maxLines = 1
                    )
                }
                Button(
                    onClick = { viewModel.openWallpaperEditor(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("create_wallpaper_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Create", fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1)
                }
            }
        }

        // Search & Filter Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = viewModel.wallpaperSearchQuery,
                        onValueChange = { viewModel.wallpaperSearchQuery = it },
                        placeholder = { Text("Search by title, tag, or ID...", fontSize = 13.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                        trailingIcon = {
                            if (viewModel.wallpaperSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.wallpaperSearchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_wallpapers_input"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanPrimary,
                            unfocusedBorderColor = BorderSubtle,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Type Filter Pills
                    Text(text = "EXPERIENCE TYPE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val types = listOf("ALL" to "All Types", "NORMAL" to "Live: NORMAL", "TRANSITION" to "Live: TRANSITION", "STATIC" to "Static")
                        items(types) { (key, label) ->
                            FilterChip(
                                selected = viewModel.selectedTypeFilter == key,
                                onClick = { viewModel.selectedTypeFilter = key },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CyanPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = CyanPrimary
                                )
                            )
                        }
                    }

                    // Status & Tier Pills
                    Text(text = "STATUS & TIER", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val statuses = listOf("ALL" to "All Status", "PUBLISHED" to "Published", "DRAFT" to "Draft", "PREMIUM" to "VIP Only", "FREE" to "Free")
                        items(statuses) { (key, label) ->
                            val isSelected = when (key) {
                                "ALL" -> viewModel.selectedStatusFilter == "ALL" && viewModel.selectedTierFilter == "ALL"
                                "PUBLISHED", "DRAFT" -> viewModel.selectedStatusFilter == key
                                "PREMIUM", "FREE" -> viewModel.selectedTierFilter == key
                                else -> false
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    when (key) {
                                        "ALL" -> {
                                            viewModel.selectedStatusFilter = "ALL"
                                            viewModel.selectedTierFilter = "ALL"
                                        }
                                        "PUBLISHED", "DRAFT" -> viewModel.selectedStatusFilter = key
                                        "PREMIUM", "FREE" -> viewModel.selectedTierFilter = key
                                    }
                                },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SkyAccent.copy(alpha = 0.2f),
                                    selectedLabelColor = SkyAccent
                                )
                            )
                        }
                    }
                }
            }
        }

        // Count Summary
        item {
            Text(
                text = "Showing ${filteredList.size} of ${viewModel.wallpapersList.size} Wallpapers",
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Empty State
        if (filteredList.isEmpty()) {
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
                        Icon(Icons.Default.Wallpaper, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No wallpapers found", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "No wallpapers match your criteria. Tap '+ Create' to upload your first live wallpaper experience.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        // Wallpaper Cards
        items(filteredList) { wp ->
            WallpaperCardItem(
                wallpaper = wp,
                onEdit = { viewModel.openWallpaperEditor(wp) },
                onPreview = {
                    viewModel.previewingWallpaper = wp
                    viewModel.isPreviewSimulatorOpen = true
                    viewModel.updateSimulatorState()
                },
                onToggleStatus = { viewModel.toggleWallpaperStatus(wp.id) },
                onDelete = { viewModel.deleteWallpaper(wp.id) }
            )
        }
    }
}

@Composable
fun WallpaperCardItem(
    wallpaper: Wallpaper,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onToggleStatus: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = wallpaper.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (wallpaper.isPremium) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(StatusWarning.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("VIP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusWarning)
                        }
                    }
                }

                // Experience Type Pill
                val (pillBg, pillColor, pillText) = when {
                    wallpaper.contentType == ContentType.STATIC -> Triple(Color(0xFF334155), Color(0xFFCBD5E1), "STATIC")
                    wallpaper.liveExperienceType == LiveExperienceType.TRANSITION -> Triple(CyanPrimary.copy(alpha = 0.2f), CyanPrimary, "TRANSITION")
                    else -> Triple(SkyAccent.copy(alpha = 0.2f), SkyAccent, "NORMAL LIVE")
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(pillBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(pillText, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = pillColor)
                }
            }

            wallpaper.description?.let {
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = it, fontSize = 12.sp, color = TextSecondary, maxLines = 2)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Tags & Meta Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                wallpaper.tags.take(3).forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurface)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("#$tag", fontSize = 10.sp, color = TextSecondary)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${wallpaper.downloadCount} dl • ${wallpaper.viewCount} views",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = BorderSubtle)
            Spacer(modifier = Modifier.height(10.dp))

            // Card Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Toggle Pill Button
                val isPub = wallpaper.status == WallpaperStatus.PUBLISHED
                OutlinedButton(
                    onClick = onToggleStatus,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (isPub) StatusSuccess else StatusWarning
                    ),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPub) StatusSuccess else StatusWarning)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isPub) "Published" else "Draft", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onPreview,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurface, contentColor = CyanPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Preview", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = onEdit,
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurface, contentColor = TextPrimary),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = StatusDanger, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. WALLPAPER CREATE & EDIT VIEW
// ==========================================
@Composable
fun WallpaperEditorView(viewModel: AdminViewModel, initialWallpaper: Wallpaper?) {
    var title by remember { mutableStateOf(initialWallpaper?.title ?: "") }
    var description by remember { mutableStateOf(initialWallpaper?.description ?: "") }
    var contentType by remember { mutableStateOf(initialWallpaper?.contentType ?: ContentType.LIVE) }
    var liveType by remember { mutableStateOf(initialWallpaper?.liveExperienceType ?: LiveExperienceType.NORMAL) }
    var isPremium by remember { mutableStateOf(initialWallpaper?.isPremium ?: false) }
    var isFeatured by remember { mutableStateOf(initialWallpaper?.isFeatured ?: false) }
    var isTrending by remember { mutableStateOf(initialWallpaper?.isTrending ?: false) }
    var isNew by remember { mutableStateOf(initialWallpaper?.isNew ?: true) }
    var tagsString by remember { mutableStateOf(initialWallpaper?.tags?.joinToString(", ") ?: "") }
    var sortOrder by remember { mutableStateOf(initialWallpaper?.sortOrder?.toString() ?: "0") }

    // Advanced Slot URLs
    var normalPrimaryUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.primary?.url ?: "https://cdn.livewallpaper.app/wallpapers/sample/primary.mp4") }
    var normalChargingUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.chargingLoop?.url ?: "") }
    var transHomeUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.home?.url ?: "https://cdn.livewallpaper.app/wallpapers/sample/home.mp4") }
    var transLockUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.lock?.url ?: "https://cdn.livewallpaper.app/wallpapers/sample/lock.mp4") }
    var transLockToHomeUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.lockToHome?.url ?: "") }
    var transChargingUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.transitionChargingLoop?.url ?: "") }
    var staticImageUrl by remember { mutableStateOf(initialWallpaper?.advancedConfig?.primaryImage?.url ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sticky Header / Action Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { viewModel.closeWallpaperEditor() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (initialWallpaper?.title.isNullOrEmpty()) "Create Wallpaper" else "Edit Wallpaper",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Button(
                    onClick = {
                        val tags = tagsString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        val config = when (contentType) {
                            ContentType.STATIC -> AdvancedConfig(
                                primaryImage = MediaSlotAsset(url = staticImageUrl, mimeType = "image/png")
                            )
                            ContentType.LIVE -> {
                                if (liveType == LiveExperienceType.TRANSITION) {
                                    AdvancedConfig(
                                        home = MediaSlotAsset(url = transHomeUrl, durationSeconds = 12.0, hasAudio = true),
                                        lock = MediaSlotAsset(url = transLockUrl, durationSeconds = 10.0, hasAudio = false),
                                        lockToHome = if (transLockToHomeUrl.isNotEmpty()) MediaSlotAsset(url = transLockToHomeUrl, durationSeconds = 1.5) else null,
                                        transitionChargingLoop = if (transChargingUrl.isNotEmpty()) MediaSlotAsset(url = transChargingUrl, durationSeconds = 8.0, hasAudio = true) else null
                                    )
                                } else {
                                    AdvancedConfig(
                                        primary = MediaSlotAsset(url = normalPrimaryUrl, durationSeconds = 15.0, hasAudio = true),
                                        chargingLoop = if (normalChargingUrl.isNotEmpty()) MediaSlotAsset(url = normalChargingUrl, durationSeconds = 10.0, hasAudio = true) else null
                                    )
                                }
                            }
                        }

                        val updatedWp = (initialWallpaper ?: Wallpaper(id = "wp_" + System.currentTimeMillis(), title = "")).copy(
                            title = title.ifEmpty { "Untitled Wallpaper" },
                            description = description,
                            contentType = contentType,
                            liveExperienceType = liveType,
                            isPremium = isPremium,
                            isFeatured = isFeatured,
                            isTrending = isTrending,
                            isNew = isNew,
                            tags = tags,
                            sortOrder = sortOrder.toIntOrNull() ?: 0,
                            status = WallpaperStatus.PUBLISHED,
                            advancedConfig = config
                        )
                        viewModel.saveWallpaper(updatedWp)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save & Publish", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Section A: Basic Metadata
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SECTION A: BASIC INFO & TAXONOMY", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = tagsString,
                        onValueChange = { tagsString = it },
                        label = { Text("Tags (comma-separated)") },
                        placeholder = { Text("cyberpunk, neon, 4k, amoled") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Premium VIP Only", fontSize = 13.sp, color = TextPrimary)
                        Switch(checked = isPremium, onCheckedChange = { isPremium = it })
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Featured on Explore", fontSize = 13.sp, color = TextPrimary)
                        Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                    }
                }
            }
        }

        // Section B: Architecture Type Selection
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SECTION B: ARCHITECTURE TYPE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = contentType == ContentType.LIVE,
                            onClick = { contentType = ContentType.LIVE },
                            label = { Text("LIVE VIDEO ENGINE") }
                        )
                        FilterChip(
                            selected = contentType == ContentType.STATIC,
                            onClick = { contentType = ContentType.STATIC },
                            label = { Text("STATIC IMAGE") }
                        )
                    }

                    if (contentType == ContentType.LIVE) {
                        Text("Live Experience Type (Explicit Admin Field):", fontSize = 12.sp, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = liveType == LiveExperienceType.NORMAL,
                                onClick = { liveType = LiveExperienceType.NORMAL },
                                label = { Text("NORMAL (Single + Charging)") }
                            )
                            FilterChip(
                                selected = liveType == LiveExperienceType.TRANSITION,
                                onClick = { liveType = LiveExperienceType.TRANSITION },
                                label = { Text("TRANSITION (Multi-State)") }
                            )
                        }
                    }
                }
            }
        }

        // Section C: Media Slots
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SECTION C: MEDIA SLOT ASSETS (R2)", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)

                    if (contentType == ContentType.STATIC) {
                        OutlinedTextField(
                            value = staticImageUrl,
                            onValueChange = { staticImageUrl = it },
                            label = { Text("Primary Image URL (PNG/WEBP)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else if (liveType == LiveExperienceType.NORMAL) {
                        OutlinedTextField(
                            value = normalPrimaryUrl,
                            onValueChange = { normalPrimaryUrl = it },
                            label = { Text("Primary Live Video URL (MP4) *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = normalChargingUrl,
                            onValueChange = { normalChargingUrl = it },
                            label = { Text("Optional Charging Loop URL (MP4)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = transHomeUrl,
                            onValueChange = { transHomeUrl = it },
                            label = { Text("Home Screen Video URL (MP4) *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = transLockUrl,
                            onValueChange = { transLockUrl = it },
                            label = { Text("Lock Screen Video URL (MP4) *") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = transLockToHomeUrl,
                            onValueChange = { transLockToHomeUrl = it },
                            label = { Text("Optional Lock -> Home Transition (MP4)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = transChargingUrl,
                            onValueChange = { transChargingUrl = it },
                            label = { Text("Transition Charging Loop (MP4)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. VIRTUAL PHONE PREVIEW SIMULATOR
// ==========================================
@Composable
fun WallpaperSimulatorView(viewModel: AdminViewModel, wallpaper: Wallpaper) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { viewModel.isPreviewSimulatorOpen = false }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Close Simulator", tint = TextPrimary)
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text("Virtual Phone Simulator", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Testing: ${wallpaper.title}", fontSize = 11.sp, color = CyanPrimary)
                }
            }
        }

        // Phone Frame Mockup
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Black),
            shape = RoundedCornerShape(20.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Status Bar Mock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("12:00", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.Wifi, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                        if (viewModel.simIsCharging) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        }
                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }

                // Active Node Canvas Simulation
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (viewModel.simIsScreenOn) DarkSurface else Color(0xFF030508)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!viewModel.simIsScreenOn) {
                        Text("SCREEN OFF (ENGINE MUTED)", fontSize = 12.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (viewModel.simIsCharging) Icons.Default.ElectricBolt else if (viewModel.simIsLocked) Icons.Default.Lock else Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = CyanPrimary,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = viewModel.simCurrentStateName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Audio: ${if (viewModel.simSoundEnabled) "ON (Playing)" else "MUTED"}",
                                fontSize = 10.sp,
                                color = if (viewModel.simSoundEnabled) StatusSuccess else TextMuted
                            )
                        }
                    }
                }

                // Bottom Navigation Line
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(60.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.White.copy(alpha = 0.5f))
                )
            }
        }

        // Interactive Physical Simulation Controls Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PHYSICAL SIMULATION CONTROLS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.simIsScreenOn = !viewModel.simIsScreenOn
                            viewModel.updateSimulatorState()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (viewModel.simIsScreenOn) DarkSurface else StatusDanger.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (viewModel.simIsScreenOn) "Power: ON" else "Power: OFF", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.simIsLocked = !viewModel.simIsLocked
                            viewModel.updateSimulatorState()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (viewModel.simIsLocked) "State: LOCKED" else "State: HOME", fontSize = 11.sp)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.simIsCharging = !viewModel.simIsCharging
                            viewModel.updateSimulatorState()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = if (viewModel.simIsCharging) StatusSuccess.copy(alpha = 0.2f) else DarkSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (viewModel.simIsCharging) "⚡ Charger: IN" else "Charger: OUT", fontSize = 11.sp)
                    }

                    FilledTonalButton(
                        onClick = {
                            viewModel.simSoundEnabled = !viewModel.simSoundEnabled
                            viewModel.updateSimulatorState()
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = DarkSurface),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (viewModel.simSoundEnabled) "Sound: ON" else "Sound: OFF", fontSize = 11.sp)
                    }
                }
            }
        }

        // State Machine Live Log Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("STATE MACHINE TRANSITIONS LOG", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                viewModel.simLog.take(4).forEach { line ->
                    Text(
                        text = "• $line",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
