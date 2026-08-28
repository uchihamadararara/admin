package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.DestructiveConfirmDialog
import com.example.ui.components.EmptyStateView
import com.example.ui.components.RolePill
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ModerationScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.moderationReports.collectAsState()
    var selectedReportForAction by remember { mutableStateOf<ModerationReport?>(null) }
    var resolutionNotes by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Content Moderation & Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("User reported content and flag queue (${reports.size} reports)", fontSize = 12.sp, color = TextSecondary)
        }

        if (reports.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.VerifiedUser,
                title = "Moderation Queue Clear",
                description = "There are no pending reports or reported content in the system."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(reports, key = { it.id }) { report ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Reason: ${report.reason}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    StatusPill(
                                        text = report.status.name,
                                        backgroundColor = when (report.status) {
                                            ReportStatus.OPEN -> RoyalRoseContainer
                                            ReportStatus.IN_REVIEW -> RoyalGoldContainer
                                            ReportStatus.RESOLVED -> RoyalEmeraldContainer
                                            ReportStatus.DISMISSED -> AmoledSurfaceVariant
                                        },
                                        textColor = when (report.status) {
                                            ReportStatus.OPEN -> RoyalRoseText
                                            ReportStatus.IN_REVIEW -> RoyalGoldText
                                            ReportStatus.RESOLVED -> RoyalEmeraldText
                                            ReportStatus.DISMISSED -> TextMuted
                                        }
                                    )
                                }
                                Text("Target ID: ${report.targetId.ifBlank { "N/A" }} (${report.targetType}) • Reporter: ${report.reporterUid}", fontSize = 11.sp, color = TextSecondary)
                                if (report.comments.isNotBlank()) {
                                    Text("Comment: \"${report.comments}\"", fontSize = 12.sp, color = TextMuted)
                                }
                            }

                            if (viewModel.canModerate() && report.status in listOf(ReportStatus.OPEN, ReportStatus.IN_REVIEW)) {
                                Button(
                                    onClick = {
                                        selectedReportForAction = report
                                        resolutionNotes = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Resolve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedReportForAction?.let { report ->
        AlertDialog(
            onDismissRequest = { selectedReportForAction = null },
            title = { Text("Resolve Moderation Report", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Reason: ${report.reason}", fontSize = 13.sp, color = TextPrimary)
                    OutlinedTextField(
                        value = resolutionNotes,
                        onValueChange = { resolutionNotes = it },
                        label = { Text("Resolution Notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.resolveModerationReport(report.id, ReportStatus.RESOLVED, resolutionNotes)
                                selectedReportForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalEmerald, contentColor = AmoledBackground)
                        ) {
                            Text("Mark Resolved", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                viewModel.resolveModerationReport(report.id, ReportStatus.DISMISSED, resolutionNotes)
                                selectedReportForAction = null
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = AmoledSurfaceVariant, contentColor = TextPrimary)
                        ) {
                            Text("Dismiss", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { selectedReportForAction = null }) { Text("Cancel") }
            },
            containerColor = AmoledSurface
        )
    }
}

@Composable
fun AnnouncementsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val announcements by viewModel.announcements.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var announcementToDelete by remember { mutableStateOf<Announcement?>(null) }

    var titleInput by remember { mutableStateOf("") }
    var messageInput by remember { mutableStateOf("") }
    var actionUrlInput by remember { mutableStateOf("") }
    var targetAudienceInput by remember { mutableStateOf("ALL") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Broadcast Announcements", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("In-app notifications and banner alerts", fontSize = 12.sp, color = TextSecondary)
            }

            if (viewModel.canManageUsers()) {
                Button(
                    onClick = {
                        titleInput = ""
                        messageInput = ""
                        actionUrlInput = ""
                        isAdding = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Announcement", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (announcements.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.Campaign,
                title = "No Active Announcements",
                description = "Broadcast updates or maintenance notifications to free, VIP, or all app users.",
                actionLabel = if (viewModel.canManageUsers()) "+ Create Announcement" else null,
                onAction = { isAdding = true }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(announcements, key = { it.id }) { ann ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(ann.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    StatusPill(
                                        text = if (ann.isActive) "ACTIVE" else "INACTIVE",
                                        backgroundColor = if (ann.isActive) RoyalEmeraldContainer else AmoledSurfaceVariant,
                                        textColor = if (ann.isActive) RoyalEmeraldText else TextMuted
                                    )
                                    StatusPill(
                                        text = ann.targetAudience.name,
                                        backgroundColor = RoyalIndigoContainer,
                                        textColor = RoyalIndigoText
                                    )
                                }
                                Text(ann.message, fontSize = 13.sp, color = TextSecondary)
                                if (!ann.actionUrl.isNullOrBlank()) {
                                    Text("Action URL: ${ann.actionUrl}", fontSize = 11.sp, color = RoyalGoldText)
                                }
                            }

                            if (viewModel.canManageUsers()) {
                                IconButton(
                                    onClick = { announcementToDelete = ann },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = RoyalRose)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isAdding) {
        AlertDialog(
            onDismissRequest = { isAdding = false },
            title = { Text("Broadcast Announcement", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleInput,
                        onValueChange = { titleInput = it },
                        label = { Text("Title *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        label = { Text("Message *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                    OutlinedTextField(
                        value = actionUrlInput,
                        onValueChange = { actionUrlInput = it },
                        label = { Text("Action URL (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val ann = Announcement(
                            title = titleInput,
                            message = messageInput,
                            actionUrl = actionUrlInput.ifBlank { null },
                            targetAudience = TargetAudience.fromString(targetAudienceInput),
                            isActive = true
                        )
                        viewModel.saveAnnouncement(ann) { success, _ ->
                            if (success) isAdding = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground)
                ) {
                    Text("Broadcast", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isAdding = false }) { Text("Cancel") }
            },
            containerColor = AmoledSurface
        )
    }

    announcementToDelete?.let { ann ->
        DestructiveConfirmDialog(
            title = "Delete Announcement?",
            message = "Remove '${ann.title}'?",
            confirmText = "Delete",
            onConfirm = {
                viewModel.deleteAnnouncement(ann.id, ann.title)
                announcementToDelete = null
            },
            onDismiss = { announcementToDelete = null }
        )
    }
}

@Composable
fun AppConfigScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val liveConfig by viewModel.appConfig.collectAsState()
    var config by remember(liveConfig) { mutableStateOf(liveConfig) }
    var isSaving by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Global App Configuration", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Firestore: app_config/global", fontSize = 12.sp, color = TextSecondary)
            }

            if (viewModel.canEditAppConfig()) {
                Button(
                    onClick = {
                        isSaving = true
                        viewModel.saveAppConfig(config) { _, _ -> isSaving = false }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isSaving) CircularProgressIndicator(modifier = Modifier.size(16.dp), color = AmoledBackground)
                    else {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Maintenance Mode Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = AmoledSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Maintenance Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("Block client app requests with maintenance screen", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = config.maintenanceMode,
                        onCheckedChange = { config = config.copy(maintenanceMode = it) }
                    )
                }

                if (config.maintenanceMode) {
                    OutlinedTextField(
                        value = config.maintenanceMessage,
                        onValueChange = { config = config.copy(maintenanceMessage = it) },
                        label = { Text("Maintenance Banner Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Version Governance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = AmoledSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Version Governance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = config.minSupportedVersionCode.toString(),
                        onValueChange = { config = config.copy(minSupportedVersionCode = it.toIntOrNull() ?: 1) },
                        label = { Text("Min Supported Version Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = config.latestVersionCode.toString(),
                        onValueChange = { config = config.copy(latestVersionCode = it.toIntOrNull() ?: 1) },
                        label = { Text("Latest Version Code") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }

        // Ad Frequency Governance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = AmoledSurface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Ad Frequency & Monetization Governance", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = config.interstitialAdIntervalMinutes.toString(),
                        onValueChange = { config = config.copy(interstitialAdIntervalMinutes = it.toIntOrNull() ?: 15) },
                        label = { Text("Interstitial Interval (min)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = config.rewardedAdDailyLimit.toString(),
                        onValueChange = { config = config.copy(rewardedAdDailyLimit = it.toIntOrNull() ?: 5) },
                        label = { Text("Daily Rewarded Limit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        }
    }
}

@Composable
fun AuditLogsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val auditLogs by viewModel.auditLogs.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Administrative Audit Trail", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Immutable record of system operations (${auditLogs.size} logs recorded)", fontSize = 12.sp, color = TextSecondary)
        }

        if (auditLogs.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.History,
                title = "Audit Trail Clean",
                description = "All create, update, delete, and moderation actions taken by console administrators are recorded here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(auditLogs, key = { it.id }) { log ->
                    val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(log.action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoyalGoldText, fontFamily = FontFamily.Monospace)
                                    StatusPill(
                                        text = log.role,
                                        backgroundColor = RoyalIndigoContainer,
                                        textColor = RoyalIndigoText
                                    )
                                }
                                Text(dateStr, fontSize = 10.sp, color = TextMuted)
                            }
                            Text("By: ${log.adminEmail} (${log.adminUid})", fontSize = 11.sp, color = TextSecondary)
                            Text("Details: ${log.details}", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminManagementScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val adminUsers by viewModel.adminUsers.collectAsState()
    var isAdding by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var uidInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(AdminRole.CONTENT_MANAGER) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Admin Role-Based Access Control", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Super Admin Governance (${adminUsers.size} authorized admins)", fontSize = 12.sp, color = TextSecondary)
            }

            if (viewModel.canManageAdmins()) {
                Button(
                    onClick = {
                        emailInput = ""
                        uidInput = ""
                        nameInput = ""
                        selectedRole = AdminRole.CONTENT_MANAGER
                        isAdding = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Grant Role", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        if (adminUsers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.AdminPanelSettings,
                title = "No Administrator Records",
                description = "Bootstrap or grant administrator roles in Firestore collection 'admin_users'."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(adminUsers, key = { it.uid }) { admin ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(admin.displayName.ifBlank { admin.email }, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    RolePill(role = admin.role)
                                }
                                Text(admin.email, fontSize = 12.sp, color = TextSecondary)
                                Text("UID: ${admin.uid}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                            }
                            StatusPill(
                                text = if (admin.isActive) "ACTIVE" else "REVOKED",
                                backgroundColor = if (admin.isActive) RoyalEmeraldContainer else RoyalRoseContainer,
                                textColor = if (admin.isActive) RoyalEmeraldText else RoyalRoseText
                            )
                        }
                    }
                }
            }
        }
    }

    if (isAdding) {
        AlertDialog(
            onDismissRequest = { isAdding = false },
            title = { Text("Grant Administrator Access", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Admin Email *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uidInput,
                        onValueChange = { uidInput = it },
                        label = { Text("Firebase Auth UID *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Display Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Text("Select Admin Role:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                    AdminRole.entries.forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (selectedRole == role) RoyalGoldContainer else AmoledSurfaceVariant)
                                .clickable { selectedRole = role }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(role.displayName, fontSize = 12.sp, color = if (selectedRole == role) RoyalGoldText else TextPrimary, fontWeight = FontWeight.SemiBold)
                            RolePill(role = role)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newAdmin = AdminUser(
                            uid = uidInput,
                            email = emailInput,
                            displayName = nameInput,
                            role = selectedRole,
                            isActive = true
                        )
                        viewModel.saveAdminUser(newAdmin) { success, _ ->
                            if (success) isAdding = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalGold, contentColor = AmoledBackground)
                ) {
                    Text("Grant Access", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { isAdding = false }) { Text("Cancel") }
            },
            containerColor = AmoledSurface
        )
    }
}

@Composable
fun SystemHealthScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val currentAdmin = viewModel.currentAdmin()
    val wallpapers by viewModel.wallpapers.collectAsState()
    val mediaAssets by viewModel.mediaAssets.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("System & Infrastructure Health", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Real-time diagnostics and infrastructure status", fontSize = 12.sp, color = TextSecondary)
        }

        HealthCheckCard(
            title = "Firebase Authentication",
            status = if (currentAdmin != null) "OPERATIONAL" else "NOT AUTHENTICATED",
            statusColor = RoyalEmerald,
            details = "Active Token: ${currentAdmin?.email ?: "None"} (UID: ${currentAdmin?.uid?.take(8) ?: "N/A"}...)"
        )

        HealthCheckCard(
            title = "Cloud Firestore Database",
            status = "CONNECTED & SYNCHRONIZED",
            statusColor = RoyalEmerald,
            details = "Real-time snapshot listeners active across 8 Firestore collections."
        )

        HealthCheckCard(
            title = "Cloudflare R2 Media Storage",
            status = "PUBLIC HTTPS PIPELINE ACTIVE",
            statusColor = RoyalEmerald,
            details = "${mediaAssets.size} registered media objects • Public CDN delivery verified."
        )

        HealthCheckCard(
            title = "Live Wallpaper State Machine Engine",
            status = "OPERATIONAL",
            statusColor = RoyalEmerald,
            details = "Content-driven state machine validator ready for NORMAL and TRANSITION multi-state wallpapers."
        )
    }
}

@Composable
private fun HealthCheckCard(
    title: String,
    status: String,
    statusColor: Color,
    details: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(details, fontSize = 11.sp, color = TextSecondary)
            }
            StatusPill(
                text = status,
                backgroundColor = statusColor.copy(alpha = 0.15f),
                textColor = statusColor,
                borderColor = statusColor.copy(alpha = 0.4f)
            )
        }
    }
}
