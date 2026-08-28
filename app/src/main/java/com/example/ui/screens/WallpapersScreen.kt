package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun WallpapersScreen(
    repository: AdminRepository,
    onCreateWallpaper: () -> Unit,
    onEditWallpaper: (Wallpaper) -> Unit,
    onPreviewWallpaper: (Wallpaper) -> Unit
) {
    val wallpapers by repository.wallpapers.collectAsState()
    val currentAdmin by repository.currentAdmin.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirmDialog by remember { mutableStateOf<Wallpaper?>(null) }

    val canEdit = currentAdmin.role in listOf(AdminRole.SUPER_ADMIN, AdminRole.ADMIN, AdminRole.CONTENT_MANAGER)
    val canDelete = currentAdmin.role in listOf(AdminRole.SUPER_ADMIN, AdminRole.ADMIN)

    val filteredList = wallpapers.filter { wp ->
        val matchesSearch = wp.title.contains(searchQuery, ignoreCase = true) ||
                wp.category.contains(searchQuery, ignoreCase = true) ||
                wp.tags.any { it.contains(searchQuery, ignoreCase = true) }

        val matchesFilter = when (selectedFilter) {
            "LIVE" -> wp.type == WallpaperType.LIVE
            "STATIC" -> wp.type == WallpaperType.STATIC
            "FREE" -> wp.accessType == AccessType.FREE
            "PREMIUM" -> wp.accessType == AccessType.PREMIUM
            "FEATURED" -> wp.isFeatured
            "TRENDING" -> wp.isTrending
            "NEW" -> wp.isNew
            "SOUND" -> wp.soundAvailable
            "CHARGING" -> wp.chargingAnimationAvailable
            "INACTIVE" -> wp.status != ContentStatus.ACTIVE
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WALLPAPER LIBRARY",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredList.size} of ${wallpapers.size} total items",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (canEdit) {
                Button(
                    onClick = onCreateWallpaper,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianCanvas)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Wallpaper", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by title, category, tags...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ChampagneGold,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Filter Pills
        val filterOptions = listOf(
            "ALL" to "All",
            "LIVE" to "Live Wallpapers",
            "STATIC" to "Static",
            "PREMIUM" to "Premium",
            "FREE" to "Free",
            "FEATURED" to "Featured",
            "TRENDING" to "Trending",
            "NEW" to "New",
            "SOUND" to "With Audio",
            "CHARGING" to "Charging FX",
            "INACTIVE" to "Inactive"
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(filterOptions) { (key, label) ->
                FilterChip(
                    selected = selectedFilter == key,
                    onClick = { selectedFilter = key },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChampagneGold,
                        selectedLabelColor = ObsidianCanvas,
                        containerColor = MaterialTheme.colorScheme.surface,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedFilter == key,
                        borderColor = MaterialTheme.colorScheme.outline,
                        selectedBorderColor = ChampagneGold
                    )
                )
            }
        }

        // Bulk Action Bar if items selected
        if (selectedIds.isNotEmpty() && canEdit) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedIds.size} items selected",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneGold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                repository.bulkUpdateStatus(selectedIds, ContentStatus.ACTIVE)
                                selectedIds = emptySet()
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusSuccessDark),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Activate", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = {
                                repository.bulkUpdateStatus(selectedIds, ContentStatus.INACTIVE)
                                selectedIds = emptySet()
                            },
                            shape = RoundedCornerShape(4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusWarningDark),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Deactivate", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { selectedIds = emptySet() }) {
                            Text("Deselect All", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        // Wallpapers List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                EmptyStateCard(
                    title = "No Wallpapers Found",
                    description = if (searchQuery.isNotBlank() || selectedFilter != "ALL") "No wallpapers match your current search or filter criteria." else "Your wallpaper catalog is empty. Click 'Create Wallpaper' to publish your first wallpaper."
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { wp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Checkbox for bulk actions
                        if (canEdit) {
                            Checkbox(
                                checked = selectedIds.contains(wp.id),
                                onCheckedChange = { checked ->
                                    selectedIds = if (checked) selectedIds + wp.id else selectedIds - wp.id
                                }
                            )
                        }

                        // Thumbnail Box
                        Box(
                            modifier = Modifier
                                .size(64.dp, 84.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Black)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
                        ) {
                            AsyncImage(
                                model = wp.thumbnailUrl,
                                contentDescription = wp.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        // Metadata Details
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = wp.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusBadge(
                                    text = wp.type.name,
                                    type = if (wp.type == WallpaperType.LIVE) StatusBadgeType.INFO else StatusBadgeType.NEUTRAL
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                StatusBadge(
                                    text = wp.accessType.name,
                                    type = if (wp.accessType == AccessType.PREMIUM) StatusBadgeType.GOLD else StatusBadgeType.SUCCESS
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Category: ${wp.category} · Priority: ${wp.sortOrder}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            // Experience Badges
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (wp.soundAvailable) {
                                    StatusBadge(text = "SOUND", type = StatusBadgeType.INFO)
                                }
                                if (wp.chargingAnimationAvailable) {
                                    StatusBadge(text = "CHARGING FX", type = StatusBadgeType.GOLD)
                                }
                                if (wp.transitionAvailable) {
                                    StatusBadge(text = "TRANSITION", type = StatusBadgeType.NEUTRAL)
                                }
                                if (wp.isFeatured) {
                                    StatusBadge(text = "FEATURED", type = StatusBadgeType.SUCCESS)
                                }
                                if (wp.isTrending) {
                                    StatusBadge(text = "TRENDING", type = StatusBadgeType.WARNING)
                                }
                                if (wp.isNew) {
                                    StatusBadge(text = "NEW", type = StatusBadgeType.INFO)
                                }
                            }
                        }

                        // Action Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(
                                    onClick = { onPreviewWallpaper(wp) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Preview", tint = ChampagneGold)
                                }

                                if (canEdit) {
                                    IconButton(
                                        onClick = { onEditWallpaper(wp) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurface)
                                    }

                                    IconButton(
                                        onClick = { repository.toggleWallpaperStatus(wp.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (wp.status == ContentStatus.ACTIVE) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle Visibility",
                                            tint = if (wp.status == ContentStatus.ACTIVE) StatusSuccessDark else StatusWarningDark
                                        )
                                    }
                                }

                                if (canDelete) {
                                    IconButton(
                                        onClick = { showDeleteConfirmDialog = wp },
                                        modifier = Modifier.size(32.dp)
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
    }
}

    // Delete Confirmation Dialog
    showDeleteConfirmDialog?.let { wpToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = null },
            title = { Text("Delete Wallpaper Permanently?") },
            text = {
                Text(
                    "Are you sure you want to delete '${wpToDelete.title}' from the database?\n\n" +
                            "Note: Users who already applied this wallpaper on their Android devices will keep their local persistent media. Server-side deletion will stop new downloads only."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.deleteWallpaper(wpToDelete.id)
                        showDeleteConfirmDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusDangerDark)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
