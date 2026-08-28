package com.example.ui.admin.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.model.InAppAnnouncement
import com.example.ui.admin.AdminViewModel
import com.example.ui.theme.*

@Composable
fun AdMobSection(viewModel: AdminViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("AdMob / SSV Events", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Ad unit configuration & cryptographic Server-Side Verification (SSV) logs", fontSize = 12.sp, color = TextSecondary)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ADMOB INTEGRATION SETTINGS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                    Text("App ID: ca-app-pub-3940256099942544~3347511713", fontSize = 12.sp, color = TextPrimary)
                    Text("Rewarded Ad Unit: ca-app-pub-3940256099942544/5224354917", fontSize = 11.sp, color = TextSecondary)
                    Text("Interstitial Ad Unit: ca-app-pub-3940256099942544/1033173712", fontSize = 11.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.showToast("Dispatched test SSV callback validation (Signature: Valid ECDSA)") },
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = CyanPrimary)
                    ) {
                        Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test SSV Callback Signature", fontSize = 12.sp)
                    }
                }
            }
        }

        item {
            Text("RECENT SSV REWARD VERIFICATIONS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("✓ tx_89124 • User: usr_001 • +5 Credits Granted", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = StatusSuccess)
                    Text("Key ID: 39402 • Sig: MEUCIQCY2q... (Verified)", fontSize = 10.sp, color = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun ModerationSection(viewModel: AdminViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Moderation Queue", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Review reported content and safety compliance", fontSize = 12.sp, color = TextSecondary)
            }
        }

        if (viewModel.moderationReportsList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Pending Reports", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("All user feedback and flagged content resolved.", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        } else {
            items(viewModel.moderationReportsList) { report ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(report.wallpaperTitle, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(StatusWarning.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(report.status, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusWarning)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Reason: ${report.reason}", fontSize = 12.sp, color = TextPrimary)
                        Text("Reported by: ${report.reporterEmail} • ${report.timestamp}", fontSize = 10.sp, color = TextSecondary)

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.dismissReport(report.id) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Dismiss", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnnouncementsSection(viewModel: AdminViewModel) {
    var isAddDialogOpen by remember { mutableStateOf(false) }
    var annTitle by remember { mutableStateOf("") }
    var annMsg by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Announcements", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("In-app broadcast banners & push notices", fontSize = 12.sp, color = TextSecondary)
                }
                Button(
                    onClick = { isAddDialogOpen = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Notice", fontSize = 12.sp)
                }
            }
        }

        items(viewModel.announcementsList) { ann ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(ann.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ann.message, fontSize = 12.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Audience: ${ann.targetAudience} • Expires: ${ann.expiresAt}", fontSize = 10.sp, color = TextMuted)
                }
            }
        }
    }

    if (isAddDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAddDialogOpen = false },
            title = { Text("Publish Announcement", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = annTitle,
                        onValueChange = { annTitle = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = annMsg,
                        onValueChange = { annMsg = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (annTitle.isNotEmpty()) {
                            viewModel.addAnnouncement(
                                InAppAnnouncement(
                                    id = "ann_" + System.currentTimeMillis().toString().takeLast(4),
                                    title = annTitle,
                                    message = annMsg,
                                    expiresAt = "2026-10-30"
                                )
                            )
                            isAddDialogOpen = false
                            annTitle = ""
                            annMsg = ""
                        }
                    }
                ) { Text("Broadcast") }
            },
            dismissButton = { TextButton(onClick = { isAddDialogOpen = false }) { Text("Cancel") } }
        )
    }
}
