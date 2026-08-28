package com.example.data.repository

import com.example.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class AdminRepository private constructor() {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private fun getCurrentTimestamp(): String = dateFormat.format(Date())

    // Active Admin Session
    private val _currentAdmin = MutableStateFlow(
        AdminUser(
            id = "admin-session-active",
            email = "admin@livewallpaper.internal",
            name = "Administrator",
            role = AdminRole.SUPER_ADMIN,
            isActive = true,
            lastLoginAt = getCurrentTimestamp()
        )
    )
    val currentAdmin: StateFlow<AdminUser> = _currentAdmin.asStateFlow()

    // Platform Metrics (Computed strictly from real authoritative records)
    private val _metrics = MutableStateFlow(PlatformMetrics())
    val metrics: StateFlow<PlatformMetrics> = _metrics.asStateFlow()

    // Categories
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    // Tags
    private val _tags = MutableStateFlow<List<Tag>>(emptyList())
    val tags: StateFlow<List<Tag>> = _tags.asStateFlow()

    // Wallpapers
    private val _wallpapers = MutableStateFlow<List<Wallpaper>>(emptyList())
    val wallpapers: StateFlow<List<Wallpaper>> = _wallpapers.asStateFlow()

    // Users
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Google Play Events
    private val _googlePlayEvents = MutableStateFlow<List<GooglePlayEvent>>(emptyList())
    val googlePlayEvents: StateFlow<List<GooglePlayEvent>> = _googlePlayEvents.asStateFlow()

    // Rewarded Ad Events
    private val _rewardAdEvents = MutableStateFlow<List<RewardAdEvent>>(emptyList())
    val rewardAdEvents: StateFlow<List<RewardAdEvent>> = _rewardAdEvents.asStateFlow()

    // Moderation Reports
    private val _reports = MutableStateFlow<List<ModerationReport>>(emptyList())
    val reports: StateFlow<List<ModerationReport>> = _reports.asStateFlow()

    // Media Assets (R2)
    private val _mediaAssets = MutableStateFlow<List<MediaAsset>>(emptyList())
    val mediaAssets: StateFlow<List<MediaAsset>> = _mediaAssets.asStateFlow()

    // App Configurations
    private val _appConfig = MutableStateFlow(AppConfig())
    val appConfig: StateFlow<AppConfig> = _appConfig.asStateFlow()

    // Announcements
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

    // Admin Users
    private val _adminUsers = MutableStateFlow<List<AdminUser>>(emptyList())
    val adminUsers: StateFlow<List<AdminUser>> = _adminUsers.asStateFlow()

    // Audit Logs
    private val _auditLogs = MutableStateFlow<List<AdminAuditLog>>(emptyList())
    val auditLogs: StateFlow<List<AdminAuditLog>> = _auditLogs.asStateFlow()

    // Role Switching for Testing & UI Demonstration
    fun switchActiveAdminRole(role: AdminRole) {
        val current = _currentAdmin.value
        _currentAdmin.value = current.copy(role = role)
        logAudit("ROLE_SIMULATION_SWITCH", "ADMIN_ROLE", current.id, "Switched preview role to $role")
    }

    // Wallpaper CRUD
    fun saveWallpaper(wallpaper: Wallpaper, isNew: Boolean = false) {
        val list = _wallpapers.value.toMutableList()
        val timestamp = getCurrentTimestamp()
        if (isNew) {
            val created = wallpaper.copy(
                id = if (wallpaper.id.isNotBlank()) wallpaper.id else UUID.randomUUID().toString(),
                createdAt = timestamp,
                updatedAt = timestamp
            )
            list.add(0, created)
            _wallpapers.value = list
            logAudit("CREATE_WALLPAPER", "WALLPAPER", created.id, "Created '${created.title}' (${created.type}, ${created.accessType})")
        } else {
            val index = list.indexOfFirst { it.id == wallpaper.id }
            if (index != -1) {
                val updated = wallpaper.copy(updatedAt = timestamp)
                list[index] = updated
                _wallpapers.value = list
                logAudit("UPDATE_WALLPAPER", "WALLPAPER", updated.id, "Updated fields for '${updated.title}'")
            } else {
                list.add(0, wallpaper)
                _wallpapers.value = list
            }
        }
        updateMetrics()
    }

    fun deleteWallpaper(wallpaperId: String, reason: String = "Admin deletion") {
        val target = _wallpapers.value.find { it.id == wallpaperId }
        _wallpapers.value = _wallpapers.value.filter { it.id != wallpaperId }
        logAudit("DELETE_WALLPAPER", "WALLPAPER", wallpaperId, "Deleted '${target?.title ?: wallpaperId}'. Reason: $reason")
        updateMetrics()
    }

    fun toggleWallpaperStatus(wallpaperId: String) {
        val list = _wallpapers.value.map { wp ->
            if (wp.id == wallpaperId) {
                val newStatus = if (wp.status == ContentStatus.ACTIVE) ContentStatus.INACTIVE else ContentStatus.ACTIVE
                logAudit("STATUS_CHANGE", "WALLPAPER", wp.id, "Changed status of '${wp.title}' to $newStatus")
                wp.copy(status = newStatus, updatedAt = getCurrentTimestamp())
            } else wp
        }
        _wallpapers.value = list
        updateMetrics()
    }

    fun bulkUpdateStatus(ids: Set<String>, newStatus: ContentStatus) {
        _wallpapers.value = _wallpapers.value.map { wp ->
            if (ids.contains(wp.id)) wp.copy(status = newStatus, updatedAt = getCurrentTimestamp()) else wp
        }
        logAudit("BULK_STATUS_UPDATE", "WALLPAPERS", ids.joinToString(), "Bulk updated ${ids.size} wallpapers to $newStatus")
        updateMetrics()
    }

    // Categories
    fun saveCategory(category: Category, isNew: Boolean = false) {
        val list = _categories.value.toMutableList()
        if (isNew) {
            list.add(category)
            logAudit("CREATE_CATEGORY", "CATEGORY", category.id, "Created category '${category.name}'")
        } else {
            val index = list.indexOfFirst { it.id == category.id }
            if (index != -1) {
                list[index] = category
                logAudit("UPDATE_CATEGORY", "CATEGORY", category.id, "Updated category '${category.name}'")
            }
        }
        _categories.value = list
    }

    fun deleteCategory(categoryId: String): Boolean {
        // Check if referenced
        val count = _wallpapers.value.count { it.categoryId == categoryId }
        if (count > 0) {
            // Soft deactivate
            _categories.value = _categories.value.map {
                if (it.id == categoryId) it.copy(isActive = false) else it
            }
            logAudit("DEACTIVATE_CATEGORY", "CATEGORY", categoryId, "Soft deactivated category (has $count linked wallpapers)")
            return false
        }
        _categories.value = _categories.value.filter { it.id != categoryId }
        logAudit("DELETE_CATEGORY", "CATEGORY", categoryId, "Safely deleted unreferenced category")
        return true
    }

    // Media Assets
    fun registerMediaAsset(asset: MediaAsset) {
        val list = _mediaAssets.value.toMutableList()
        list.add(0, asset)
        _mediaAssets.value = list
        logAudit("REGISTER_R2_MEDIA", "MEDIA", asset.id, "Registered ${asset.filename} (${asset.assetType})")
    }

    fun deleteMediaAsset(assetId: String): Boolean {
        val asset = _mediaAssets.value.find { it.id == assetId }
        if (asset?.linkedWallpaperId != null) {
            return false // Protected: linked to a wallpaper
        }
        _mediaAssets.value = _mediaAssets.value.filter { it.id != assetId }
        logAudit("DELETE_R2_MEDIA", "MEDIA", assetId, "Removed unlinked asset ${asset?.r2ObjectKey}")
        return true
    }

    // Moderation
    fun updateReportStatus(reportId: String, status: String, notes: String, actionTaken: String) {
        _reports.value = _reports.value.map { rep ->
            if (rep.id == reportId) {
                rep.copy(status = status, moderatorNotes = notes, actionTaken = actionTaken)
            } else rep
        }
        logAudit("MODERATION_ACTION", "REPORT", reportId, "Updated report status to $status with action $actionTaken")
    }

    // App Config
    fun updateAppConfig(config: AppConfig) {
        _appConfig.value = config
        logAudit("UPDATE_CONFIG", "CONFIG", "APP_SETTINGS", "Updated remote app configuration")
    }

    // Announcements
    fun saveAnnouncement(announcement: Announcement) {
        val list = _announcements.value.toMutableList()
        val index = list.indexOfFirst { it.id == announcement.id }
        if (index != -1) {
            list[index] = announcement
        } else {
            list.add(0, announcement)
        }
        _announcements.value = list
        logAudit("SAVE_ANNOUNCEMENT", "ANNOUNCEMENT", announcement.id, "Saved '${announcement.title}'")
    }

    // Audit logger helper
    private fun logAudit(action: String, targetType: String, targetId: String?, details: String) {
        val newLog = AdminAuditLog(
            adminEmail = _currentAdmin.value.email,
            action = action,
            targetType = targetType,
            targetId = targetId,
            details = details,
            createdAt = getCurrentTimestamp()
        )
        val list = _auditLogs.value.toMutableList()
        list.add(0, newLog)
        _auditLogs.value = list
    }

    private fun updateMetrics() {
        val wps = _wallpapers.value
        val usrs = _users.value
        val reps = _reports.value
        val media = _mediaAssets.value
        _metrics.value = PlatformMetrics(
            totalUsers = usrs.size.toLong(),
            newUsersToday = usrs.count { it.lastActiveAt.startsWith(getCurrentTimestamp().take(10)) }.toLong(),
            activeSubscribers = usrs.count { it.isPremium }.toLong(),
            totalWallpapers = wps.size.toLong(),
            liveWallpapers = wps.count { it.type == WallpaperType.LIVE }.toLong(),
            staticWallpapers = wps.count { it.type == WallpaperType.STATIC }.toLong(),
            premiumWallpapers = wps.count { it.accessType == AccessType.PREMIUM }.toLong(),
            freeWallpapers = wps.count { it.accessType == AccessType.FREE }.toLong(),
            activeWallpapers = wps.count { it.status == ContentStatus.ACTIVE }.toLong(),
            featuredWallpapers = wps.count { it.isFeatured }.toLong(),
            trendingWallpapers = wps.count { it.isTrending }.toLong(),
            newWallpapers = wps.count { it.isNew }.toLong(),
            rewardCompletionsToday = _rewardAdEvents.value.size.toLong(),
            totalMediaStorageBytes = media.sumOf { it.sizeBytes },
            openReportsCount = reps.count { it.status == "OPEN" }.toLong()
        )
    }

    companion object {
        @Volatile
        private var instance: AdminRepository? = null

        fun getInstance(): AdminRepository {
            return instance ?: synchronized(this) {
                instance ?: AdminRepository().also { instance = it }
            }
        }
    }
}
