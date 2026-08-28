package com.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

enum class ContentType {
    STATIC,
    LIVE
}

enum class LiveExperienceType {
    NORMAL,
    TRANSITION
}

enum class WallpaperStatus {
    DRAFT,
    READY_FOR_REVIEW,
    PUBLISHED,
    INACTIVE,
    ARCHIVED
}

@JsonClass(generateAdapter = true)
data class MediaSlotAsset(
    val url: String,
    @Json(name = "mime_type") val mimeType: String? = "video/mp4",
    val width: Int? = null,
    val height: Int? = null,
    @Json(name = "duration_seconds") val durationSeconds: Double? = null,
    val fps: Double? = null,
    @Json(name = "has_audio") val hasAudio: Boolean = false,
    @Json(name = "audio_codec") val audioCodec: String? = null,
    @Json(name = "checksum_sha256") val checksumSha256: String? = null,
    @Json(name = "file_size_bytes") val fileSizeBytes: Long? = null
)

@JsonClass(generateAdapter = true)
data class AdvancedConfig(
    // Normal Live slots
    val primary: MediaSlotAsset? = null,
    @Json(name = "charging_entry") val chargingEntry: MediaSlotAsset? = null,
    @Json(name = "charging_loop") val chargingLoop: MediaSlotAsset? = null,
    @Json(name = "charging_return") val chargingReturn: MediaSlotAsset? = null,

    // Transition Live slots
    val home: MediaSlotAsset? = null,
    val lock: MediaSlotAsset? = null,
    @Json(name = "lock_to_home") val lockToHome: MediaSlotAsset? = null,
    @Json(name = "home_to_lock") val homeToLock: MediaSlotAsset? = null,
    @Json(name = "home_to_charging") val homeToCharging: MediaSlotAsset? = null,
    @Json(name = "lock_to_charging") val lockToCharging: MediaSlotAsset? = null,
    @Json(name = "transition_charging_loop") val transitionChargingLoop: MediaSlotAsset? = null,
    @Json(name = "transition_charging_return") val transitionChargingReturn: MediaSlotAsset? = null,

    // Static slot
    @Json(name = "primary_image") val primaryImage: MediaSlotAsset? = null
)

@JsonClass(generateAdapter = true)
data class Wallpaper(
    val id: String,
    val title: String,
    val description: String? = null,
    @Json(name = "content_type") val contentType: ContentType = ContentType.LIVE,
    @Json(name = "live_experience_type") val liveExperienceType: LiveExperienceType = LiveExperienceType.NORMAL,
    @Json(name = "category_id") val categoryId: String? = null,
    val tags: List<String> = emptyList(),
    @Json(name = "is_premium") val isPremium: Boolean = false,
    @Json(name = "is_featured") val isFeatured: Boolean = false,
    @Json(name = "is_trending") val isTrending: Boolean = false,
    @Json(name = "is_new") val isNew: Boolean = true,
    @Json(name = "sort_order") val sortOrder: Int = 0,
    val status: WallpaperStatus = WallpaperStatus.DRAFT,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "advanced_config") val advancedConfig: AdvancedConfig = AdvancedConfig(),
    @Json(name = "download_count") val downloadCount: Int = 0,
    @Json(name = "view_count") val viewCount: Int = 0,
    @Json(name = "favorite_count") val favoriteCount: Int = 0,
    @Json(name = "created_at") val createdAt: String? = null,
    @Json(name = "updated_at") val updatedAt: String? = null
)

@JsonClass(generateAdapter = true)
data class OfflineBundleManifest(
    @Json(name = "wallpaper_id") val wallpaperId: String,
    @Json(name = "experience_type") val experienceType: String,
    @Json(name = "bundle_version") val bundleVersion: Int = 1,
    @Json(name = "assets") val assets: Map<String, String>, // slot -> local relative filename
    @Json(name = "has_audio") val hasAudio: Boolean = false,
    @Json(name = "created_at") val createdAt: String
)
