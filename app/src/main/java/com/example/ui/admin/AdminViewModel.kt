package com.example.ui.admin

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.model.*

class AdminViewModel : ViewModel() {

    // Auth State
    var isAuthenticated by mutableStateOf(false)
    var currentAdmin by mutableStateOf(
        AdminUser(
            id = "4ebff349-81dd-429a-b9b4-3d0248117592",
            email = "mahiyadinesh777@gmail.com",
            role = AdminRole.SUPER_ADMIN,
            lastActive = "Active now",
            createdAt = "2026-01-10",
            isCurrent = true
        )
    )

    // Navigation State
    var currentSection by mutableStateOf(AdminSection.DASHBOARD)
    var isDrawerOpen by mutableStateOf(false)

    // Toast & Dialog Alerts
    var toastMessage by mutableStateOf<String?>(null)

    fun showToast(msg: String) {
        toastMessage = msg
    }

    fun dismissToast() {
        toastMessage = null
    }

    // ==========================================
    // 1. WALLPAPERS STATE & MANAGEMENT
    // ==========================================
    var wallpapersList by mutableStateOf(emptyList<Wallpaper>())

    // Wallpaper Filter & Search State
    var wallpaperSearchQuery by mutableStateOf("")
    var selectedTypeFilter by mutableStateOf("ALL") // ALL, STATIC, NORMAL, TRANSITION
    var selectedStatusFilter by mutableStateOf("ALL") // ALL, PUBLISHED, DRAFT, INACTIVE, ARCHIVED
    var selectedTierFilter by mutableStateOf("ALL") // ALL, FREE, PREMIUM

    // Wallpaper Editor & Preview Mode
    var editingWallpaper by mutableStateOf<Wallpaper?>(null)
    var isEditorOpen by mutableStateOf(false)
    var previewingWallpaper by mutableStateOf<Wallpaper?>(null)
    var isPreviewSimulatorOpen by mutableStateOf(false)

    // Simulator Interactive Controls
    var simIsScreenOn by mutableStateOf(true)
    var simIsLocked by mutableStateOf(false)
    var simIsCharging by mutableStateOf(false)
    var simSoundEnabled by mutableStateOf(true)
    var simCurrentStateName by mutableStateOf("HomeScreen")
    var simLog by mutableStateOf(listOf("Simulator initialized on Home Screen"))

    fun updateSimulatorState() {
        if (!simIsScreenOn) {
            simCurrentStateName = "Hidden"
            simLog = listOf("Screen OFF -> Engine Hidden & Muted") + simLog.take(6)
            return
        }
        val wp = previewingWallpaper ?: return
        if (wp.liveExperienceType == LiveExperienceType.TRANSITION) {
            if (simIsCharging) {
                simCurrentStateName = if (simIsLocked) "LockToChargingTransition -> ChargingLoop" else "HomeToChargingTransition -> ChargingLoop"
                simLog = listOf("Power Connected -> State: $simCurrentStateName") + simLog.take(6)
            } else if (simIsLocked) {
                simCurrentStateName = "LockScreen"
                simLog = listOf("Device Locked -> Playing lock.mp4") + simLog.take(6)
            } else {
                simCurrentStateName = "HomeScreen"
                simLog = listOf("Device Unlocked -> Playing home.mp4") + simLog.take(6)
            }
        } else {
            if (simIsCharging && wp.advancedConfig.chargingLoop != null) {
                simCurrentStateName = "ChargingLoop (Configured Asset)"
                simLog = listOf("Charger Plugged -> Playing charging_loop.mp4") + simLog.take(6)
            } else {
                simCurrentStateName = if (wp.contentType == ContentType.STATIC) "StaticImage" else "PrimaryLiveVideo"
                simLog = listOf("Default State -> Playing primary media") + simLog.take(6)
            }
        }
    }

    fun openWallpaperEditor(wallpaper: Wallpaper?) {
        editingWallpaper = wallpaper ?: Wallpaper(
            id = "wp_" + System.currentTimeMillis().toString().takeLast(5),
            title = "",
            description = "",
            contentType = ContentType.LIVE,
            liveExperienceType = LiveExperienceType.NORMAL,
            categoryId = "cat_scifi",
            tags = emptyList(),
            isPremium = false,
            isFeatured = false,
            isTrending = false,
            isNew = true,
            status = WallpaperStatus.DRAFT
        )
        isEditorOpen = true
    }

