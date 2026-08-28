package com.example.model

enum class AdminSection(val title: String, val group: String) {
    DASHBOARD("Dashboard", "CORE CATALOG"),
    WALLPAPERS("Wallpapers Catalog", "CORE CATALOG"),
    MEDIA_LIBRARY("Media Library (R2)", "CORE CATALOG"),
    CATEGORIES("Categories", "CORE CATALOG"),
    TAGS("Tags", "CORE CATALOG"),
    USERS("Users Explorer", "USERS & MONETIZATION"),
    SUBSCRIPTIONS("Subscriptions (Play)", "USERS & MONETIZATION"),
    ADMOB_SSV("AdMob / SSV Events", "USERS & MONETIZATION"),
    MODERATION("Moderation Queue", "USERS & MONETIZATION"),
    ANNOUNCEMENTS("Announcements", "SYSTEM & GOVERNANCE"),
    APP_CONFIG("App Configuration", "SYSTEM & GOVERNANCE"),
    AUDIT_LOGS("Audit Logs", "SYSTEM & GOVERNANCE"),
    ADMIN_MANAGEMENT("Admin Management", "SYSTEM & GOVERNANCE"),
    SETTINGS("Settings & Health", "SYSTEM & GOVERNANCE")
}

enum class AdminRole {
    SUPER_ADMIN,
    ADMIN,
    CONTENT_MANAGER,
    MODERATOR,
    SUPPORT
}

data class AdminUser(
    val id: String,
    val email: String,
    val role: AdminRole,
    val lastActive: String,
    val createdAt: String,
    val isCurrent: Boolean = false
)

data class MediaAsset(
    val id: String,
    val key: String,
    val name: String,
    val url: String,
    val mimeType: String,
    val sizeBytes: Long,
    val width: Int? = null,
    val height: Int? = null,
    val durationSeconds: Double? = null,
    val hasAudio: Boolean = false,
    val createdAt: String = "2026-08-28"
)

data class WallpaperCategory(
    val id: String,
    val name: String,
    val slug: String,
    val icon: String,
    val count: Int,
    val isActive: Boolean = true
)

data class WallpaperTag(
    val id: String,
    val name: String,
    val usageCount: Int,
    val isTrending: Boolean = false
)

data class EndUser(
    val id: String,
    val email: String,
    val tier: String, // FREE, PRO, LIFETIME
    val adCredits: Int,
    val joinDate: String,
    val isBanned: Boolean = false
)

data class PlaySubscription(
    val sku: String,
    val name: String,
    val price: String,
    val billingPeriod: String,
    val activeSubscribers: Int,
    val status: String = "ACTIVE"
)

data class ModerationReport(
    val id: String,
    val wallpaperId: String,
    val wallpaperTitle: String,
    val reason: String,
    val reporterEmail: String,
    val timestamp: String,
    val status: String = "PENDING"
)

data class InAppAnnouncement(
    val id: String,
    val title: String,
    val message: String,
    val actionUrl: String? = null,
    val targetAudience: String = "ALL_USERS",
    val expiresAt: String,
    val isActive: Boolean = true
)

data class RemoteAppConfig(
    val minAppVersion: String = "1.0.0",
    val forceUpdate: Boolean = false,
    val maintenanceMode: Boolean = false,
    val dailyFreeDownloads: Int = 3,
    val interstitialAdIntervalSeconds: Int = 180,
    val supabaseUrl: String = "https://twzrtrbbsehvlabupygl.supabase.co",
    val r2CdnEndpoint: String = "https://cdn.livewallpaper.app"
)

data class AdminAuditLog(
    val id: String,
    val timestamp: String,
    val adminEmail: String,
    val action: String,
    val targetResource: String,
    val detailsJson: String
)
