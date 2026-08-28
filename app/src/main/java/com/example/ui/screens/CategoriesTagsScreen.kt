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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Category
import com.example.data.model.Tag
import com.example.ui.components.DestructiveConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.AdminViewModel

@Composable
fun CategoriesScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val categories by viewModel.categories.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var categoryToDelete by remember { mutableStateOf<Category?>(null) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    var nameInput by remember { mutableStateOf("") }
    var slugInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }
    var iconUrlInput by remember { mutableStateOf("") }
    var sortOrderInput by remember { mutableStateOf("0") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Categories Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${categories.size} taxonomies configured", fontSize = 12.sp, color = TextSecondary)
            }

            if (viewModel.canManageWallpapers()) {
                Button(
                    onClick = {
                        nameInput = ""
                        slugInput = ""
                        descInput = ""
                        iconUrlInput = ""
                        sortOrderInput = "0"
                        editingCategory = null
                        isAdding = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Category", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (categories.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Category,
                title = "No Categories Defined",
                description = "Define categories (such as AMOLED, Cyberpunk, Anime, Nature) to organize wallpapers for end users.",
                actionLabel = if (viewModel.canManageWallpapers()) "+ Create First Category" else null,
                onAction = { isAdding = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories, key = { it.id }) { cat ->
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
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(cat.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    StatusPill(
                                        text = if (cat.isActive) "ACTIVE" else "DISABLED",
                                        backgroundColor = if (cat.isActive) RoyalEmeraldContainer else AmoledSurfaceVariant,
                                        textColor = if (cat.isActive) RoyalEmeraldText else TextMuted
                                    )
                                }
                                if (cat.description.isNotBlank()) {
                                    Text(cat.description, fontSize = 12.sp, color = TextSecondary)
                                }
                                Text("Slug: ${cat.slug} • Order: ${cat.sortOrder} • ${cat.wallpapersCount} wallpapers", fontSize = 11.sp, color = TextMuted)
                            }

                            if (viewModel.canManageWallpapers()) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            editingCategory = cat
                                            nameInput = cat.name
                                            slugInput = cat.slug
                                            descInput = cat.description
                                            iconUrlInput = cat.iconUrl ?: ""
                                            sortOrderInput = cat.sortOrder.toString()
                                            isAdding = true
                                        },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = RoyalGold)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                                    }

                                    IconButton(
                                        onClick = { categoryToDelete = cat },
                                        colors = IconButtonDefaults.iconButtonColors(contentColor = RoyalRose)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add/Edit Dialog
    if (isAdding) {
        AlertDialog(
            onDismissRequest = { isAdding = false },
            title = {
                Text(
                    text = if (editingCategory == null) "New Category" else "Edit Category",
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = {
                            nameInput = it
                            if (editingCategory == null) {
                                slugInput = it.lowercase().replace(" ", "-")
                            }
                        },
                        label = { Text("Category Name *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = slugInput,
                        onValueChange = { slugInput = it },
                        label = { Text("Slug (identifier)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = descInput,
                        onValueChange = { descInput = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = sortOrderInput,
                        onValueChange = { sortOrderInput = it },
                        label = { Text("Sort Order") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cat = (editingCategory ?: Category()).copy(
                            name = nameInput,
                            slug = slugInput,
                            description = descInput,
                            iconUrl = iconUrlInput,
                            sortOrder = sortOrderInput.toIntOrNull() ?: 0
                        )
                        viewModel.saveCategory(cat) { success, _ ->
                            if (success) isAdding = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground)
                ) {
                    Text("Save Category", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isAdding = false }) { Text("Cancel") }
            },
            containerColor = AmoledSurface
        )
    }

    categoryToDelete?.let { cat ->
        DestructiveConfirmDialog(
            title = "Delete Category?",
            message = "Are you sure you want to delete category '${cat.name}'?",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteCategory(cat.id, cat.name)
                categoryToDelete = null
            },
            onDismiss = { categoryToDelete = null }
        )
    }
}

@Composable
fun TagsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val tags by viewModel.tags.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var tagToDelete by remember { mutableStateOf<Tag?>(null) }
    var nameInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Tags Taxonomy", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${tags.size} searchable tags", fontSize = 12.sp, color = TextSecondary)
            }

            if (viewModel.canManageWallpapers()) {
                Button(
                    onClick = {
                        nameInput = ""
                        isAdding = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Tag", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (tags.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Tag,
                title = "No Tags Defined",
                description = "Tags allow granular search indexing (e.g. 4k, neon, dark, dynamic, battery-friendly).",
                actionLabel = if (viewModel.canManageWallpapers()) "+ Add First Tag" else null,
                onAction = { isAdding = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(tags, key = { it.id }) { tag ->
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("#${tag.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalGoldText)
                                Text("Usage: ${tag.usageCount} wallpapers", fontSize = 11.sp, color = TextMuted)
                            }

                            if (viewModel.canManageWallpapers()) {
                                IconButton(
                                    onClick = { tagToDelete = tag },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = RoyalRose)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAdding) {
        AlertDialog(
            onDismissRequest = { isAdding = false },
            title = { Text("New Tag", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Tag Name (e.g. anime, amoled, dark)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val cleanName = nameInput.trim().lowercase().replace("#", "")
                        val tag = Tag(name = cleanName, slug = cleanName)
                        viewModel.saveTag(tag) { success, _ ->
                            if (success) isAdding = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground)
                ) {
                    Text("Save Tag", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isAdding = false }) { Text("Cancel") }
            },
            containerColor = AmoledSurface
        )
    }

    tagToDelete?.let { tag ->
        DestructiveConfirmDialog(
            title = "Delete Tag?",
            message = "Remove tag #${tag.name} from global taxonomy?",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteTag(tag.id, tag.name)
                tagToDelete = null
            },
            onDismiss = { tagToDelete = null }
        )
    }
}
