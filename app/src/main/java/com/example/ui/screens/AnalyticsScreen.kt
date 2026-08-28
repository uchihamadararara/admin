package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.AdminRepository
import com.example.ui.components.MetricCard
import com.example.ui.theme.*

data class OemStat(val brand: String, val fraction: Float, val label: String)

@Composable
fun AnalyticsScreen(
    repository: AdminRepository
) {
    val metrics by repository.metrics.collectAsState()
    val wallpapers by repository.wallpapers.collectAsState()
    val users by repository.users.collectAsState()

    val oemDistribution = remember(users) {
        if (users.isEmpty()) {
            emptyList<OemStat>()
        } else {
            val total = users.size.toFloat()
            users.groupBy { it.oemBrand.ifBlank { "Unknown Device" } }
                .map { (brand, list) ->
                    val frac = list.size / total
                    OemStat(brand, frac, "${(frac * 100).toInt()}% (${list.size})")
                }
                .sortedByDescending { it.fraction }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PLATFORM ANALYTICS & TELEMETRY",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Real telemetry collected from client applications without mock estimations",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard(
                    title = "Live Wallpaper Ratio",
                    value = if (metrics.totalWallpapers > 0) "${(metrics.liveWallpapers * 100 / metrics.totalWallpapers)}%" else "0%",
                    subtitle = "${metrics.liveWallpapers} of ${metrics.totalWallpapers} total items",
                    icon = Icons.Default.VideoLibrary,
                    accentColor = ChampagneGold,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Premium Catalog Share",
                    value = if (metrics.totalWallpapers > 0) "${(metrics.premiumWallpapers * 100 / metrics.totalWallpapers)}%" else "0%",
                    subtitle = "${metrics.premiumWallpapers} Premium · ${metrics.freeWallpapers} Free",
                    icon = Icons.Default.Star,
                    accentColor = StatusSuccessDark,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // OEM Distribution & Platform Compatibility Telemetry
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "OEM ACTIVE DEVICE DISTRIBUTION",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ChampagneGold
                    )
                    if (oemDistribution.isEmpty()) {
                        Text(
                            text = "No user devices recorded yet. Telemetry will automatically populate as devices connect.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        oemDistribution.forEach { stat ->
                            ProgressBarWithLabel(stat.brand, stat.fraction, stat.label)
                        }
                    }
                }
            }
        }

        // Top Wallpapers Performance Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "MOST APPLIED WALLPAPERS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (wallpapers.isEmpty()) {
                        Text(
                            text = "No wallpaper applies logged yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        wallpapers.sortedByDescending { it.appliesCount }.take(5).forEach { wp ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = wp.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                                    Text(text = "${wp.category} · ${wp.accessType.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "${wp.appliesCount} applies",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = ChampagneGoldLight
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
private fun ProgressBarWithLabel(label: String, fraction: Float, valueLabel: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Text(text = valueLabel, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = ChampagneGoldLight)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(ChampagneGold)
            )
        }
    }
}
