package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusPill
import com.example.ui.theme.*
import com.example.viewmodel.AdminViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun UsersScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val users by viewModel.users.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForStatus by remember { mutableStateOf<UserProfile?>(null) }

    val filteredUsers = users.filter { u ->
        searchQuery.isBlank() || u.email.contains(searchQuery, ignoreCase = true) || u.uid.contains(searchQuery, ignoreCase = true) || u.displayName.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("User Management", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("${users.size} registered app accounts in Firestore", fontSize = 12.sp, color = TextSecondary)
            }
        }

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by email, name or UID...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RoyalGold,
                unfocusedBorderColor = AmoledCardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        if (users.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.People,
                title = "No Users Registered Yet",
                description = "User profiles are automatically recorded to Cloud Firestore when users sign in via Google Sign-In on the client app."
            )
        } else if (filteredUsers.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.PersonSearch,
                title = "No Users Matching Query",
                description = "No user found matching '$searchQuery'."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredUsers, key = { it.uid }) { user ->
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
                                    Text(
                                        text = user.displayName.ifBlank { "User" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    StatusPill(
                                        text = user.subscriptionTier.name,
                                        backgroundColor = if (user.subscriptionTier == SubscriptionTier.VIP) RoyalGoldContainer else AmoledSurfaceVariant,
                                        textColor = if (user.subscriptionTier == SubscriptionTier.VIP) RoyalGoldText else TextSecondary
                                    )
                                    StatusPill(
                                        text = user.accountStatus.name,
                                        backgroundColor = when (user.accountStatus) {
                                            AccountStatus.ACTIVE -> RoyalEmeraldContainer
                                            AccountStatus.SUSPENDED -> RoyalRoseContainer
                                            AccountStatus.BANNED -> RoyalRoseContainer
                                        },
                                        textColor = when (user.accountStatus) {
                                            AccountStatus.ACTIVE -> RoyalEmeraldText
                                            else -> RoyalRoseText
                                        }
                                    )
                                }

                                Text(user.email, fontSize = 12.sp, color = TextSecondary)
                                Text("UID: ${user.uid}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextMuted)
                                if (!user.currentAppliedWallpaperTitle.isNullOrBlank()) {
                                    Text("Applied Wallpaper: ${user.currentAppliedWallpaperTitle}", fontSize = 11.sp, color = RoyalGoldText)
                                }
                            }

                            if (viewModel.canManageUsers()) {
                                Button(
                                    onClick = { selectedUserForStatus = user },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AmoledSurfaceVariant,
                                        contentColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("Moderate", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedUserForStatus?.let { user ->
        AlertDialog(
            onDismissRequest = { selectedUserForStatus = null },
            title = { Text("Moderate User: ${user.displayName.ifBlank { user.email }}", fontWeight = FontWeight.Bold, color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Account Status:", fontSize = 13.sp, color = TextSecondary)
                    AccountStatus.entries.forEach { status ->
                        Button(
                            onClick = {
                                viewModel.updateUserStatus(user.uid, status)
                                selectedUserForStatus = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (user.accountStatus == status) RoyalGold else AmoledSurfaceVariant,
                                contentColor = if (user.accountStatus == status) AmoledBackground else TextPrimary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(status.name, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { selectedUserForStatus = null }) { Text("Cancel") }
            },
            containerColor = AmoledSurface
        )
    }
}

@Composable
fun SubscriptionsScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val billingEvents by viewModel.billingEvents.collectAsState()

    val canonicalSkus = listOf(
        SubscriptionProduct("vip_3days", "3-Day VIP Pass", "$0.99", "3 Days"),
        SubscriptionProduct("vip_7days", "Weekly VIP", "$1.99", "7 Days"),
        SubscriptionProduct("vip_14days", "Bi-Weekly VIP", "$3.49", "14 Days"),
        SubscriptionProduct("vip_1month", "Monthly VIP Access", "$5.99", "1 Month"),
        SubscriptionProduct("vip_lifetime", "Lifetime VIP Royalty", "$19.99", "Lifetime")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Subscription Governance", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Canonical Google Play Billing Products & Purchase Verifications", fontSize = 12.sp, color = TextSecondary)
        }

        // Canonical SKUs Grid
        Text(
            text = "CANONICAL IN-APP PRODUCTS (5 GOOGLE PLAY SKUS)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalGold,
            letterSpacing = 0.8.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            canonicalSkus.take(3).forEach { sku ->
                SkuCard(sku = sku, modifier = Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            canonicalSkus.drop(3).forEach { sku ->
                SkuCard(sku = sku, modifier = Modifier.weight(1f))
            }
        }

        HorizontalDivider(color = AmoledCardBorder)

        // Real Billing Events Section
        Text(
            text = "PURCHASE & BILLING EVENTS (LIVE FIRESTORE)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.8.sp
        )

        if (billingEvents.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.CardMembership,
                title = "No Billing Events Yet",
                description = "Live purchase tokens and subscription receipts verified by your backend will appear here."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(billingEvents, key = { it.id }) { event ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(event.sku, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalGoldText)
                                Text("User: ${event.uid} • Order: ${event.orderId}", fontSize = 11.sp, color = TextSecondary)
                                Text("State: ${event.verificationState.name} • Purchased: ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(event.purchaseTime))}", fontSize = 10.sp, color = TextMuted)
                            }
                            StatusPill(
                                text = event.verificationState.name,
                                backgroundColor = if (event.verificationState == VerificationState.VERIFIED) RoyalEmeraldContainer else AmoledSurfaceVariant,
                                textColor = if (event.verificationState == VerificationState.VERIFIED) RoyalEmeraldText else TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class SubscriptionProduct(val sku: String, val title: String, val price: String, val duration: String)

@Composable
private fun SkuCard(sku: SubscriptionProduct, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, AmoledCardBorder, RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(sku.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("SKU: ${sku.sku}", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = RoyalGold)
            Text("${sku.price} • ${sku.duration}", fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
fun AdMobSSVScreen(
    viewModel: AdminViewModel,
    modifier: Modifier = Modifier
) {
    val ssvEvents by viewModel.ssvEvents.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AmoledBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("AdMob Server-Side Verification (SSV)", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text("Telemetry for rewarded ad verification callbacks (${ssvEvents.size} events logged)", fontSize = 12.sp, color = TextSecondary)
        }

        if (ssvEvents.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.MonetizationOn,
                title = "No SSV Callbacks Logged",
                description = "When users complete rewarded video ads in the mobile app, verification pings from Google AdMob SSV endpoints are logged in this collection."
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ssvEvents, key = { it.id }) { ssv ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, AmoledCardBorder, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = AmoledSurface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Reward: ${ssv.rewardAmount} ${ssv.rewardType}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RoyalGoldText)
                                Text("User: ${ssv.uid} • Data: ${ssv.customData ?: ssv.id.take(8)}", fontSize = 11.sp, color = TextSecondary)
                            }
                            val isVerified = ssv.verificationState == VerificationState.VERIFIED
                            StatusPill(
                                text = ssv.verificationState.name,
                                backgroundColor = if (isVerified) RoyalEmeraldContainer else RoyalRoseContainer,
                                textColor = if (isVerified) RoyalEmeraldText else RoyalRoseText
                            )
                        }
                    }
                }
            }
        }
    }
}
