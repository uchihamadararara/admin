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
import com.example.ui.admin.AdminViewModel
import com.example.ui.theme.*

@Composable
fun UsersSection(viewModel: AdminViewModel) {
    var searchUser by remember { mutableStateOf("") }
    val filteredUsers = viewModel.usersList.filter {
        searchUser.isEmpty() || it.email.contains(searchUser, ignoreCase = true) || it.id.contains(searchUser, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Users Explorer", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("End-user profiles, monetization tier, and security standing", fontSize = 12.sp, color = TextSecondary)
            }
        }

        item {
            OutlinedTextField(
                value = searchUser,
                onValueChange = { searchUser = it },
                placeholder = { Text("Search by user email or ID...", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )
        }

        items(filteredUsers) { user ->
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
                        Column {
                            Text(user.email, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("UID: ${user.id} • Joined: ${user.joinDate}", fontSize = 10.sp, color = TextSecondary)
                        }

                        val tierColor = when (user.tier) {
                            "PRO" -> SkyAccent
                            "LIFETIME" -> StatusWarning
                            else -> TextSecondary
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(tierColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(user.tier, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tierColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ad Reward Credits: ${user.adCredits}", fontSize = 12.sp, color = TextSecondary)
                        OutlinedButton(
                            onClick = { viewModel.toggleUserBan(user.id) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = if (user.isBanned) StatusSuccess else StatusDanger
                            ),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(if (user.isBanned) "Unban Account" else "Ban User", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionsSection(viewModel: AdminViewModel) {
    val totalSubs = viewModel.subscriptionsList.sumOf { it.activeSubscribers }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("Subscriptions (Google Play)", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Google Play Billing products, active subscriptions & RTDN webhook listener", fontSize = 12.sp, color = TextSecondary)
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("TOTAL ACTIVE SUBSCRIBERS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                        Text("$totalSubs Active", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(StatusSuccess.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("RTDN Synced", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }
            }
        }

        item {
            Text("CONFIGURED PLAY BILLING SKUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
        }

        items(viewModel.subscriptionsList) { sub ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(sub.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text("SKU: ${sub.sku} • Price: ${sub.price}", fontSize = 11.sp, color = TextSecondary)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${sub.activeSubscribers} active", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyanPrimary)
                        Text(sub.status, fontSize = 10.sp, color = StatusSuccess)
                    }
                }
            }
        }
    }
}
