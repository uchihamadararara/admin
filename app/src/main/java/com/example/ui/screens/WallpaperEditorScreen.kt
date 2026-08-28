package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ui.components.VirtualPhoneSimulator
import com.example.ui.theme.*
import com.example.viewmodel.AdminScreen
import com.example.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperEditorScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val wallpaper by viewModel.editingWallpaper.collectAsState()
    val simulatorState by viewModel.simulatorState.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var isSaving by remember { mutableStateOf(false) }
    var saveError by remember { mutableStateOf<String?>(null) }

    var tagsInput by remember(wallpaper.id) {
        mutableStateOf(wallpaper.tags.joinToString(", "))
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
    ) {
        // Editor Action Header
        Surface(
            color = AmoledSurface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmoledCardBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.navigateTo(AdminScreen.WALLPAPERS) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = TextPrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column {
                        Text(
                            text = if (wallpaper.id.isBlank()) "Create Wallpaper" else "Edit Wallpaper",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (wallpaper.id.isBlank()) "Draft Document" else "ID: ${wallpaper.id.take(12)}...",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            isSaving = true
                            saveError = null
                            viewModel.saveEditingWallpaper { success, err ->
                                isSaving = false
                                if (!success) saveError = err
                            }
                        },
                        enabled = !isSaving,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalGold,
                            contentColor = AmoledBackground
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = AmoledBackground)
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Wallpaper", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Main Editor Content Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Save Error Banner
            if (saveError != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyalRoseContainer)
                        .border(1.dp, RoyalRose, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = RoyalRoseText)
                        Text(saveError ?: "", color = RoyalRoseText, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // SECTION 1: BASIC INFORMATION
            EditorSectionCard(title = "1. BASIC INFORMATION") {
                OutlinedTextField(
                    value = wallpaper.title,
                    onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(title = it) } },
                    label = { Text("Wallpaper Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = editorFieldColors(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = wallpaper.description,
                    onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(description = it) } },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = editorFieldColors(),
                    shape = RoundedCornerShape(8.dp),
                    minLines = 2
                )
            }

            // SECTION 2: ACCESS & EXPERIENCE CLASSIFICATION
            EditorSectionCard(title = "2. ACCESS & EXPERIENCE TYPE") {
                // Access Type Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Access Tier", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AccessType.entries.forEach { access ->
                            val selected = wallpaper.accessType == access
                            OptionChoicePill(
                                text = access.name,
                                isSelected = selected,
                                activeColor = if (access == AccessType.PREMIUM) RoyalGold else RoyalEmerald,
                                onClick = { viewModel.updateEditingWallpaper { w -> w.copy(accessType = access) } }
                            )
                        }
                    }
                    Text(
                        text = if (wallpaper.accessType == AccessType.PREMIUM)
                            "⭐ PREMIUM: Everyone can freely browse, view details & run simulator. Subscription required ONLY when applying to actual phone."
                        else "✓ FREE: Everyone can preview and apply freely.",
                        fontSize = 11.sp,
                        color = if (wallpaper.accessType == AccessType.PREMIUM) RoyalGoldText else RoyalEmeraldText
                    )
                }

                HorizontalDivider(color = AmoledCardBorder)

                // Content Type Selector
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Content Architecture", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ContentType.entries.forEach { type ->
                            val selected = wallpaper.contentType == type
                            OptionChoicePill(
                                text = type.name,
                                isSelected = selected,
                                activeColor = RoyalIndigo,
                                onClick = {
                                    viewModel.updateEditingWallpaper { w ->
                                        w.copy(
                                            contentType = type,
                                            liveExperienceType = if (type == ContentType.LIVE && w.liveExperienceType == null) LiveExperienceType.NORMAL else w.liveExperienceType
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

                // If Live: Experience Type
                if (wallpaper.contentType == ContentType.LIVE) {
                    HorizontalDivider(color = AmoledCardBorder)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Live Experience Type (Explicit Contract)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            LiveExperienceType.entries.forEach { exp ->
                                val selected = wallpaper.liveExperienceType == exp
                                OptionChoicePill(
                                    text = exp.name,
                                    isSelected = selected,
                                    activeColor = RoyalGold,
                                    onClick = { viewModel.updateEditingWallpaper { w -> w.copy(liveExperienceType = exp) } }
                                )
                            }
                        }
                        Text(
                            text = if (wallpaper.liveExperienceType == LiveExperienceType.NORMAL)
                                "NORMAL: Single primary live video loop + optional charging media."
                            else "TRANSITION: Multi-state explicit wallpaper with independent Home, Lock, Lock-to-Home, Charging transitions.",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // SECTION 3: MEDIA ASSET SLOTS
            EditorSectionCard(title = "3. MEDIA SLOTS (CLOUDFLARE R2 PUBLIC HTTPS URLS)") {
                // Thumbnail URL (Common)
                OutlinedTextField(
                    value = wallpaper.thumbnailUrl,
                    onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(thumbnailUrl = it) } },
                    label = { Text("Thumbnail Preview Image URL") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = editorFieldColors(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                when {
                    wallpaper.contentType == ContentType.STATIC -> {
                        OutlinedTextField(
                            value = wallpaper.primaryMediaUrl,
                            onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(primaryMediaUrl = it) } },
                            label = { Text("Primary Static Image URL *") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    wallpaper.liveExperienceType == LiveExperienceType.NORMAL -> {
                        OutlinedTextField(
                            value = wallpaper.advancedConfig.primaryUrl,
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(
                                        primaryMediaUrl = url,
                                        advancedConfig = w.advancedConfig.copy(primaryUrl = url)
                                    )
                                }
                            },
                            label = { Text("Primary Live Video URL * (Mandatory)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        Text("Optional Charging Experience Media:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = RoyalGold)

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.chargingEntryUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(chargingEntryUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("Charging Entry Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.chargingLoopUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(chargingLoopUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("Charging Loop Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.chargingReturnUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(chargingReturnUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("Charging Return Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    wallpaper.liveExperienceType == LiveExperienceType.TRANSITION -> {
                        Text("Transition Live Multi-State Slots:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalGold)

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.homeUrl,
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(
                                        primaryMediaUrl = url,
                                        advancedConfig = w.advancedConfig.copy(homeUrl = url)
                                    )
                                }
                            },
                            label = { Text("HOME State Video URL * (Mandatory)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.lockUrl,
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(lockUrl = url))
                                }
                            },
                            label = { Text("LOCK State Video URL * (Mandatory)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.homeToLockUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(homeToLockUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("HOME → LOCK Transition Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.lockToHomeUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(lockToHomeUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("LOCK → HOME Transition Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.homeToChargingUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(homeToChargingUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("HOME → CHARGING Transition Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.lockToChargingUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(lockToChargingUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("LOCK → CHARGING Transition Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.chargingLoopUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(chargingLoopUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("CHARGING LOOP Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = wallpaper.advancedConfig.chargingReturnUrl ?: "",
                            onValueChange = { url ->
                                viewModel.updateEditingWallpaper { w ->
                                    w.copy(advancedConfig = w.advancedConfig.copy(chargingReturnUrl = url.ifBlank { null }))
                                }
                            },
                            label = { Text("CHARGING RETURN Video URL (Optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = editorFieldColors(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // SECTION 4: METADATA & AUDIO
            EditorSectionCard(title = "4. METADATA & AUDIO SPECIFICATION") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Media Audio Available", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text("If audio is present, Android app prompts user Sound ON/OFF during Apply.", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = wallpaper.hasAudio,
                        onCheckedChange = { viewModel.updateEditingWallpaper { w -> w.copy(hasAudio = it) } },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = RoyalGold,
                            checkedTrackColor = RoyalGoldContainer
                        )
                    )
                }

                if (wallpaper.hasAudio) {
                    OutlinedTextField(
                        value = wallpaper.audioCodec ?: "",
                        onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(audioCodec = it.ifBlank { null }) } },
                        label = { Text("Audio Codec (e.g. aac, opus)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = editorFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = wallpaper.fps?.toString() ?: "",
                        onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(fps = it.toIntOrNull()) } },
                        label = { Text("FPS (e.g. 60)") },
                        modifier = Modifier.weight(1f),
                        colors = editorFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = wallpaper.durationMs?.toString() ?: "",
                        onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(durationMs = it.toLongOrNull()) } },
                        label = { Text("Duration (ms)") },
                        modifier = Modifier.weight(1f),
                        colors = editorFieldColors(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }

            // SECTION 5: TAXONOMY & STATUS
            EditorSectionCard(title = "5. TAXONOMY & PUBLISH STATUS") {
                // Category selection
                OutlinedTextField(
                    value = wallpaper.categoryId,
                    onValueChange = { viewModel.updateEditingWallpaper { w -> w.copy(categoryId = it) } },
                    label = { Text("Category (e.g. Cyberpunk, Anime, Nature)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = editorFieldColors(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Tags
                OutlinedTextField(
                    value = tagsInput,
                    onValueChange = {
                        tagsInput = it
                        val tagList = it.split(",").map { t -> t.trim() }.filter { t -> t.isNotBlank() }
                        viewModel.updateEditingWallpaper { w -> w.copy(tags = tagList) }
                    },
                    label = { Text("Tags (comma-separated, e.g. dark, neon, 4k)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = editorFieldColors(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                // Status
                Text("Publication Lifecycle State", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WallpaperStatus.entries.forEach { status ->
                        val selected = wallpaper.status == status
                        OptionChoicePill(
                            text = status.name,
                            isSelected = selected,
                            activeColor = when (status) {
                                WallpaperStatus.PUBLISHED -> RoyalEmerald
                                WallpaperStatus.DRAFT -> RoyalIndigo
                                WallpaperStatus.INACTIVE -> RoyalGold
                                WallpaperStatus.ARCHIVED -> TextMuted
                            },
                            onClick = { viewModel.updateEditingWallpaper { w -> w.copy(status = status) } }
                        )
                    }
                }
            }

            // SECTION 6: EMBEDDED VIRTUAL PHONE SIMULATOR
            EditorSectionCard(title = "6. VIRTUAL RUNTIME SIMULATOR") {
                VirtualPhoneSimulator(
                    wallpaper = wallpaper,
                    simulatorState = simulatorState,
                    onTogglePowerLock = { viewModel.simulatorTogglePowerLock() },
                    onToggleCharging = { viewModel.simulatorToggleCharging() },
                    onToggleSound = { viewModel.simulatorToggleSound() },
                    onCompleteTransition = { viewModel.simulatorCompleteTransition() }
                )
            }
        }
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                letterSpacing = 0.8.sp
            )
            content()
        }
    }
}

@Composable
private fun OptionChoicePill(
    text: String,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.2f) else AmoledSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) activeColor else AmoledCardBorder,
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) activeColor else TextSecondary
        )
    }
}

@Composable
private fun editorFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = RoyalGold,
    unfocusedBorderColor = AmoledCardBorder,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = RoyalGold,
    unfocusedLabelColor = TextSecondary
)
