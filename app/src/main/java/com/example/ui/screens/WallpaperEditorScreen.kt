package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperEditorScreen(
    repository: AdminRepository,
    wallpaperToEdit: Wallpaper?,
    onNavigateBack: () -> Unit
) {
    val isNew = wallpaperToEdit == null
    val categories by repository.categories.collectAsState()

    var title by remember { mutableStateOf(wallpaperToEdit?.title ?: "") }
    var description by remember { mutableStateOf(wallpaperToEdit?.description ?: "") }
    var type by remember { mutableStateOf(wallpaperToEdit?.type ?: WallpaperType.LIVE) }
    var accessType by remember { mutableStateOf(wallpaperToEdit?.accessType ?: AccessType.FREE) }
    var status by remember { mutableStateOf(wallpaperToEdit?.status ?: ContentStatus.ACTIVE) }
    var selectedCategory by remember { mutableStateOf(wallpaperToEdit?.category ?: categories.firstOrNull()?.name ?: "Abstract") }
    var tagsInput by remember { mutableStateOf(wallpaperToEdit?.tags?.joinToString(", ") ?: "4K Ultra, 60 FPS") }
    var isFeatured by remember { mutableStateOf(wallpaperToEdit?.isFeatured ?: false) }
    var isTrending by remember { mutableStateOf(wallpaperToEdit?.isTrending ?: false) }
    var isNewItem by remember { mutableStateOf(wallpaperToEdit?.isNew ?: true) }
    var sortOrder by remember { mutableIntStateOf(wallpaperToEdit?.sortOrder ?: 0) }

    // Media URLs
    var thumbnailUrl by remember { mutableStateOf(wallpaperToEdit?.thumbnailUrl ?: "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?w=600") }
    var mediaUrl by remember { mutableStateOf(wallpaperToEdit?.mediaUrl ?: "https://assets.mixkit.co/videos/preview/mixkit-nebula-in-deep-space-42603-large.mp4") }
    var durationSeconds by remember { mutableFloatStateOf(wallpaperToEdit?.durationSeconds ?: 15.0f) }
    var fps by remember { mutableIntStateOf(wallpaperToEdit?.fps ?: 60) }

    // Sound metadata
    var soundAvailable by remember { mutableStateOf(wallpaperToEdit?.soundAvailable ?: false) }
    var audioVolume by remember { mutableFloatStateOf(wallpaperToEdit?.soundMetadata?.defaultVolume ?: 1.0f) }

    // Charging Experience
    var chargingAnimationAvailable by remember { mutableStateOf(wallpaperToEdit?.chargingAnimationAvailable ?: false) }
    var chargingType by remember { mutableStateOf(wallpaperToEdit?.chargingAnimationType ?: "ENERGY_BEAM") }
    var chargingAsset by remember { mutableStateOf(wallpaperToEdit?.chargingAnimationAsset ?: "media/charging/plasma_beam.mp4") }
    var chargingDurationMs by remember { mutableIntStateOf(wallpaperToEdit?.chargingTransitionDurationMs ?: 300) }

    // Transition Experience
    var transitionAvailable by remember { mutableStateOf(wallpaperToEdit?.transitionAvailable ?: false) }
    var transitionType by remember { mutableStateOf(wallpaperToEdit?.transitionType ?: "FADE") }
    var transitionDurationMs by remember { mutableIntStateOf(wallpaperToEdit?.transitionDurationMs ?: 400) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isNew) "Create New Wallpaper" else "Edit '${wallpaperToEdit?.title}'",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (title.isBlank() || thumbnailUrl.isBlank() || mediaUrl.isBlank()) {
                                errorMessage = "Title, Thumbnail URL, and Media URL are strictly required."
                                return@Button
                            }

                            val tagsList = tagsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            val categoryObj = categories.find { it.name == selectedCategory }

                            val updatedWp = (wallpaperToEdit ?: Wallpaper(
                                title = title,
                                thumbnailUrl = thumbnailUrl,
                                mediaUrl = mediaUrl
                            )).copy(
                                title = title,
                                description = description,
                                type = type,
                                accessType = accessType,
                                status = status,
                                category = selectedCategory,
                                categoryId = categoryObj?.id,
                                tags = tagsList,
                                isFeatured = isFeatured,
                                isTrending = isTrending,
                                isNew = isNewItem,
                                sortOrder = sortOrder,
                                thumbnailUrl = thumbnailUrl,
                                mediaUrl = mediaUrl,
                                durationSeconds = durationSeconds,
                                fps = fps,
                                soundAvailable = soundAvailable,
                                soundMetadata = SoundMetadata(
                                    hasAudioTrack = soundAvailable,
                                    defaultVolume = audioVolume
                                ),
                                chargingAnimationAvailable = chargingAnimationAvailable,
                                chargingAnimationType = chargingType,
                                chargingAnimationAsset = if (chargingAnimationAvailable) chargingAsset else null,
                                chargingTransitionDurationMs = chargingDurationMs,
                                transitionAvailable = transitionAvailable,
                                transitionType = transitionType,
                                transitionDurationMs = transitionDurationMs
                            )

                            repository.saveWallpaper(updatedWp, isNew = isNew)
                            onNavigateBack()
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianCanvas)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isNew) "Publish Wallpaper" else "Save Changes", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            errorMessage?.let { msg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusDangerBgDark),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = StatusDangerDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = msg, color = StatusDangerDark, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Basic Information Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. BASIC INFORMATION", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Type Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Content Type", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = type == WallpaperType.LIVE,
                                    onClick = { type = WallpaperType.LIVE },
                                    label = { Text("LIVE VIDEO") }
                                )
                                FilterChip(
                                    selected = type == WallpaperType.STATIC,
                                    onClick = { type = WallpaperType.STATIC },
                                    label = { Text("STATIC IMAGE") }
                                )
                            }
                        }

                        // Access Tier
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Monetization Tier", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = accessType == AccessType.FREE,
                                    onClick = { accessType = AccessType.FREE },
                                    label = { Text("FREE (Rewarded Ad)") }
                                )
                                FilterChip(
                                    selected = accessType == AccessType.PREMIUM,
                                    onClick = { accessType = AccessType.PREMIUM },
                                    label = { Text("PREMIUM") }
                                )
                            }
                        }
                    }

                    // Category & Tags
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { selectedCategory = it },
                            label = { Text("Category") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = tagsInput,
                            onValueChange = { tagsInput = it },
                            label = { Text("Tags (comma-separated)") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }

                    // Discovery Flags
                    Text("Discovery Flags", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isFeatured, onCheckedChange = { isFeatured = it })
                            Text("Featured", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isTrending, onCheckedChange = { isTrending = it })
                            Text("Trending", style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = isNewItem, onCheckedChange = { isNewItem = it })
                            Text("New", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Cloudflare R2 Media Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("2. CLOUDFLARE R2 MEDIA ASSETS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)

                    OutlinedTextField(
                        value = mediaUrl,
                        onValueChange = { mediaUrl = it },
                        label = { Text("Media Asset URL / R2 Key *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = thumbnailUrl,
                        onValueChange = { thumbnailUrl = it },
                        label = { Text("Thumbnail URL *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = "$fps",
                            onValueChange = { fps = it.toIntOrNull() ?: 60 },
                            label = { Text("Frame Rate (FPS)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = "$durationSeconds",
                            onValueChange = { durationSeconds = it.toFloatOrNull() ?: 15f },
                            label = { Text("Duration (Sec)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }

            // Sound Metadata Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("3. LIVE WALLPAPER SOUND", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)
                            Text("Sound availability is content-driven per wallpaper.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = soundAvailable, onCheckedChange = { soundAvailable = it })
                    }

                    if (soundAvailable) {
                        Text("Default Audio Volume: ${(audioVolume * 100).toInt()}%", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = audioVolume,
                            onValueChange = { audioVolume = it },
                            valueRange = 0.0f..1.0f
                        )
                    }
                }
            }

            // Charging Experience Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("4. CHARGING EXPERIENCE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)
                            Text("Define battery-connected animation transition.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = chargingAnimationAvailable, onCheckedChange = { chargingAnimationAvailable = it })
                    }

                    if (chargingAnimationAvailable) {
                        OutlinedTextField(
                            value = chargingType,
                            onValueChange = { chargingType = it },
                            label = { Text("Charging Animation Type (e.g. ENERGY_BEAM, BATTERY_PULSE)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = chargingAsset,
                            onValueChange = { chargingAsset = it },
                            label = { Text("Charging Asset R2 Key") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // Transition Experience Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("5. VISUAL TRANSITIONS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)
                            Text("Smooth state transitions supported by Android WallpaperService.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = transitionAvailable, onCheckedChange = { transitionAvailable = it })
                    }

                    if (transitionAvailable) {
                        OutlinedTextField(
                            value = transitionType,
                            onValueChange = { transitionType = it },
                            label = { Text("Transition Type (e.g. FADE, CROSSFADE, ZOOM_ENTER)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
