package com.example.data.model

enum class ContentType {
    STATIC,
    LIVE;

    companion object {
        fun fromString(value: String?): ContentType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: STATIC
        }
    }
}

enum class LiveExperienceType {
    NORMAL,
    TRANSITION;

    companion object {
        fun fromString(value: String?): LiveExperienceType? {
            if (value.isNullOrBlank()) return null
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
        }
    }
}

enum class AccessType {
    FREE,
    PREMIUM;

    companion object {
        fun fromString(value: String?): AccessType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: FREE
        }
    }
}

enum class WallpaperStatus {
    DRAFT,
    PUBLISHED,
    INACTIVE,
    ARCHIVED;

    companion object {
        fun fromString(value: String?): WallpaperStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: DRAFT
        }
    }
}

/**
 * AdvancedConfig holds exact media slot URLs for Normal Live and Transition Live wallpapers.
 * This directly matches the Android client app contract.
 */
data class AdvancedConfig(
    // Normal Live slots
    val primaryUrl: String = "",
    val chargingEntryUrl: String? = null,
    val chargingLoopUrl: String? = null,
    val chargingReturnUrl: String? = null,

    // Transition Live slots
    val homeUrl: String = "",
    val lockUrl: String = "",
    val homeToLockUrl: String? = null,
    val lockToHomeUrl: String? = null,
    val homeToChargingUrl: String? = null,
    val lockToChargingUrl: String? = null
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "primaryUrl" to primaryUrl,
            "chargingEntryUrl" to chargingEntryUrl,
            "chargingLoopUrl" to chargingLoopUrl,
            "chargingReturnUrl" to chargingReturnUrl,
            "homeUrl" to homeUrl,
            "lockUrl" to lockUrl,
            "homeToLockUrl" to homeToLockUrl,
            "lockToHomeUrl" to lockToHomeUrl,
            "homeToChargingUrl" to homeToChargingUrl,
            "lockToChargingUrl" to lockToChargingUrl
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any?>?): AdvancedConfig {
            if (map == null) return AdvancedConfig()
            return AdvancedConfig(
                primaryUrl = map["primaryUrl"] as? String ?: "",
                chargingEntryUrl = map["chargingEntryUrl"] as? String,
                chargingLoopUrl = map["chargingLoopUrl"] as? String,
                chargingReturnUrl = map["chargingReturnUrl"] as? String,
                homeUrl = map["homeUrl"] as? String ?: "",
                lockUrl = map["lockUrl"] as? String ?: "",
                homeToLockUrl = map["homeToLockUrl"] as? String,
                lockToHomeUrl = map["lockToHomeUrl"] as? String,
                homeToChargingUrl = map["homeToChargingUrl"] as? String,
                lockToChargingUrl = map["lockToChargingUrl"] as? String
            )
        }
    }
}

data class Wallpaper(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val contentType: ContentType = ContentType.STATIC,
    val liveExperienceType: LiveExperienceType? = null,
    val accessType: AccessType = AccessType.FREE,
    val status: WallpaperStatus = WallpaperStatus.DRAFT,
    val categoryId: String = "",
    val tags: List<String> = emptyList(),
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val isNew: Boolean = true,
    val sortOrder: Int = 0,
    val thumbnailUrl: String = "",
    val previewUrl: String = "",
    val primaryMediaUrl: String = "",
    val hasAudio: Boolean = false,
    val audioCodec: String? = null,
    val durationMs: Long? = null,
    val width: Int? = null,
    val height: Int? = null,
    val fps: Int? = null,
    val fileSizeBytes: Long? = null,
    val viewsCount: Long = 0,
    val appliesCount: Long = 0,
    val favoritesCount: Long = 0,
    val advancedConfig: AdvancedConfig = AdvancedConfig(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val createdBy: String = "",
    val updatedBy: String = ""
)
