package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun UsersAndSubscriptionsScreen(
    repository: AdminRepository
) {
    val users by repository.users.collectAsState()
    val playEvents by repository.googlePlayEvents.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Users, 1 = Google Play Events
    var searchQuery by remember { mutableStateOf("") }

    val filteredUsers = users.filter { u ->
        u.email.contains(searchQuery, ignoreCase = true) ||
                u.id.contains(searchQuery, ignoreCase = true) ||
                u.oemBrand.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "USERS & MONETIZATION",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Google Play authoritative billing & user entitlement inspection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = ChampagneGold
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Registered Users (${users.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Google Play & RTDN Logs (${playEvents.size})") }
            )
        }

        if (selectedTab == 0) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by email, User ID, OEM device...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        title = "No Users Found",
                        description = if (searchQuery.isNotBlank()) "No users match your query '$searchQuery'." else "No registered users recorded in the database yet."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredUsers, key = { it.id }) { u ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = u.email,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadge(
                                            text = u.accountStatus,
                                            type = if (u.accountStatus == "ACTIVE") StatusBadgeType.SUCCESS else StatusBadgeType.DANGER
                                        )
                                    }
                                    StatusBadge(
                                        text = u.subscriptionPlan.displayName,
                                        type = if (u.isPremium) StatusBadgeType.GOLD else StatusBadgeType.NEUTRAL
                                    )
                                }

                                Text(
                                    text = "User ID: ${u.id} · Device: ${u.oemBrand} · App v${u.appVersion}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (u.isPremium && u.subscriptionExpiresAt != null) {
                                    Text(
                                        text = "Premium Valid Until: ${u.subscriptionExpiresAt.substringBefore("T")}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = ChampagneGoldLight
                                    )
                                }

                                if (u.currentAppliedWallpaperId != null) {
                                    Text(
                                        text = "Active Applied Wallpaper ID: ${u.currentAppliedWallpaperId}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (playEvents.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyStateCard(
                        title = "No Google Play Events",
                        description = "Real-time Developer Notifications (RTDN) and in-app purchase receipts will appear here once received."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(playEvents, key = { it.id }) { event ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = CardDefaults.outlinedCardBorder()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusBadge(text = event.eventType, type = StatusBadgeType.GOLD)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = event.basePlanId,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    StatusBadge(text = event.processingStatus, type = StatusBadgeType.SUCCESS)
                                }
                                Text(
                                    text = "Order: ${event.orderId ?: "N/A (RTDN)"} · User: ${event.userEmail ?: event.userId ?: "Anonymous"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Token Hash: ${event.purchaseTokenHash} · Time: ${event.eventTime.substringBefore("Z")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
