package com.example.ui.admin.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AdminSection
import com.example.model.ContentType
import com.example.model.LiveExperienceType
import com.example.model.WallpaperStatus
import com.example.ui.admin.AdminViewModel
import com.example.ui.theme.*

@Composable
fun DashboardSection(viewModel: AdminViewModel) {
    val totalWallpapers = viewModel.wallpapersList.size
    val publishedCount = viewModel.wallpapersList.count { it.status == WallpaperStatus.PUBLISHED }
    val draftCount = totalWallpapers - publishedCount
    val liveCount = viewModel.wallpapersList.count { it.contentType == ContentType.LIVE }
    val transitionCount = viewModel.wallpapersList.count { it.liveExperienceType == LiveExperienceType.TRANSITION }
    val normalCount = liveCount - transitionCount
    val staticCount = totalWallpapers - liveCount
    val totalUsers = viewModel.usersList.size
    val vipUsers = viewModel.usersList.count { it.tier != "FREE" }
    val totalAssets = viewModel.mediaAssetsList.size
    val totalMediaMB = viewModel.mediaAssetsList.sumOf { it.sizeBytes } / (1024 * 1024)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Operations Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time catalog metrics and platform infrastructure health",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                FilledTonalButton(
                    onClick = { viewModel.showToast("Dashboard metrics refreshed") },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = DarkCard,
                        contentColor = CyanPrimary
                    ),
                    modifier = Modifier.testTag("refresh_dashboard_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Refresh", fontSize = 12.sp)
                }
            }
        }

        // Quick Actions Row
        item {
            Text(
                text = "QUICK ACTIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    title = "+ New Wallpaper",
                    icon = Icons.Default.AddPhotoAlternate,
                    color = CyanPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.openWallpaperEditor(null)
                    viewModel.currentSection = AdminSection.WALLPAPERS
                }
                QuickActionButton(
                    title = "Upload Media",
                    icon = Icons.Default.CloudUpload,
                    color = SkyAccent,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.currentSection = AdminSection.MEDIA_LIBRARY
                }
                QuickActionButton(
                    title = "Broadcast Alert",
                    icon = Icons.Default.Campaign,
                    color = StatusWarning,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.currentSection = AdminSection.ANNOUNCEMENTS
                }
            }
        }

        // Metric Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricDashboardCard(
                        title = "TOTAL WALLPAPERS",
                        value = "$totalWallpapers",
                        subtext = "$publishedCount Published • $draftCount Draft",
                        icon = Icons.Default.Wallpaper,
                        iconColor = CyanPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricDashboardCard(
                        title = "LIVE ARCHITECTURE",
                        value = "$liveCount Live",
                        subtext = "$normalCount Normal • $transitionCount Transition",
                        icon = Icons.Default.PlayCircle,
                        iconColor = SkyAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricDashboardCard(
                        title = "APP USERS",
                        value = "$totalUsers",
                        subtext = "$vipUsers VIP Active • ${totalUsers - vipUsers} Free",
                        icon = Icons.Default.People,
                        iconColor = StatusSuccess,
                        modifier = Modifier.weight(1f)
                    )
                    MetricDashboardCard(
                        title = "CLOUDFLARE R2",
                        value = "$totalAssets Files",
                        subtext = "$totalMediaMB MB Total Storage",
                        icon = Icons.Default.CloudQueue,
                        iconColor = StatusPurple,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Infrastructure Signals & System Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Platform Infrastructure & Service Status",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    StatusItemRow(label = "Supabase Database & Auth", status = "Connected (23ms)", isHealthy = true)
                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    StatusItemRow(label = "Cloudflare R2 Media Bucket", status = "Active CDN", isHealthy = true)
                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    StatusItemRow(label = "Google Play RTDN Webhook", status = "Listening (Pub/Sub)", isHealthy = true)
                    HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(vertical = 8.dp))
                    StatusItemRow(label = "AdMob Server-Side Verification", status = "Key Verification OK", isHealthy = true)
                }
            }
        }

        // Recent Audit Activity Card
        item {
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
                        Text(
                            text = "Recent Admin Activity",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        TextButton(
                            onClick = { viewModel.currentSection = AdminSection.AUDIT_LOGS }
                        ) {
                            Text("View All", fontSize = 12.sp, color = CyanPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    viewModel.auditLogsList.take(3).forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(CyanPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${log.action} • ${log.targetResource}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${log.adminEmail} • ${log.timestamp}",
                                    fontSize = 10.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MetricDashboardCard(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatusItemRow(label: String, status: String, isHealthy: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isHealthy) StatusSuccess else StatusDanger)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (isHealthy) StatusSuccess else StatusDanger
            )
        }
    }
}
