package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AccessTypePill
import com.example.ui.components.ContentTypePill
import com.example.ui.components.EmptyStateView
import com.example.ui.components.WallpaperStatusPill
import com.example.ui.theme.*
import com.example.viewmodel.AdminScreen
import com.example.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val wallpapers by viewModel.wallpapers.collectAsState()
    val users by viewModel.users.collectAsState()
    val mediaAssets by viewModel.mediaAssets.collectAsState()
    val reports by viewModel.moderationReports.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val currentAdmin = viewModel.currentAdmin()

    val totalWp = wallpapers.size
    val publishedWp = wallpapers.count { it.status == WallpaperStatus.PUBLISHED }
    val draftWp = wallpapers.count { it.status == WallpaperStatus.DRAFT }
    val freeWp = wallpapers.count { it.accessType == AccessType.FREE }
    val premiumWp = wallpapers.count { it.accessType == AccessType.PREMIUM }
    val staticWp = wallpapers.count { it.contentType == ContentType.STATIC }
    val normalLiveWp = wallpapers.count { it.contentType == ContentType.LIVE && it.liveExperienceType == LiveExperienceType.NORMAL }
    val transitionLiveWp = wallpapers.count { it.contentType == ContentType.LIVE && it.liveExperienceType == LiveExperienceType.TRANSITION }

    val totalUsers = users.size
    val vipUsers = users.count { it.subscriptionTier == SubscriptionTier.VIP }
    val openReports = reports.count { it.status == ReportStatus.OPEN }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GeoBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Welcome & Quick Action Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = GeoSurface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Royal Control Console",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Signed in as ${currentAdmin?.email ?: "Admin"} • Live Firestore sync",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (viewModel.canManageWallpapers()) {
                    Button(
                        onClick = { viewModel.startCreateWallpaper() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GeoPrimary,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Wallpaper", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Key Metrics Grid
        Text(
            text = "SYSTEM METRICS (LIVE FIRESTORE DATA)",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.8.sp
        )

        // Row 1 Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Total Wallpapers",
                value = "$totalWp",
                subtitle = "$publishedWp Published • $draftWp Draft",
                icon = Icons.Default.Wallpaper,
                containerColor = GeoTertiaryContainer,
                iconBadgeBg = GeoDarkAccent,
                iconColor = Color.White,
                textColor = GeoOnTertiaryContainer,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AdminScreen.WALLPAPERS) }
            )
            MetricCard(
                title = "Registered Users",
                value = "$totalUsers",
                subtitle = if (totalUsers == 0) "No registered users yet" else "$vipUsers VIP subscribers",
                icon = Icons.Default.People,
                containerColor = GeoSecondaryContainer,
                iconBadgeBg = GeoOnSecondaryContainer,
                iconColor = Color.White,
                textColor = GeoOnSecondaryContainer,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AdminScreen.USERS) }
            )
        }

        // Row 2 Metrics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Media Assets",
                value = "${mediaAssets.size}",
                subtitle = "Cloudflare R2 Public References",
                icon = Icons.Default.PermMedia,
                containerColor = GeoPrimaryContainer,
                iconBadgeBg = GeoOnPrimaryContainer,
                iconColor = Color.White,
                textColor = GeoOnPrimaryContainer,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AdminScreen.MEDIA_LIBRARY) }
            )
            MetricCard(
                title = "Moderation Queue",
                value = "$openReports",
                subtitle = if (openReports == 0) "All reports resolved" else "$openReports Open reports pending",
                icon = Icons.Default.Report,
                containerColor = if (openReports > 0) GeoRoseContainer else GeoSurfaceVariant,
                iconBadgeBg = if (openReports > 0) GeoRose else GeoSecondary,
                iconColor = Color.White,
                textColor = if (openReports > 0) GeoRoseText else TextPrimary,
                modifier = Modifier.weight(1f),
                onClick = { viewModel.navigateTo(AdminScreen.MODERATION) }
            )
        }

        // Content Breakdown Cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = GeoSurface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "CATALOG DISTRIBUTION",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DistributionItem("Free Content", "$freeWp", GeoEmeraldText, GeoEmeraldContainer)
                    DistributionItem("Premium (VIP)", "$premiumWp", GeoGoldText, GeoGoldContainer)
                    DistributionItem("Static Image", "$staticWp", TextSecondary, GeoSurfaceVariant)
                    DistributionItem("Normal Live", "$normalLiveWp", GeoOnSecondaryContainer, GeoSecondaryContainer)
                    DistributionItem("Transition Live", "$transitionLiveWp", GeoOnPrimaryContainer, GeoPrimaryContainer)
                }
            }
        }

        // Recent Audit Activity Section
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT ADMINISTRATIVE AUDIT TRAIL",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 0.8.sp
                )
                TextButton(onClick = { viewModel.navigateTo(AdminScreen.AUDIT_LOGS) }) {
                    Text("View Full Log →", color = GeoPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            if (auditLogs.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.History,
                    title = "No Recent Activity",
                    description = "Administrative actions (such as creating wallpapers, modifying categories, or updating configs) will be logged here."
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, GeoCardBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = GeoSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        auditLogs.take(5).forEach { log ->
                            val timeStr = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(GeoSurfaceVariant)
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = log.action,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = GeoPrimary,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "• by ${log.adminEmail}",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Text(
                                        text = log.details,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = timeStr,
                                    fontSize = 10.sp,
                                    color = TextMuted
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
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    containerColor: Color,
    iconBadgeBg: Color,
    iconColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.85f)
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(iconBadgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = textColor
            )

            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = textColor.copy(alpha = 0.75f)
            )
        }
    }
}

@Composable
private fun DistributionItem(
    label: String,
    count: String,
    countColor: Color,
    containerColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(containerColor)
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = countColor
            )
        }
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium
        )
    }
}

