package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Wallpaper
import com.example.data.repository.AdminRepository
import com.example.ui.components.AdminRolePill
import com.example.ui.components.WallpaperPreviewModal
import com.example.ui.screens.*
import com.example.ui.theme.ChampagneGold
import com.example.ui.theme.LiveWallpaperAdminTheme
import com.example.ui.theme.ObsidianCanvas
import kotlinx.coroutines.launch

enum class AdminNavigationItem(val route: String, val title: String, val icon: ImageVector) {
    DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
    WALLPAPERS("wallpapers", "Wallpapers", Icons.Default.Wallpaper),
    MEDIA("media", "Media R2", Icons.Default.CloudQueue),
    CATEGORIES("categories", "Categories & Tags", Icons.Default.Category),
    USERS("users", "Users & Subs", Icons.Default.People),
    REWARDS("rewards", "Rewarded SSV", Icons.Default.MonetizationOn),
    MODERATION("moderation", "Moderation", Icons.Default.Shield),
    ANALYTICS("analytics", "Analytics", Icons.Default.BarChart),
    CONFIG("config", "App Config", Icons.Default.Settings),
    ADMINS("admins", "Admin Team", Icons.Default.SupervisorAccount),
    HEALTH("health", "System Health", Icons.Default.HealthAndSafety)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApp() {
    val repository = remember { AdminRepository.getInstance() }
    val currentAdmin by repository.currentAdmin.collectAsState()

    var isDarkTheme by remember { mutableStateOf(true) }
    var currentScreen by remember { mutableStateOf(AdminNavigationItem.DASHBOARD) }
    var wallpaperToEdit by remember { mutableStateOf<Wallpaper?>(null) }
    var isEditingWallpaper by remember { mutableStateOf(false) }
    var wallpaperToPreview by remember { mutableStateOf<Wallpaper?>(null) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    LiveWallpaperAdminTheme(darkTheme = isDarkTheme) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(280.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            // Brand Header
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = ChampagneGold,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "LIVE WALLPAPER",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp
                                        ),
                                        color = ChampagneGold
                                    )
                                    Text(
                                        text = "Production Control Center",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Navigation Items
                            AdminNavigationItem.entries.forEach { item ->
                                NavigationDrawerItem(
                                    label = { Text(item.title, style = MaterialTheme.typography.labelLarge) },
                                    icon = { Icon(item.icon, contentDescription = item.title) },
                                    selected = currentScreen == item && !isEditingWallpaper,
                                    onClick = {
                                        currentScreen = item
                                        isEditingWallpaper = false
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    colors = NavigationDrawerItemDefaults.colors(
                                        selectedContainerColor = ChampagneGold.copy(alpha = 0.15f),
                                        selectedIconColor = ChampagneGold,
                                        selectedTextColor = ChampagneGold,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurface
                                    ),
                                    shape = MaterialTheme.shapes.small,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }

                        // Footer with Current Admin and Theme Toggle
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = currentAdmin.name,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    AdminRolePill(role = currentAdmin.role)
                                }
                                IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                    Icon(
                                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "Toggle Theme",
                                        tint = ChampagneGold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = if (isEditingWallpaper) "Wallpaper Editor" else currentScreen.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        actions = {
                            IconButton(onClick = { isDarkTheme = !isDarkTheme }) {
                                Icon(
                                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                    contentDescription = "Toggle Theme",
                                    tint = ChampagneGold
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    if (isEditingWallpaper) {
                        WallpaperEditorScreen(
                            repository = repository,
                            wallpaperToEdit = wallpaperToEdit,
                            onNavigateBack = { isEditingWallpaper = false }
                        )
                    } else {
                        when (currentScreen) {
                            AdminNavigationItem.DASHBOARD -> DashboardScreen(
                                repository = repository,
                                onNavigateToWallpapers = { currentScreen = AdminNavigationItem.WALLPAPERS },
                                onNavigateToSubscriptions = { currentScreen = AdminNavigationItem.USERS },
                                onNavigateToUsers = { currentScreen = AdminNavigationItem.USERS },
                                onNavigateToModeration = { currentScreen = AdminNavigationItem.MODERATION },
                                onNavigateToMedia = { currentScreen = AdminNavigationItem.MEDIA },
                                onPreviewWallpaper = { wp -> wallpaperToPreview = wp }
                            )
                            AdminNavigationItem.WALLPAPERS -> WallpapersScreen(
                                repository = repository,
                                onCreateWallpaper = {
                                    wallpaperToEdit = null
                                    isEditingWallpaper = true
                                },
                                onEditWallpaper = { wp ->
                                    wallpaperToEdit = wp
                                    isEditingWallpaper = true
                                },
                                onPreviewWallpaper = { wp -> wallpaperToPreview = wp }
                            )
                            AdminNavigationItem.MEDIA -> MediaLibraryScreen(repository = repository)
                            AdminNavigationItem.CATEGORIES -> CategoriesAndTagsScreen(repository = repository)
                            AdminNavigationItem.USERS -> UsersAndSubscriptionsScreen(repository = repository)
                            AdminNavigationItem.REWARDS -> RewardsScreen(repository = repository)
                            AdminNavigationItem.MODERATION -> ModerationScreen(repository = repository)
                            AdminNavigationItem.ANALYTICS -> AnalyticsScreen(repository = repository)
                            AdminNavigationItem.CONFIG -> AppConfigScreen(repository = repository)
                            AdminNavigationItem.ADMINS -> AdminUsersScreen(repository = repository)
                            AdminNavigationItem.HEALTH -> SystemHealthScreen()
                        }
                    }

                    // Global Wallpaper & Charging Simulator Modal
                    wallpaperToPreview?.let { wp ->
                        WallpaperPreviewModal(
                            wallpaper = wp,
                            onDismiss = { wallpaperToPreview = null }
                        )
                    }
                }
            }
        }
    }
}
