package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdminRole
import com.example.data.model.AdminUser
import com.example.ui.theme.*
import com.example.viewmodel.AdminScreen

data class NavItem(
    val screen: AdminScreen,
    val label: String,
    val icon: ImageVector,
    val requiredMinRole: AdminRole = AdminRole.SUPPORT
)

@Composable
fun ConsoleDrawerContent(
    currentScreen: AdminScreen,
    currentAdmin: AdminUser?,
    onNavigate: (AdminScreen) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val role = currentAdmin?.role ?: AdminRole.SUPPORT

    val coreItems = listOf(
        NavItem(AdminScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard, AdminRole.SUPPORT),
        NavItem(AdminScreen.WALLPAPERS, "Wallpapers", Icons.Default.Wallpaper, AdminRole.CONTENT_MANAGER),
        NavItem(AdminScreen.MEDIA_LIBRARY, "Media Library", Icons.Default.PermMedia, AdminRole.CONTENT_MANAGER),
        NavItem(AdminScreen.CATEGORIES, "Categories", Icons.Default.Category, AdminRole.CONTENT_MANAGER),
        NavItem(AdminScreen.TAGS, "Tags", Icons.Default.Tag, AdminRole.CONTENT_MANAGER)
    )

    val monetizationItems = listOf(
        NavItem(AdminScreen.USERS, "Users", Icons.Default.People, AdminRole.SUPPORT),
        NavItem(AdminScreen.SUBSCRIPTIONS, "Subscriptions", Icons.Default.CardMembership, AdminRole.ADMIN),
        NavItem(AdminScreen.ADMOB_SSV, "AdMob / SSV", Icons.Default.MonetizationOn, AdminRole.ADMIN)
    )

    val safetyItems = listOf(
        NavItem(AdminScreen.MODERATION, "Moderation", Icons.Default.Report, AdminRole.MODERATOR),
        NavItem(AdminScreen.ANNOUNCEMENTS, "Announcements", Icons.Default.Campaign, AdminRole.ADMIN)
    )

    val governanceItems = listOf(
        NavItem(AdminScreen.APP_CONFIG, "App Configuration", Icons.Default.Tune, AdminRole.ADMIN),
        NavItem(AdminScreen.AUDIT_LOGS, "Audit Logs", Icons.Default.History, AdminRole.ADMIN),
        NavItem(AdminScreen.ADMIN_MANAGEMENT, "Admin Management", Icons.Default.AdminPanelSettings, AdminRole.SUPER_ADMIN)
    )

    val systemItems = listOf(
        NavItem(AdminScreen.SYSTEM_HEALTH, "Settings / Health", Icons.Default.HealthAndSafety, AdminRole.SUPPORT)
    )

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(GeoBackground)
            .border(width = 1.dp, color = GeoCardBorder)
            .padding(16.dp)
    ) {
        // Brand Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GeoPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = GeoOnPrimaryContainer,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = "ROYAL CONSOLE",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Geometric Balance Admin",
                    fontSize = 11.sp,
                    color = GeoPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        HorizontalDivider(color = GeoOutlineVariant, modifier = Modifier.padding(bottom = 12.dp))

        // Navigation Lists
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            NavGroupSection(
                groupTitle = "CORE",
                items = coreItems,
                currentScreen = currentScreen,
                currentRole = role,
                onNavigate = onNavigate
            )

            NavGroupSection(
                groupTitle = "USERS & MONETIZATION",
                items = monetizationItems,
                currentScreen = currentScreen,
                currentRole = role,
                onNavigate = onNavigate
            )

            NavGroupSection(
                groupTitle = "CONTENT & SAFETY",
                items = safetyItems,
                currentScreen = currentScreen,
                currentRole = role,
                onNavigate = onNavigate
            )

            NavGroupSection(
                groupTitle = "SYSTEM & GOVERNANCE",
                items = governanceItems,
                currentScreen = currentScreen,
                currentRole = role,
                onNavigate = onNavigate
            )

            NavGroupSection(
                groupTitle = "SYSTEM",
                items = systemItems,
                currentScreen = currentScreen,
                currentRole = role,
                onNavigate = onNavigate
            )
        }

        HorizontalDivider(color = GeoOutlineVariant, modifier = Modifier.padding(vertical = 12.dp))

        // Sign Out Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(GeoRoseContainer.copy(alpha = 0.6f))
                .clickable { onSignOut() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
                tint = GeoRose,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = "Sign Out",
                color = GeoRoseText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NavGroupSection(
    groupTitle: String,
    items: List<NavItem>,
    currentScreen: AdminScreen,
    currentRole: AdminRole,
    onNavigate: (AdminScreen) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = groupTitle,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
        )

        items.forEach { item ->
            val isAccessible = currentRole.level >= item.requiredMinRole.level
            val isSelected = currentScreen == item.screen

            val bg = when {
                isSelected -> GeoSecondaryContainer
                else -> Color.Transparent
            }
            val contentColor = when {
                !isAccessible -> TextMuted.copy(alpha = 0.4f)
                isSelected -> GeoOnSecondaryContainer
                else -> TextSecondary
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(bg)
                    .clickable(enabled = isAccessible) { onNavigate(item.screen) }
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) GeoPrimaryContainer else Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = if (isSelected) GeoOnPrimaryContainer else contentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = item.label,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }

                if (!isAccessible) {
                    Text(
                        text = item.requiredMinRole.displayName,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

