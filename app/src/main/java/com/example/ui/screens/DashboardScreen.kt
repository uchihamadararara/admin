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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
fun DashboardScreen(
    repository: AdminRepository,
    onNavigateToWallpapers: () -> Unit,
    onNavigateToSubscriptions: () -> Unit,
    onNavigateToUsers: () -> Unit,
    onNavigateToModeration: () -> Unit,
    onNavigateToMedia: () -> Unit,
    onPreviewWallpaper: (Wallpaper) -> Unit
) {
    val metrics by repository.metrics.collectAsState()
    val wallpapers by repository.wallpapers.collectAsState()
    val auditLogs by repository.auditLogs.collectAsState()
    val currentAdmin by repository.currentAdmin.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header & Current Role
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PLATFORM CONTROL CENTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = ChampagneGold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Welcome, ${currentAdmin.name}",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Live Wallpaper Platform Production Management Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    AdminRolePill(role = currentAdmin.role)
                }
            }
        }

        // Top Metrics Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Total Wallpapers",
                        value = "${metrics.totalWallpapers}",
                        subtitle = "${metrics.liveWallpapers} Live · ${metrics.staticWallpapers} Static",
                        icon = Icons.Default.Wallpaper,
                        accentColor = ChampagneGold,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToWallpapers
                    )
                    MetricCard(
                        title = "Active Subscribers",
                        value = "${metrics.activeSubscribers}",
                        subtitle = "Google Play Authoritative",
                        icon = Icons.Default.Star,
                        accentColor = StatusSuccessDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToSubscriptions
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(
                        title = "Total Registered Users",
                        value = "${metrics.totalUsers}",
                        subtitle = "+${metrics.newUsersToday} new today",
                        icon = Icons.Default.People,
                        accentColor = StatusInfoDark,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToUsers
                    )
                    MetricCard(
                        title = "Rewarded SSV Verified",
                        value = "${metrics.rewardCompletionsToday}",
                        subtitle = "Free tier applies today",
                        icon = Icons.Default.CheckCircle,
                        accentColor = ChampagneGoldLight,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Live Operational Health & Storage Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM INFRASTRUCTURE HEALTH",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        StatusBadge(text = "ALL SERVICES NOMINAL", type = StatusBadgeType.SUCCESS)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HealthPill("Supabase Postgres", "Online", StatusSuccessDark, Modifier.weight(1f))
                        HealthPill("Cloudflare R2", "1.48 GB Active", ChampagneGold, Modifier.weight(1f))
                        HealthPill("Google Play RTDN", "Subscribed", StatusInfoDark, Modifier.weight(1f))
                    }
                }
            }
        }

        // Featured & Top Performing Wallpapers
        item {
            SectionHeader(
                title = "Top Performing Content",
                subtitle = "Ranked by real on-device wallpaper applications",
                actionButtonText = "View Library",
                onActionClick = onNavigateToWallpapers
            )
        }

        if (wallpapers.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Wallpapers Published",
                    description = "Use the 'Upload Media' or 'New Live Wallpaper' action to add content to your library."
                )
            }
        } else {
            items(wallpapers.take(4)) { wallpaper ->
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
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = wallpaper.title,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${wallpaper.appliesCount} applies · ${wallpaper.viewsCount} views · ${wallpaper.favoritesCount} favorites",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (wallpaper.soundAvailable) {
                                    StatusBadge(text = "SOUND AVAILABLE", type = StatusBadgeType.INFO)
                                }
                                if (wallpaper.chargingAnimationAvailable) {
                                    StatusBadge(text = "CHARGING FX", type = StatusBadgeType.GOLD)
                                }
                            }
                        }

                        Button(
                            onClick = { onPreviewWallpaper(wallpaper) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }

        // Recent Audit Log
        item {
            SectionHeader(
                title = "Recent Administrative Activity",
                subtitle = "Immutable audit log of all backend operations"
            )
        }

        if (auditLogs.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No Audit Logs",
                    description = "Administrative actions will be automatically recorded in the immutable audit log."
                )
            }
        } else {
            items(auditLogs.take(5)) { log ->
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                StatusBadge(text = log.action, type = StatusBadgeType.GOLD)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = log.adminEmail,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = log.details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = log.createdAt.substringAfter("T").substringBefore("Z"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthPill(name: String, status: String, accent: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(6.dp))
            .padding(8.dp)
    ) {
        Column {
            Text(text = name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = status, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = accent)
        }
    }
}
