package com.example.data.model

enum class ReportStatus {
    OPEN,
    IN_REVIEW,
    RESOLVED,
    DISMISSED;

    companion object {
        fun fromString(value: String?): ReportStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: OPEN
        }
    }
}

data class ModerationReport(
    val id: String = "",
    val reporterUid: String = "",
    val reporterEmail: String? = null,
    val targetType: String = "WALLPAPER", // WALLPAPER, USER, COMMENT
    val targetId: String = "",
    val targetTitle: String? = null,
    val reason: String = "",
    val comments: String = "",
    val status: ReportStatus = ReportStatus.OPEN,
    val reviewedBy: String? = null,
    val resolutionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class TargetAudience {
    ALL,
    FREE_USERS,
    VIP_USERS;

    companion object {
        fun fromString(value: String?): TargetAudience {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ALL
        }
    }
}

data class Announcement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val actionUrl: String? = null,
    val targetAudience: TargetAudience = TargetAudience.ALL,
    val isActive: Boolean = true,
    val startTime: Long? = null,
    val expiryTime: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AppConfig(
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Live Wallpaper Royal is currently undergoing scheduled maintenance. Please check back shortly.",
    val minSupportedVersionCode: Int = 1,
    val latestVersionCode: Int = 1,
    val interstitialAdIntervalMinutes: Int = 15,
    val rewardedAdDailyLimit: Int = 10,
    val enableTransitionWallpapers: Boolean = true,
    val enableChargingExperience: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedBy: String = "system"
)

data class AuditLog(
    val id: String = "",
    val adminUid: String = "",
    val adminEmail: String = "",
    val role: String = "",
    val action: String = "",
    val entity: String = "",
    val entityId: String = "",
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