    fun closeWallpaperEditor() {
        editingWallpaper = null
        isEditorOpen = false
    }

    fun saveWallpaper(wallpaper: Wallpaper) {
        val existingIndex = wallpapersList.indexOfFirst { it.id == wallpaper.id }
        wallpapersList = if (existingIndex >= 0) {
            wallpapersList.toMutableList().apply { set(existingIndex, wallpaper) }
        } else {
            listOf(wallpaper) + wallpapersList
        }
        addAuditLog("SAVE_WALLPAPER", "Wallpaper: ${wallpaper.title} (${wallpaper.id})")
        showToast("Wallpaper '${wallpaper.title}' saved successfully")
        isEditorOpen = false
    }

    fun deleteWallpaper(wallpaperId: String) {
        val wp = wallpapersList.find { it.id == wallpaperId }
        wallpapersList = wallpapersList.filter { it.id != wallpaperId }
        addAuditLog("DELETE_WALLPAPER", "Wallpaper ID: $wallpaperId (${wp?.title})")
        showToast("Wallpaper deleted")
    }

    fun toggleWallpaperStatus(wallpaperId: String) {
        wallpapersList = wallpapersList.map { wp ->
            if (wp.id == wallpaperId) {
                val nextStatus = if (wp.status == WallpaperStatus.PUBLISHED) WallpaperStatus.DRAFT else WallpaperStatus.PUBLISHED
                addAuditLog("TOGGLE_STATUS", "${wp.title} status changed to $nextStatus")
                wp.copy(status = nextStatus)
            } else wp
        }
        showToast("Status updated")
    }

    // ==========================================
    // 2. MEDIA LIBRARY STATE
    // ==========================================
    var mediaAssetsList by mutableStateOf(emptyList<MediaAsset>())

    fun addMediaAsset(asset: MediaAsset) {
        mediaAssetsList = listOf(asset) + mediaAssetsList
        addAuditLog("UPLOAD_MEDIA", "Uploaded to R2: ${asset.key}")
        showToast("Asset uploaded to Cloudflare R2")
    }

    fun deleteMediaAsset(assetId: String) {
        val asset = mediaAssetsList.find { it.id == assetId }
        mediaAssetsList = mediaAssetsList.filter { it.id != assetId }
        addAuditLog("DELETE_MEDIA", "Deleted asset: ${asset?.key}")
        showToast("Asset removed from R2")
    }

    // ==========================================
    // 3. CATEGORIES & TAGS
    // ==========================================
    var categoriesList by mutableStateOf(
        listOf(
            WallpaperCategory("cat_scifi", "Sci-Fi & Cyber", "sci-fi", "RocketLaunch", 0),
            WallpaperCategory("cat_nature", "Nature & Landscape", "nature", "Landscape", 0),
            WallpaperCategory("cat_minimal", "Minimal & AMOLED", "minimal", "Contrast", 0),
            WallpaperCategory("cat_anime", "Anime & Fantasy", "anime", "AutoAwesome", 0),
            WallpaperCategory("cat_cars", "Automotive & Speed", "automotive", "DirectionsCar", 0)
        )
    )

    fun addCategory(name: String, slug: String) {
        val newCat = WallpaperCategory(
            id = "cat_" + System.currentTimeMillis().toString().takeLast(4),
            name = name,
            slug = slug,
            icon = "Folder",
            count = 0
        )
        categoriesList = categoriesList + newCat
        addAuditLog("CREATE_CATEGORY", "Category created: $name")
        showToast("Category '$name' created")
    }

    var tagsList by mutableStateOf(
        listOf(
            WallpaperTag("tag_1", "Cyberpunk", 0, isTrending = false),
            WallpaperTag("tag_2", "AMOLED", 0, isTrending = false),
            WallpaperTag("tag_3", "Neon", 0, isTrending = false),
            WallpaperTag("tag_4", "4K", 0, isTrending = false),
            WallpaperTag("tag_5", "Nature", 0, isTrending = false),
            WallpaperTag("tag_6", "Relaxing", 0, isTrending = false)
        )
    )

    fun addTag(name: String) {
        val newTag = WallpaperTag(
            id = "tag_" + System.currentTimeMillis().toString().takeLast(4),
            name = name,
            usageCount = 1,
            isTrending = false
        )
        tagsList = tagsList + newTag
        addAuditLog("CREATE_TAG", "Tag created: $name")
        showToast("Tag '$name' added")
    }

