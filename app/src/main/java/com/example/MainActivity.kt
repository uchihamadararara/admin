package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.AuthState
import com.example.ui.components.ConsoleDrawerContent
import com.example.ui.components.ConsoleHeader
import com.example.ui.screens.*
import com.example.ui.theme.AmoledBackground
import com.example.ui.theme.RoyalAdminTheme
import com.example.ui.theme.RoyalGold
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.AdminScreen
import com.example.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoyalAdminTheme {
                RoyalAdminApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoyalAdminApp(
    viewModel: AdminViewModel = viewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val context = LocalContext.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    when (val state = authState) {
        is AuthState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AmoledBackground),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = RoyalGold, strokeWidth = 3.dp)
                    Text(
                        text = "Authenticating Royal Console...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )
                }
            }
        }

        is AuthState.Unauthenticated, is AuthState.Error -> {
            LoginScreen(viewModel = viewModel)
        }

        is AuthState.Authenticated -> {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = AmoledBackground,
                        modifier = Modifier.width(280.dp)
                    ) {
                        ConsoleDrawerContent(
                            currentScreen = currentScreen,
                            currentAdmin = state.adminUser,
                            onNavigate = { screen ->
                                viewModel.navigateTo(screen)
                                scope.launch { drawerState.close() }
                            },
                            onSignOut = {
                                scope.launch { drawerState.close() }
                                viewModel.signOut()
                            }
                        )
                    }
                }
            ) {
                Scaffold(
                    topBar = {
                        if (currentScreen != AdminScreen.WALLPAPER_CREATE && currentScreen != AdminScreen.WALLPAPER_EDIT) {
                            ConsoleHeader(
                                currentAdmin = state.adminUser,
                                title = currentScreen.title,
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onSignOut = { viewModel.signOut() }
                            )
                        }
                    },
                    containerColor = AmoledBackground
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AdminScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                            AdminScreen.WALLPAPERS -> WallpapersScreen(viewModel = viewModel)
                            AdminScreen.WALLPAPER_CREATE, AdminScreen.WALLPAPER_EDIT -> WallpaperEditorScreen(viewModel = viewModel)
                            AdminScreen.MEDIA_LIBRARY -> MediaLibraryScreen(viewModel = viewModel)
                            AdminScreen.CATEGORIES -> CategoriesScreen(viewModel = viewModel)
                            AdminScreen.TAGS -> TagsScreen(viewModel = viewModel)
                            AdminScreen.USERS -> UsersScreen(viewModel = viewModel)
                            AdminScreen.SUBSCRIPTIONS -> SubscriptionsScreen(viewModel = viewModel)
                            AdminScreen.ADMOB_SSV -> AdMobSSVScreen(viewModel = viewModel)
                            AdminScreen.MODERATION -> ModerationScreen(viewModel = viewModel)
                            AdminScreen.ANNOUNCEMENTS -> AnnouncementsScreen(viewModel = viewModel)
                            AdminScreen.APP_CONFIG -> AppConfigScreen(viewModel = viewModel)
                            AdminScreen.AUDIT_LOGS -> AuditLogsScreen(viewModel = viewModel)
                            AdminScreen.ADMIN_MANAGEMENT -> AdminManagementScreen(viewModel = viewModel)
                            AdminScreen.SYSTEM_HEALTH -> SystemHealthScreen(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

