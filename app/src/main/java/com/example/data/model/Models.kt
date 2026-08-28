package com.example.data.model

import java.util.UUID

enum class WallpaperType {
    STATIC, LIVE
}

enum class LiveExperienceType {
    NORMAL, TRANSITION
}

enum class AccessType {
    FREE, PREMIUM
}

enum class ContentStatus {
    ACTIVE, INACTIVE, UNDER_REVIEW, REJECTED
}

enum class AdminRole {
    SUPER_ADMIN, ADMIN, CONTENT_MANAGER, MODERATOR, SUPPORT
}

enum class SubscriptionPlan(val planId: String, val displayName: String, val durationDays: Int) {
    NONE("none", "Free Tier", 0),
    PREMIUM_3_DAYS("premium_3_days", "3-Day Pass", 3),
    PREMIUM_7_DAYS("premium_7_days", "7-Day Pass", 7),
    PREMIUM_MONTHLY("premium_monthly", "Monthly Pass", 30),
    PREMIUM_YEARLY("premium_yearly", "Annual Pass", 365)
}

data class SoundMetadata(
    val hasAudioTrack: Boolean = false,
    val sampleRate: Int = 44100,
    val codec: String = "aac",
    val defaultVolume: Float = 1.0f
)

data class Wallpaper(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val type: WallpaperType = WallpaperType.STATIC,
    val liveExperienceType: LiveExperienceType = LiveExperienceType.NORMAL,
    val accessType: AccessType = AccessType.FREE,
    val status: ContentStatus = ContentStatus.ACTIVE,
    val categoryId: String? = null,
    val category: String = "Abstract",
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isNew: Boolean = true,
    val sortOrder: Int = 0,
    val thumbnailUrl: String,
    val previewUrl: String = "",
    val mediaUrl: String,
    val fileSizeBytes: Long = 0,
    val width: Int = 1080,
    val height: Int = 1920,
    val durationSeconds: Float = 0.0f,
    val fps: Int = 60,
    val aspectRatio: String = "9:16",
    
    // Per-Wallpaper Sound Behavior
    val soundAvailable: Boolean = false,
    val soundMetadata: SoundMetadata = SoundMetadata(),
    
    // Content-Driven Charging Experience
    val chargingAnimationAvailable: Boolean = false,
    val chargingAnimationId: String? = null,
    val chargingAnimationType: String = "BATTERY_PULSE",
    val chargingAnimationAsset: String? = null,
    val chargingAnimationPreview: String? = null,
    val chargingTransitionDurationMs: Int = 300,
    
    // Visual Transition Experience
    val transitionAvailable: Boolean = false,
    val transitionType: String = "FADE",
    val transitionAsset: String? = null,
    val transitionSourceState: String = "HOME",
    val transitionTargetState: String = "CHARGING",
    val transitionDurationMs: Int = 400,
    
    // Telemetry
    val viewsCount: Long = 0,
    val previewsCount: Long = 0,
    val appliesCount: Long = 0,
    val favoritesCount: Long = 0,
    val createdAt: String = "2026-08-20T10:00:00Z",
    val updatedAt: String = "2026-08-26T12:00:00Z"
) {
    val isPremium: Boolean get() = accessType == AccessType.PREMIUM
}

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val slug: String,
    val description: String = "",
    val iconUrl: String? = null,
    val thumbnailUrl: String? = null,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val wallpaperCount: Int = 0
)

data class Tag(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val isActive: Boolean = true,
    val usageCount: Int = 0
)

data class User(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val accountStatus: String = "ACTIVE",
    val isPremium: Boolean = false,
    val subscriptionPlan: SubscriptionPlan = SubscriptionPlan.NONE,
    val subscriptionExpiresAt: String? = null,
    val currentAppliedWallpaperId: String? = null,
    val oemBrand: String = "Samsung",
    val appVersion: String = "1.2.0",
    val lastActiveAt: String = "2026-08-26T11:45:00Z",
    val createdAt: String = "2026-08-15T08:30:00Z"
)

data class GooglePlayEvent(
    val id: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val userEmail: String? = null,
    val orderId: String? = null,
    val purchaseTokenHash: String = "e3b0c442...",
    val productId: String = "premium_pass",
    val basePlanId: String = "premium_monthly",
    val eventType: String = "PURCHASE", // PURCHASE, RENEWAL, CANCELLATION, EXPIRATION, RTDN_NOTIFICATION
    val processingStatus: String = "SUCCESS",
    val failureReason: String? = null,
    val eventTime: String = "2026-08-26T10:15:00Z"
)

