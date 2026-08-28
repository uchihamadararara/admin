package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.*
import com.example.ui.theme.*

@Composable
fun WallpaperPreviewModal(
    wallpaper: Wallpaper,
    onDismiss: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(wallpaper.soundAvailable) }
    var isSimulatingCharging by remember { mutableStateOf(false) }
    var transitionProgress by remember { mutableFloatStateOf(0f) }
    var batteryLevel by remember { mutableIntStateOf(68) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = wallpaper.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = wallpaper.type.name,
                                type = if (wallpaper.type == WallpaperType.LIVE) StatusBadgeType.INFO else StatusBadgeType.NEUTRAL
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(
                                text = wallpaper.accessType.name,
                                type = if (wallpaper.accessType == AccessType.PREMIUM) StatusBadgeType.GOLD else StatusBadgeType.SUCCESS
                            )
                        }
                        Text(
                            text = "R2 Key: ${wallpaper.mediaUrl.substringAfterLast("/")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulator Canvas & Controls Grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Simulated Android Phone Canvas (9:16 aspect ratio preview)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.Black)
                            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        // Background Wallpaper Image / Video Placeholder
                        AsyncImage(
                            model = wallpaper.thumbnailUrl,
                            contentDescription = wallpaper.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // If simulating charging and charging animation is available
                        if (isSimulatingCharging && wallpaper.chargingAnimationAvailable) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    // Animated Charging Ring
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .background(ChampagneGold.copy(alpha = 0.15f))
                                            .border(
                                                3.dp,
                                                Brush.radialGradient(
                                                    listOf(ChampagneGold, Color.Transparent)
                                                ),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = ChampagneGold,
                                            modifier = Modifier.size(54.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "$batteryLevel% CHARGING",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 2.sp
                                        ),
                                        color = ChampagneGoldLight
                                    )
                                    Text(
                                        text = "Charging FX: ${wallpaper.chargingAnimationType}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        // Phone Status Bar Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("12:00", style = MaterialTheme.typography.labelSmall, color = Color.White)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (wallpaper.soundAvailable && soundEnabled) {
                                    Icon(
                                        imageVector = Icons.Default.VolumeUp,
                                        contentDescription = null,
                                        tint = ChampagneGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    if (isSimulatingCharging) "⚡ $batteryLevel%" else "🔋 $batteryLevel%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Metadata & Simulation Controls Pane
                    Column(
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "LIVE PLAYBACK CONTROLS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = { isPlaying = !isPlaying },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isPlaying) "Pause" else "Play")
                                    }

                                    if (wallpaper.soundAvailable) {
                                        FilledTonalButton(
                                            onClick = { soundEnabled = !soundEnabled },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (soundEnabled) "Audio ON" else "Muted")
                                        }
                                    }
                                }
                            }
                        }

                        // Charging Experience Tester
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "CHARGING TRANSITION TESTER",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = ChampagneGold
                                    )
                                    StatusBadge(
                                        text = if (wallpaper.chargingAnimationAvailable) "AVAILABLE" else "NO CHARGING FX",
                                        type = if (wallpaper.chargingAnimationAvailable) StatusBadgeType.GOLD else StatusBadgeType.NEUTRAL
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                if (wallpaper.chargingAnimationAvailable) {
                                    Button(
                                        onClick = { isSimulatingCharging = !isSimulatingCharging },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSimulatingCharging) StatusWarningDark else ChampagneGold,
                                            contentColor = ObsidianCanvas
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isSimulatingCharging) "Stop Charging Event" else "Simulate Plugged in Charger")
                                    }
                                } else {
                                    Text(
                                        text = "When plugged in, this wallpaper continues standard Live playback without custom battery animation overlay.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Technical Specifications Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "MEDIA SPECIFICATIONS",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                SpecRow("Resolution", "${wallpaper.width} x ${wallpaper.height} (${wallpaper.aspectRatio})")
                                SpecRow("Frame Rate", "${wallpaper.fps} FPS")
                                SpecRow("Duration", "${wallpaper.durationSeconds}s")
                                SpecRow("File Size", "%.2f MB".format(wallpaper.fileSizeBytes / (1024.0 * 1024.0)))
                                SpecRow("Audio Track", if (wallpaper.soundAvailable) "Present (${wallpaper.soundMetadata.codec.uppercase()})" else "None")
                                SpecRow("Transitions", if (wallpaper.transitionAvailable) "${wallpaper.transitionType} (${wallpaper.transitionDurationMs}ms)" else "Standard")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = MaterialTheme.colorScheme.onSurface)
    }
}
