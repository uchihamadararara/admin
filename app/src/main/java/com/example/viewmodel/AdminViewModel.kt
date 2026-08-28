package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.AdminRepository
import com.example.data.repository.AuthRepository
import com.example.data.repository.AuthState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AdminScreen(val title: String, val group: String) {
    DASHBOARD("Dashboard", "CORE"),
    WALLPAPERS("Wallpapers", "CORE"),
    WALLPAPER_CREATE("New Wallpaper", "CORE"),
    WALLPAPER_EDIT("Edit Wallpaper", "CORE"),
    MEDIA_LIBRARY("Media Library", "CORE"),
    CATEGORIES("Categories", "CORE"),
    TAGS("Tags", "CORE"),

    USERS("Users", "USERS & MONETIZATION"),
    SUBSCRIPTIONS("Subscriptions", "USERS & MONETIZATION"),
    ADMOB_SSV("AdMob / SSV", "USERS & MONETIZATION"),

    MODERATION("Moderation", "CONTENT & SAFETY"),
    ANNOUNCEMENTS("Announcements", "CONTENT & SAFETY"),

    APP_CONFIG("App Configuration", "SYSTEM & GOVERNANCE"),
    AUDIT_LOGS("Audit Logs", "SYSTEM & GOVERNANCE"),
    ADMIN_MANAGEMENT("Admin Management", "SYSTEM & GOVERNANCE"),

    SYSTEM_HEALTH("System Health", "SYSTEM")
}

enum class SimulatorState {
    HOME,
    LOCK,
    HOME_TO_LOCK,
    LOCK_TO_HOME,
    HOME_TO_CHARGING,
    LOCK_TO_CHARGING,
    CHARGING_LOOP,
    CHARGING_RETURN
}

data class SimulatorUIState(
    val currentState: SimulatorState = SimulatorState.HOME,
    val isLocked: Boolean = false,
    val isCharging: Boolean = false,
    val soundEnabled: Boolean = false,
    val batteryPercent: Int = 78,
    val activeSlotName: String = "HOME",
    val activeMediaUrl: String = "",
    val stateLog: List<String> = listOf("Simulator initialized in HOME state.")
)