data class RewardAdEvent(
    val id: String = UUID.randomUUID().toString(),
    val userId: String? = null,
    val wallpaperId: String? = null,
    val wallpaperTitle: String = "Cosmic Aurora",
    val placementId: String = "rewarded_wallpaper_apply",
    val ssvTransactionId: String = "ssv_tx_981273891",
    val verificationStatus: String = "VERIFIED",
    val createdAt: String = "2026-08-26T11:20:00Z"
)

data class ModerationReport(
    val id: String = UUID.randomUUID().toString(),
    val wallpaperId: String,
    val wallpaperTitle: String,
    val reportedByUserId: String,
    val reason: String,
    val status: String = "OPEN", // OPEN, IN_REVIEW, RESOLVED, DISMISSED
    val moderatorNotes: String = "",
    val actionTaken: String = "NONE",
    val createdAt: String = "2026-08-25T14:22:00Z"
)

data class AppConfig(
    val minAppVersion: String = "1.0.0",
    val recommendedAppVersion: String = "1.2.0",
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Scheduled maintenance in progress.",
    val featuredSectionEnabled: Boolean = true,
    val trendingSectionEnabled: Boolean = true,
    val liveSectionEnabled: Boolean = true,
    val premiumSectionEnabled: Boolean = true,
    val remoteAdKillSwitch: Boolean = false
)

data class Announcement(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val startDate: String,
    val endDate: String,
    val isActive: Boolean = true,
    val priority: Int = 1,
    val targetAudience: String = "ALL" // ALL, FREE_USERS, PREMIUM_USERS
)

data class AdminUser(
    val id: String = UUID.randomUUID().toString(),
    val email: String,
    val name: String,
    val role: AdminRole = AdminRole.CONTENT_MANAGER,
    val isActive: Boolean = true,
    val lastLoginAt: String? = null,
    val createdAt: String = "2026-08-01T09:00:00Z"
)

data class AdminAuditLog(
    val id: String = UUID.randomUUID().toString(),
    val adminEmail: String,
    val action: String,
    val targetType: String,
    val targetId: String? = null,
    val details: String = "",
    val ipAddress: String = "127.0.0.1",
    val status: String = "SUCCESS",
    val createdAt: String = "2026-08-26T12:00:00Z"
)

data class MediaAsset(
    val id: String = UUID.randomUUID().toString(),
    val r2ObjectKey: String,
    val filename: String,
    val mimeType: String,
    val sizeBytes: Long,
    val assetType: String, // IMAGE, VIDEO, CHARGING_ANIMATION, TRANSITION_ASSET, THUMBNAIL
    val hasAudio: Boolean = false,
    val linkedWallpaperId: String? = null,
    val isOrphaned: Boolean = false,
    val uploadStatus: String = "COMPLETED",
    val createdAt: String = "2026-08-26T08:00:00Z"
)

data class WallpaperAsset(
    val id: String = UUID.randomUUID().toString(),
    val wallpaperId: String,
    val slotType: String, // PRIMARY, HOME, LOCK, LOCK_TO_HOME, HOME_TO_LOCK, HOME_TO_CHARGING, LOCK_TO_CHARGING, CHARGING_LOOP, CHARGING_RETURN
    val storageKey: String,
    val mediaUrl: String,
    val mimeType: String,
    val width: Int = 1080,
    val height: Int = 1920,
    val durationMs: Int = 0,
    val fps: Int = 60,
    val hasAudio: Boolean = false,
    val audioCodec: String? = null,
    val audioChannels: Int? = null,
    val fileSizeBytes: Long = 0,
    val sha256: String? = null,
    val createdAt: String = "2026-08-26T08:00:00Z",
    val updatedAt: String = "2026-08-26T08:00:00Z"
)

data class PlatformMetrics(
    val totalUsers: Long = 0,
    val newUsersToday: Long = 0,
    val activeSubscribers: Long = 0,
    val totalWallpapers: Long = 0,
    val liveWallpapers: Long = 0,
    val staticWallpapers: Long = 0,
    val premiumWallpapers: Long = 0,
    val freeWallpapers: Long = 0,
    val activeWallpapers: Long = 0,
    val featuredWallpapers: Long = 0,
    val trendingWallpapers: Long = 0,
    val newWallpapers: Long = 0,
    val rewardCompletionsToday: Long = 0,
    val totalMediaStorageBytes: Long = 0,
    val openReportsCount: Long = 0
)