    // ==========================================
    // 4. USERS & MONETIZATION
    // ==========================================
    var usersList by mutableStateOf(emptyList<EndUser>())

    fun toggleUserBan(userId: String) {
        usersList = usersList.map { u ->
            if (u.id == userId) {
                val next = !u.isBanned
                addAuditLog("MODERATE_USER", "User ${u.email} ban state set to $next")
                u.copy(isBanned = next)
            } else u
        }
        showToast("User status updated")
    }

    var subscriptionsList by mutableStateOf(
        listOf(
            PlaySubscription("vip_3days", "VIP 3 Days Access", "$0.99 / 3d", "P3D", 0),
            PlaySubscription("vip_7days", "VIP 7 Days Access", "$1.99 / 7d", "P7D", 0),
            PlaySubscription("vip_14days", "VIP 14 Days Access", "$2.99 / 14d", "P14D", 0),
            PlaySubscription("vip_1month", "VIP 1 Month Subscription", "$4.99 / mo", "P1M", 0),
            PlaySubscription("vip_lifetime", "VIP Lifetime Access", "$29.99 once", "LIFETIME", 0)
        )
    )

    // ==========================================
    // 5. MODERATION & ANNOUNCEMENTS
    // ==========================================
    var moderationReportsList by mutableStateOf(emptyList<ModerationReport>())

    fun dismissReport(reportId: String) {
        moderationReportsList = moderationReportsList.filter { it.id != reportId }
        addAuditLog("DISMISS_REPORT", "Report $reportId dismissed")
        showToast("Report dismissed")
    }

    var announcementsList by mutableStateOf(emptyList<InAppAnnouncement>())

    fun addAnnouncement(announcement: InAppAnnouncement) {
        announcementsList = listOf(announcement) + announcementsList
        addAuditLog("CREATE_ANNOUNCEMENT", "Published: ${announcement.title}")
        showToast("Announcement published")
    }

    // ==========================================
    // 6. REMOTE APP CONFIG
    // ==========================================
    var remoteConfig by mutableStateOf(RemoteAppConfig())

    fun updateRemoteConfig(config: RemoteAppConfig) {
        remoteConfig = config
        addAuditLog("UPDATE_CONFIG", "Remote configuration updated")
        showToast("App Remote Config deployed successfully")
    }

    // ==========================================
    // 7. AUDIT LOGS
    // ==========================================
    var auditLogsList by mutableStateOf(
        listOf(
            AdminAuditLog("log_01", "2026-08-28 00:00:00", "system", "SYSTEM_READY", "Admin Console", "{\"status\":\"production_ready\"}")
        )
    )

    fun addAuditLog(action: String, resource: String, details: String = "{}") {
        val newLog = AdminAuditLog(
            id = "log_" + System.currentTimeMillis().toString().takeLast(6),
            timestamp = "Just now",
            adminEmail = currentAdmin.email,
            action = action,
            targetResource = resource,
            detailsJson = details
        )
        auditLogsList = listOf(newLog) + auditLogsList.take(50)
    }

    // ==========================================
    // 8. ADMIN TEAM MANAGEMENT
    // ==========================================
    var adminTeamList by mutableStateOf(
        listOf(
            AdminUser("4ebff349-81dd-429a-b9b4-3d0248117592", "mahiyadinesh777@gmail.com", AdminRole.SUPER_ADMIN, "Active now", "2026-01-10", isCurrent = true)
        )
    )

    fun addAdminMember(email: String, role: AdminRole) {
        val newAdmin = AdminUser(
            id = "adm_" + System.currentTimeMillis().toString().takeLast(4),
            email = email,
            role = role,
            lastActive = "Invited",
            createdAt = "2026-08-28"
        )
        adminTeamList = adminTeamList + newAdmin
        addAuditLog("INVITE_ADMIN", "Admin invited: $email as ${role.name}")
        showToast("Admin $email invited")
    }

    fun removeAdminMember(id: String) {
        val admin = adminTeamList.find { it.id == id }
        if (admin?.isCurrent == true) {
            showToast("Cannot remove your own active admin account")
            return
        }
        adminTeamList = adminTeamList.filter { it.id != id }
        addAuditLog("REMOVE_ADMIN", "Admin revoked: ${admin?.email}")
        showToast("Admin access revoked")
    }
}
