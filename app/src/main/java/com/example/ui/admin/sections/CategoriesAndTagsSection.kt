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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.admin.AdminViewModel
import com.example.ui.theme.*

@Composable
fun CategoriesSection(viewModel: AdminViewModel) {
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var catName by remember { mutableStateOf("") }
    var catSlug by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Categories", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Taxonomy and catalog organization", fontSize = 12.sp, color = TextSecondary)
                }
                Button(
                    onClick = { isAddDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Category", fontSize = 12.sp)
                }
            }
        }

        items(viewModel.categoriesList) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(cat.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Slug: /${cat.slug} • ${cat.count} Wallpapers", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(StatusSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddDialogOpen = false },
            title = { Text("Create New Category", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = catName,
                        onValueChange = {
                            catName = it
                            catSlug = it.lowercase().replace(" ", "-")
                        },
                        label = { Text("Category Display Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = catSlug,
                        onValueChange = { catSlug = it },
                        label = { Text("Slug (URL friendly)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (catName.isNotEmpty()) {
                            viewModel.addCategory(catName, catSlug)
                            isAddDialogOpen = false
                            catName = ""
                            catSlug = ""
                        }
                    }
                ) {
                    Text("Save Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddDialogOpen = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TagsSection(viewModel: AdminViewModel) {
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var tagName by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tags Management", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("Discovery indexing and trending algorithm tags", fontSize = 12.sp, color = TextSecondary)
                }
                Button(
                    onClick = { isAddDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Tag", fontSize = 12.sp)
                }
            }
        }

        items(viewModel.tagsList) { tag ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, tint = SkyAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("#${tag.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${tag.usageCount} items", fontSize = 11.sp, color = TextSecondary)
                        if (tag.isTrending) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusPurple.copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("Trending", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = StatusPurple)
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddDialogOpen = false },
            title = { Text("Create Discovery Tag", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = tagName,
                    onValueChange = { tagName = it },
                    label = { Text("Tag Name (e.g. Cyberpunk)") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (tagName.isNotEmpty()) {
                            viewModel.addTag(tagName)
                            isAddDialogOpen = false
                            tagName = ""
                        }
                    }
                ) { Text("Create Tag") }
            },
            dismissButton = { TextButton(onClick = { isAddDialogOpen = false }) { Text("Cancel") } }
        )
    }
}
