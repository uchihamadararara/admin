package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Wallpaper
import com.example.ui.theme.*
import com.example.viewmodel.SimulatorState
import com.example.viewmodel.SimulatorUIState

@Composable
fun VirtualPhoneSimulator(
    wallpaper: Wallpaper,
    simulatorState: SimulatorUIState,
    onTogglePowerLock: () -> Unit,
    onToggleCharging: () -> Unit,
    onToggleSound: () -> Unit,
    onCompleteTransition: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(GeoSurface)
            .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Simulator Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GeoPrimaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = GeoOnPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "VIRTUAL RUNTIME SIMULATOR",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 0.8.sp
                )
            }

            StatusPill(
                text = simulatorState.activeSlotName,
                backgroundColor = GeoSecondaryContainer,
                textColor = GeoOnSecondaryContainer,
                borderColor = GeoSecondary.copy(alpha = 0.4f)
            )
        }

        // Phone Bezel / Screen Box
        Box(
            modifier = Modifier
                .width(260.dp)
                .height(480.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFF0F111A))
                .border(4.dp, Color(0xFF2E344E), RoundedCornerShape(32.dp))
        ) {
            // Background / Active Media Slot Preview
            if (simulatorState.activeMediaUrl.isNotBlank()) {
                AsyncImage(
                    model = simulatorState.activeMediaUrl,
                    contentDescription = "Active Simulator Asset",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF181B26)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrokenImage,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "[Slot Not Configured]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Fallback to target state",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Top Camera Punch Hole & Status Bar
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12:45",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    // Center camera hole
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF000000))
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (simulatorState.soundEnabled) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                tint = GeoPrimaryContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        if (simulatorState.isCharging) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = GeoEmeraldContainer,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(
                            text = "${simulatorState.batteryPercent}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Content Overlay based on State
                when {
                    // Charging State Overlay
                    simulatorState.currentState in listOf(
                        SimulatorState.CHARGING_LOOP,
                        SimulatorState.HOME_TO_CHARGING,
                        SimulatorState.LOCK_TO_CHARGING
                    ) -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 60.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = null,
                                        tint = GeoEmeraldText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "${simulatorState.batteryPercent}% • Fast Charging",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Lock Screen Overlay
                    simulatorState.isLocked -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp, bottom = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "12:45",
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = "Friday, August 28",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Swipe to unlock",
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }

                    // Home Screen Overlay
                    else -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Minimal App Grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                repeat(4) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.White.copy(alpha = 0.3f))
                                            .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            // Dock
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .padding(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    repeat(4) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(Color.White.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Interactive Controller Actions
        Text(
            text = "HARDWARE & LIFECYCLE CONTROLS",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.8.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onTogglePowerLock,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (simulatorState.isLocked) GeoDarkAccent else GeoSurfaceVariant,
                    contentColor = if (simulatorState.isLocked) Color.White else TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (simulatorState.isLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (simulatorState.isLocked) "Unlock (Home)" else "Lock Screen",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = onToggleCharging,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (simulatorState.isCharging) GeoEmeraldContainer else GeoSurfaceVariant,
                    contentColor = if (simulatorState.isCharging) GeoEmeraldText else TextPrimary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Power,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (simulatorState.isCharging) "Unplug Charger" else "Plug Charger",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onToggleSound,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (simulatorState.soundEnabled) GeoPrimary else TextSecondary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (simulatorState.soundEnabled) GeoPrimary else GeoCardBorder
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = if (simulatorState.soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (simulatorState.soundEnabled) "Audio: ON" else "Audio: OFF",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (simulatorState.currentState in listOf(
                    SimulatorState.HOME_TO_LOCK,
                    SimulatorState.LOCK_TO_HOME,
                    SimulatorState.HOME_TO_CHARGING,
                    SimulatorState.LOCK_TO_CHARGING,
                    SimulatorState.CHARGING_RETURN
                )
            ) {
                Button(
                    onClick = onCompleteTransition,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GeoPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = "Finish Transition ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Simulator Event Log Console
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GeoSurfaceVariant)
                .border(1.dp, GeoCardBorder, RoundedCornerShape(14.dp))
                .padding(10.dp)
        ) {
            Text(
                text = "STATE MACHINE TELEMETRY",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = GeoPrimary,
                letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            simulatorState.stateLog.take(4).forEach { line ->
                Text(
                    text = "• $line",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = TextSecondary
                )
            }
        }
    }
}

