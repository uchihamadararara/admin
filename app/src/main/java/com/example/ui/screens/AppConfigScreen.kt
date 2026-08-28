package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Announcement
import com.example.data.model.AppConfig
import com.example.data.repository.AdminRepository
import com.example.ui.components.StatusBadge
import com.example.ui.components.StatusBadgeType
import com.example.ui.theme.*

@Composable
fun AppConfigScreen(
    repository: AdminRepository
) {
    val config by repository.appConfig.collectAsState()
    val announcements by repository.announcements.collectAsState()

    var minVersion by remember { mutableStateOf(config.minAppVersion) }
    var recommendedVersion by remember { mutableStateOf(config.recommendedAppVersion) }
    var maintenanceMode by remember { mutableStateOf(config.maintenanceMode) }
    var maintenanceMsg by remember { mutableStateOf(config.maintenanceMessage) }
    var showSavedMessage by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REMOTE APP CONFIGURATION",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Global app behavior, force update versions, and live broadcast banners",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = {
                        repository.updateAppConfig(
                            config.copy(
                                minAppVersion = minVersion,
                                recommendedAppVersion = recommendedVersion,
                                maintenanceMode = maintenanceMode,
                                maintenanceMessage = maintenanceMsg
                            )
                        )
                        showSavedMessage = true
                    },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ChampagneGold, contentColor = ObsidianCanvas)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Config", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                }
            }
        }

        if (showSavedMessage) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = StatusSuccessBgDark),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccessDark)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Configuration changes propagated successfully.", style = MaterialTheme.typography.bodySmall, color = StatusSuccessDark)
                    }
                }
            }
        }

        // Version & Rollout Control
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("1. ANDROID APP VERSION ENFORCEMENT", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = minVersion,
                            onValueChange = { minVersion = it },
                            label = { Text("Minimum Required Version") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = recommendedVersion,
                            onValueChange = { recommendedVersion = it },
                            label = { Text("Recommended Version") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Emergency Maintenance Mode
        item {
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
                            Text("2. MAINTENANCE MODE", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)
                            Text("Puts client app into maintenance mode with safe local fallback.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = maintenanceMode, onCheckedChange = { maintenanceMode = it })
                    }

                    if (maintenanceMode) {
                        OutlinedTextField(
                            value = maintenanceMsg,
                            onValueChange = { maintenanceMsg = it },
                            label = { Text("Maintenance Message Displayed to Users") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Active Broadcast Announcements
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("3. BROADCAST ANNOUNCEMENTS", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ChampagneGold)
                    announcements.forEach { ann ->
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = ann.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                StatusBadge(text = if (ann.isActive) "ACTIVE" else "INACTIVE", type = if (ann.isActive) StatusBadgeType.SUCCESS else StatusBadgeType.NEUTRAL)
                            }
                            Text(text = ann.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "Valid: ${ann.startDate.substringBefore("T")} to ${ann.endDate.substringBefore("T")} · Target: ${ann.targetAudience}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
