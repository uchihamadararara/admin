package com.example.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.ui.admin.sections.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(viewModel: AdminViewModel) {
    if (!viewModel.isAuthenticated) {
        AdminAuthScreen(viewModel = viewModel)
        return
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Display toast if present
    LaunchedEffect(viewModel.toastMessage) {
        viewModel.toastMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissToast()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = DarkSurface,
                drawerContentColor = TextPrimary,
                modifier = Modifier.width(300.dp)
            ) {
                AdminDrawerContent(
                    viewModel = viewModel,
                    onSectionSelected = { section ->
                        viewModel.currentSection = section
                        viewModel.isEditorOpen = false
                        viewModel.isPreviewSimulatorOpen = false
                        coroutineScope.launch { drawerState.close() }
                    },
                    onCloseDrawer = {
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyanPrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = viewModel.currentSection.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "WALLPAPER ADMIN CONSOLE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = CyanPrimary
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("admin_drawer_toggle_button")
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Drawer", tint = TextPrimary)
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(RoleBadgeBg)
                                .border(1.dp, RoleBadgeBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(CyanPrimary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = viewModel.currentAdmin.role.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = DarkSurface,
                        titleContentColor = TextPrimary
                    )
                )
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
            containerColor = DarkBg
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (viewModel.currentSection) {
                    AdminSection.DASHBOARD -> DashboardSection(viewModel = viewModel)
                    AdminSection.WALLPAPERS -> WallpapersSection(viewModel = viewModel)
                    AdminSection.MEDIA_LIBRARY -> MediaLibrarySection(viewModel = viewModel)
                    AdminSection.CATEGORIES -> CategoriesSection(viewModel = viewModel)
                    AdminSection.TAGS -> TagsSection(viewModel = viewModel)
                    AdminSection.USERS -> UsersSection(viewModel = viewModel)
                    AdminSection.SUBSCRIPTIONS -> SubscriptionsSection(viewModel = viewModel)
                    AdminSection.ADMOB_SSV -> AdMobSection(viewModel = viewModel)
                    AdminSection.MODERATION -> ModerationSection(viewModel = viewModel)
                    AdminSection.ANNOUNCEMENTS -> AnnouncementsSection(viewModel = viewModel)
                    AdminSection.APP_CONFIG -> AppConfigSection(viewModel = viewModel)
                    AdminSection.AUDIT_LOGS -> AuditLogsSection(viewModel = viewModel)
                    AdminSection.ADMIN_MANAGEMENT -> AdminManagementSection(viewModel = viewModel)
                    AdminSection.SETTINGS -> SettingsHealthSection(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminDrawerContent(
    viewModel: AdminViewModel,
    onSectionSelected: (AdminSection) -> Unit,
    onCloseDrawer: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 12.dp)
    ) {
        // Drawer Header with Profile
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "WALLPAPER ADMIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onCloseDrawer) {
                        Icon(Icons.Default.Close, contentDescription = "Close Drawer", tint = TextSecondary)
                    }
                }
                Text(
                    text = viewModel.currentAdmin.email,
                    fontSize = 12.sp,
                    color = CyanPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = BorderSubtle)
            }
        }

        // Section Groups
        val groups = listOf("CORE CATALOG", "USERS & MONETIZATION", "SYSTEM & GOVERNANCE")
        groups.forEach { groupName ->
            item {
                Text(
                    text = groupName,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            val groupSections = AdminSection.values().filter { it.group == groupName }
            items(groupSections) { section ->
                val isSelected = viewModel.currentSection == section
                val icon = getSectionIcon(section)

                NavigationDrawerItem(
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) CyanPrimary else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = section.title,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    },
                    selected = isSelected,
                    onClick = { onSectionSelected(section) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = CyanPrimary.copy(alpha = 0.12f),
                        unselectedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 2.dp)
                        .testTag("nav_item_${section.name.lowercase()}")
                )
            }
        }

        // Sign Out Option at Bottom
        item {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = BorderSubtle, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            NavigationDrawerItem(
                icon = {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = StatusDanger, modifier = Modifier.size(20.dp))
                },
                label = {
                    Text("Sign Out", fontSize = 13.sp, color = StatusDanger, fontWeight = FontWeight.SemiBold)
                },
                selected = false,
                onClick = {
                    viewModel.isAuthenticated = false
                    viewModel.showToast("Signed out successfully")
                    onCloseDrawer()
                },
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

fun getSectionIcon(section: AdminSection): ImageVector = when (section) {
    AdminSection.DASHBOARD -> Icons.Default.Dashboard
    AdminSection.WALLPAPERS -> Icons.Default.Wallpaper
    AdminSection.MEDIA_LIBRARY -> Icons.Default.CloudQueue
    AdminSection.CATEGORIES -> Icons.Default.Folder
    AdminSection.TAGS -> Icons.Default.LocalOffer
    AdminSection.USERS -> Icons.Default.People
    AdminSection.SUBSCRIPTIONS -> Icons.Default.CreditCard
    AdminSection.ADMOB_SSV -> Icons.Default.Bolt
    AdminSection.MODERATION -> Icons.Default.ReportProblem
    AdminSection.ANNOUNCEMENTS -> Icons.Default.Campaign
    AdminSection.APP_CONFIG -> Icons.Default.Tune
    AdminSection.AUDIT_LOGS -> Icons.Default.History
    AdminSection.ADMIN_MANAGEMENT -> Icons.Default.AdminPanelSettings
    AdminSection.SETTINGS -> Icons.Default.Settings
}