class AdminViewModel(
    private val authRepo: AuthRepository = AuthRepository(),
    private val adminRepo: AdminRepository = AdminRepository()
) : ViewModel() {

    val authState: StateFlow<AuthState> = authRepo.observeAuthState()
        .stateIn(viewModelScope, SharingStarted.Eagerly, AuthState.Loading)

    private val _currentScreen = MutableStateFlow(AdminScreen.DASHBOARD)
    val currentScreen: StateFlow<AdminScreen> = _currentScreen.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(msg: String) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun navigateTo(screen: AdminScreen) {
        _currentScreen.value = screen
    }

    // Auth actions
    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = authRepo.signIn(email, pass)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Sign in failed")
            }
        }
    }

    fun bootstrapSuperAdmin(email: String, pass: String, displayName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = authRepo.bootstrapSuperAdmin(email, pass, displayName)
            if (res.isSuccess) {
                onResult(true, null)
            } else {
                onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Bootstrap failed")
            }
        }
    }

    fun signOut() {
        authRepo.signOut()
        _currentScreen.value = AdminScreen.DASHBOARD
    }

    // Role checks
    fun currentAdmin(): AdminUser? {
        return (authState.value as? AuthState.Authenticated)?.adminUser
    }

    fun canManageWallpapers(): Boolean {
        val role = currentAdmin()?.role ?: return false
        return role.level >= AdminRole.CONTENT_MANAGER.level
    }

    fun canManageUsers(): Boolean {
        val role = currentAdmin()?.role ?: return false
        return role.level >= AdminRole.ADMIN.level
    }

    fun canModerate(): Boolean {
        val role = currentAdmin()?.role ?: return false
        return role.level >= AdminRole.MODERATOR.level
    }

    fun canManageAdmins(): Boolean {
        val role = currentAdmin()?.role ?: return false
        return role == AdminRole.SUPER_ADMIN
    }

    fun canEditAppConfig(): Boolean {
        val role = currentAdmin()?.role ?: return false
        return role.level >= AdminRole.ADMIN.level
    }

    // ==========================================
    // DATA FLOWS
    // ==========================================
    val wallpapers: StateFlow<List<Wallpaper>> = adminRepo.observeWallpapers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val mediaAssets: StateFlow<List<MediaAsset>> = adminRepo.observeMediaAssets()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<Category>> = adminRepo.observeCategories()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val tags: StateFlow<List<Tag>> = adminRepo.observeTags()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val users: StateFlow<List<UserProfile>> = adminRepo.observeUsers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val billingEvents: StateFlow<List<BillingEvent>> = adminRepo.observeBillingEvents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val ssvEvents: StateFlow<List<AdMobSSVEvent>> = adminRepo.observeSSVEvents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val moderationReports: StateFlow<List<ModerationReport>> = adminRepo.observeModerationReports()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val announcements: StateFlow<List<Announcement>> = adminRepo.observeAnnouncements()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val appConfig: StateFlow<AppConfig> = adminRepo.observeAppConfig()
        .stateIn(viewModelScope, SharingStarted.Lazily, AppConfig())

    val auditLogs: StateFlow<List<AuditLog>> = adminRepo.observeAuditLogs()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val adminUsers: StateFlow<List<AdminUser>> = adminRepo.observeAdminUsers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ==========================================
    // WALLPAPER FILTER & SEARCH
    // ==========================================
    val wallpaperSearch = MutableStateFlow("")
    val filterStatus = MutableStateFlow<WallpaperStatus?>(null)
    val filterAccess = MutableStateFlow<AccessType?>(null)
    val filterContentType = MutableStateFlow<ContentType?>(null)
    val filterExperienceType = MutableStateFlow<LiveExperienceType?>(null)
    val filterCategory = MutableStateFlow<String?>(null)

    data class FilterParams(
        val query: String = "",
        val status: WallpaperStatus? = null,
        val access: AccessType? = null,
        val contentType: ContentType? = null,
        val experienceType: LiveExperienceType? = null,
        val categoryId: String? = null
    )

    private val filterParams: Flow<FilterParams> = combine(
        wallpaperSearch,
        filterStatus,
        filterAccess,
        filterContentType
    ) { q, s, a, c ->
        FilterParams(query = q, status = s, access = a, contentType = c)
    }.combine(
        combine(filterExperienceType, filterCategory) { exp, cat -> exp to cat }
    ) { p, (exp, cat) ->
        p.copy(experienceType = exp, categoryId = cat)
    }

    val filteredWallpapers: StateFlow<List<Wallpaper>> = combine(
        wallpapers,
        filterParams
    ) { wList, params ->
        wList.filter { w ->
            val matchQuery = params.query.isBlank() || w.title.contains(params.query, ignoreCase = true) || w.tags.any { it.contains(params.query, ignoreCase = true) }
            val matchStatus = params.status == null || w.status == params.status
            val matchAccess = params.access == null || w.accessType == params.access
            val matchCType = params.contentType == null || w.contentType == params.contentType
            val matchExpType = params.experienceType == null || w.liveExperienceType == params.experienceType
            val matchCat = params.categoryId == null || w.categoryId == params.categoryId
            matchQuery && matchStatus && matchAccess && matchCType && matchExpType && matchCat
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ==========================================
    // WALLPAPER EDITOR
    // ==========================================
    private val _editingWallpaper = MutableStateFlow<Wallpaper>(Wallpaper())
    val editingWallpaper: StateFlow<Wallpaper> = _editingWallpaper.asStateFlow()

    fun startCreateWallpaper() {
        _editingWallpaper.value = Wallpaper(
            id = "",
            title = "",
            description = "",
            contentType = ContentType.STATIC,
            liveExperienceType = null,
            accessType = AccessType.FREE,
            status = WallpaperStatus.DRAFT
        )
        resetSimulator(_editingWallpaper.value)
        _currentScreen.value = AdminScreen.WALLPAPER_CREATE
    }

    fun startEditWallpaper(wallpaper: Wallpaper) {
        _editingWallpaper.value = wallpaper
        resetSimulator(wallpaper)
        _currentScreen.value = AdminScreen.WALLPAPER_EDIT
    }

    fun updateEditingWallpaper(update: (Wallpaper) -> Wallpaper) {
        val updated = update(_editingWallpaper.value)
        _editingWallpaper.value = updated
        syncSimulatorWithWallpaper(updated)
    }

    fun saveEditingWallpaper(onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run {
            onComplete(false, "Unauthorized")
            return
        }
        val wp = _editingWallpaper.value
        if (wp.title.isBlank()) {
            onComplete(false, "Title is required")
            return
        }
        if (wp.contentType == ContentType.STATIC && wp.primaryMediaUrl.isBlank()) {
            onComplete(false, "Primary image URL is required for static wallpaper")
            return
        }
        if (wp.contentType == ContentType.LIVE && wp.liveExperienceType == LiveExperienceType.NORMAL && wp.advancedConfig.primaryUrl.isBlank()) {
            onComplete(false, "Primary video URL is required for Normal Live wallpaper")
            return
        }
        if (wp.contentType == ContentType.LIVE && wp.liveExperienceType == LiveExperienceType.TRANSITION) {
            if (wp.advancedConfig.homeUrl.isBlank() || wp.advancedConfig.lockUrl.isBlank()) {
                onComplete(false, "Both Home and Lock assets are required for Transition Live wallpaper")
                return
            }
        }

        viewModelScope.launch {
            val res = adminRepo.saveWallpaper(wp, admin)
            if (res.isSuccess) {
                showToast("Wallpaper saved successfully.")
                _currentScreen.value = AdminScreen.WALLPAPERS
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage ?: "Failed to save wallpaper")
            }
        }
    }

    fun setWallpaperStatus(id: String, status: WallpaperStatus) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.updateWallpaperStatus(id, status, admin)
            if (res.isSuccess) {
                showToast("Status updated to ${status.name}")
            } else {
                showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun deleteWallpaper(id: String, title: String) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.deleteWallpaper(id, title, admin)
            if (res.isSuccess) {
                showToast("Deleted $title")
            } else {
                showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    // ==========================================
    // VIRTUAL SIMULATOR
    // ==========================================
    private val _simulatorState = MutableStateFlow(SimulatorUIState())
    val simulatorState: StateFlow<SimulatorUIState> = _simulatorState.asStateFlow()

    private fun resetSimulator(wallpaper: Wallpaper) {
        val primaryUrl = when {
            wallpaper.contentType == ContentType.STATIC -> wallpaper.primaryMediaUrl
            wallpaper.liveExperienceType == LiveExperienceType.NORMAL -> wallpaper.advancedConfig.primaryUrl
            wallpaper.liveExperienceType == LiveExperienceType.TRANSITION -> wallpaper.advancedConfig.homeUrl
            else -> ""
        }
        _simulatorState.value = SimulatorUIState(
            currentState = SimulatorState.HOME,
            isLocked = false,
            isCharging = false,
            soundEnabled = false,
            batteryPercent = 78,
            activeSlotName = "HOME",
            activeMediaUrl = primaryUrl,
            stateLog = listOf("Simulator loaded: ${wallpaper.title.ifBlank { "New Wallpaper" }}")
        )
    }

    private fun syncSimulatorWithWallpaper(wallpaper: Wallpaper) {
        val curState = _simulatorState.value.currentState
        val slotUrl = getSlotUrlForState(wallpaper, curState)
        _simulatorState.value = _simulatorState.value.copy(activeMediaUrl = slotUrl)
    }

    private fun getSlotUrlForState(wp: Wallpaper, state: SimulatorState): String {
        if (wp.contentType == ContentType.STATIC) return wp.primaryMediaUrl
        if (wp.liveExperienceType == LiveExperienceType.NORMAL) {
            return when (state) {
                SimulatorState.HOME_TO_CHARGING, SimulatorState.LOCK_TO_CHARGING ->
                    wp.advancedConfig.chargingEntryUrl ?: wp.advancedConfig.chargingLoopUrl ?: wp.advancedConfig.primaryUrl
                SimulatorState.CHARGING_LOOP ->
                    wp.advancedConfig.chargingLoopUrl ?: wp.advancedConfig.primaryUrl
                SimulatorState.CHARGING_RETURN ->
                    wp.advancedConfig.chargingReturnUrl ?: wp.advancedConfig.primaryUrl
                else -> wp.advancedConfig.primaryUrl
            }
        }
        // TRANSITION Live
        return when (state) {
            SimulatorState.HOME -> wp.advancedConfig.homeUrl
            SimulatorState.LOCK -> wp.advancedConfig.lockUrl
            SimulatorState.HOME_TO_LOCK -> wp.advancedConfig.homeToLockUrl ?: wp.advancedConfig.lockUrl
            SimulatorState.LOCK_TO_HOME -> wp.advancedConfig.lockToHomeUrl ?: wp.advancedConfig.homeUrl
            SimulatorState.HOME_TO_CHARGING -> wp.advancedConfig.homeToChargingUrl ?: wp.advancedConfig.chargingLoopUrl ?: wp.advancedConfig.homeUrl
            SimulatorState.LOCK_TO_CHARGING -> wp.advancedConfig.lockToChargingUrl ?: wp.advancedConfig.chargingLoopUrl ?: wp.advancedConfig.lockUrl
            SimulatorState.CHARGING_LOOP -> wp.advancedConfig.chargingLoopUrl ?: (if (_simulatorState.value.isLocked) wp.advancedConfig.lockUrl else wp.advancedConfig.homeUrl)
            SimulatorState.CHARGING_RETURN -> wp.advancedConfig.chargingReturnUrl ?: (if (_simulatorState.value.isLocked) wp.advancedConfig.lockUrl else wp.advancedConfig.homeUrl)
        }
    }

    fun simulatorTogglePowerLock() {
        val cur = _simulatorState.value
        val wp = _editingWallpaper.value
        val newLocked = !cur.isLocked

        val (nextState, slotName) = if (newLocked) {
            if (wp.liveExperienceType == LiveExperienceType.TRANSITION && !wp.advancedConfig.homeToLockUrl.isNullOrBlank()) {
                SimulatorState.HOME_TO_LOCK to "HOME → LOCK"
            } else {
                SimulatorState.LOCK to "LOCK"
            }
        } else {
            if (wp.liveExperienceType == LiveExperienceType.TRANSITION && !wp.advancedConfig.lockToHomeUrl.isNullOrBlank()) {
                SimulatorState.LOCK_TO_HOME to "LOCK → HOME"
            } else {
                SimulatorState.HOME to "HOME"
            }
        }

        val url = getSlotUrlForState(wp, nextState)
        _simulatorState.value = cur.copy(
            currentState = nextState,
            isLocked = newLocked,
            activeSlotName = slotName,
            activeMediaUrl = url,
            stateLog = listOf("Power button pressed -> $slotName") + cur.stateLog.take(8)
        )
    }

    fun simulatorToggleCharging() {
        val cur = _simulatorState.value
        val wp = _editingWallpaper.value
        val newCharging = !cur.isCharging

        val (nextState, slotName) = if (newCharging) {
            if (cur.isLocked) {
                if (wp.liveExperienceType == LiveExperienceType.TRANSITION && !wp.advancedConfig.lockToChargingUrl.isNullOrBlank()) {
                    SimulatorState.LOCK_TO_CHARGING to "LOCK → CHARGING"
                } else {
                    SimulatorState.CHARGING_LOOP to "CHARGING LOOP"
                }
            } else {
                if (!wp.advancedConfig.homeToChargingUrl.isNullOrBlank() || !wp.advancedConfig.chargingEntryUrl.isNullOrBlank()) {
                    SimulatorState.HOME_TO_CHARGING to "HOME → CHARGING"
                } else {
                    SimulatorState.CHARGING_LOOP to "CHARGING LOOP"
                }
            }
        } else {
            if (!wp.advancedConfig.chargingReturnUrl.isNullOrBlank()) {
                SimulatorState.CHARGING_RETURN to "CHARGING RETURN"
            } else {
                if (cur.isLocked) SimulatorState.LOCK to "LOCK" else SimulatorState.HOME to "HOME"
            }
        }

        val url = getSlotUrlForState(wp, nextState)
        _simulatorState.value = cur.copy(
            currentState = nextState,
            isCharging = newCharging,
            activeSlotName = slotName,
            activeMediaUrl = url,
            batteryPercent = if (newCharging) 79 else 78,
            stateLog = listOf(if (newCharging) "Charger Connected -> $slotName" else "Charger Disconnected -> $slotName") + cur.stateLog.take(8)
        )
    }

    fun simulatorToggleSound() {
        val cur = _simulatorState.value
        val newSound = !cur.soundEnabled
        _simulatorState.value = cur.copy(
            soundEnabled = newSound,
            stateLog = listOf("Sound toggled: ${if (newSound) "ON" else "OFF"}") + cur.stateLog.take(8)
        )
    }

    fun simulatorCompleteTransition() {
        val cur = _simulatorState.value
        val wp = _editingWallpaper.value
        val (finalState, slotName) = when (cur.currentState) {
            SimulatorState.HOME_TO_LOCK -> SimulatorState.LOCK to "LOCK"
            SimulatorState.LOCK_TO_HOME -> SimulatorState.HOME to "HOME"
            SimulatorState.HOME_TO_CHARGING, SimulatorState.LOCK_TO_CHARGING -> SimulatorState.CHARGING_LOOP to "CHARGING LOOP"
            SimulatorState.CHARGING_RETURN -> if (cur.isLocked) SimulatorState.LOCK to "LOCK" else SimulatorState.HOME to "HOME"
            else -> cur.currentState to cur.activeSlotName
        }
        val url = getSlotUrlForState(wp, finalState)
        _simulatorState.value = cur.copy(
            currentState = finalState,
            activeSlotName = slotName,
            activeMediaUrl = url,
            stateLog = listOf("Transition completed -> $slotName") + cur.stateLog.take(8)
        )
    }

    // ==========================================
    // MEDIA ASSETS CRUD
    // ==========================================
    fun saveMediaAsset(asset: MediaAsset, onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run { onComplete(false, "Unauthorized"); return }
        if (asset.url.isBlank()) { onComplete(false, "URL cannot be empty"); return }
        viewModelScope.launch {
            val res = adminRepo.saveMediaAsset(asset, admin)
            if (res.isSuccess) {
                showToast("Media reference saved")
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun deleteMediaAsset(id: String, url: String) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.deleteMediaAsset(id, url, admin)
            if (res.isSuccess) showToast("Media registry reference deleted")
            else showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
        }
    }

    // ==========================================
    // CATEGORIES & TAGS CRUD
    // ==========================================
    fun saveCategory(category: Category, onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run { onComplete(false, "Unauthorized"); return }
        if (category.name.isBlank()) { onComplete(false, "Category name is required"); return }
        viewModelScope.launch {
            val res = adminRepo.saveCategory(category, admin)
            if (res.isSuccess) {
                showToast("Category saved")
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun deleteCategory(id: String, name: String) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.deleteCategory(id, name, admin)
            if (res.isSuccess) showToast("Deleted category $name")
            else showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
        }
    }

    fun saveTag(tag: Tag, onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run { onComplete(false, "Unauthorized"); return }
        if (tag.name.isBlank()) { onComplete(false, "Tag name is required"); return }
        viewModelScope.launch {
            val res = adminRepo.saveTag(tag, admin)
            if (res.isSuccess) {
                showToast("Tag saved")
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun deleteTag(id: String, name: String) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.deleteTag(id, name, admin)
            if (res.isSuccess) showToast("Deleted tag $name")
            else showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
        }
    }

    // ==========================================
    // USERS & MODERATION
    // ==========================================
    fun updateUserStatus(uid: String, status: AccountStatus) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.updateUserAccountStatus(uid, status, admin)
            if (res.isSuccess) showToast("User status set to ${status.name}")
            else showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
        }
    }

    fun resolveModerationReport(reportId: String, status: ReportStatus, notes: String) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.resolveReport(reportId, status, notes, admin)
            if (res.isSuccess) showToast("Report marked as ${status.name}")
            else showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
        }
    }

    // ==========================================
    // ANNOUNCEMENTS
    // ==========================================
    fun saveAnnouncement(announcement: Announcement, onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run { onComplete(false, "Unauthorized"); return }
        if (announcement.title.isBlank() || announcement.message.isBlank()) {
            onComplete(false, "Title and message are required")
            return
        }
        viewModelScope.launch {
            val res = adminRepo.saveAnnouncement(announcement, admin)
            if (res.isSuccess) {
                showToast("Announcement saved")
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    fun deleteAnnouncement(id: String, title: String) {
        val admin = currentAdmin() ?: return
        viewModelScope.launch {
            val res = adminRepo.deleteAnnouncement(id, title, admin)
            if (res.isSuccess) showToast("Announcement deleted")
            else showToast("Error: ${res.exceptionOrNull()?.localizedMessage}")
        }
    }

    // ==========================================
    // APP CONFIGURATION
    // ==========================================
    fun saveAppConfig(config: AppConfig, onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run { onComplete(false, "Unauthorized"); return }
        viewModelScope.launch {
            val res = adminRepo.saveAppConfig(config, admin)
            if (res.isSuccess) {
                showToast("Configuration saved successfully")
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    // ==========================================
    // ADMIN USERS (Super Admin)
    // ==========================================
    fun saveAdminUser(targetUser: AdminUser, onComplete: (Boolean, String?) -> Unit) {
        val admin = currentAdmin() ?: run { onComplete(false, "Unauthorized"); return }
        if (admin.role != AdminRole.SUPER_ADMIN) {
            onComplete(false, "Only Super Admin can modify administrators")
            return
        }
        viewModelScope.launch {
            val res = adminRepo.saveAdminUser(targetUser, admin)
            if (res.isSuccess) {
                showToast("Admin account saved")
                onComplete(true, null)
            } else {
                onComplete(false, res.exceptionOrNull()?.localizedMessage)
            }
        }
    }
}
