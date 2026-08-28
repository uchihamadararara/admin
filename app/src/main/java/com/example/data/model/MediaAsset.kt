package com.example.data.model

data class MediaAsset(
    val id: String = "",
    val url: String = "",
    val title: String = "",
    val mimeType: String = "video/mp4",
    val width: Int? = null,
    val height: Int? = null,
    val durationMs: Long? = null,
    val fps: Int? = null,
    val hasAudio: Boolean = false,
    val audioCodec: String? = null,
    val fileSizeBytes: Long? = null,
    val sha256: String? = null,
    val linkedWallpaperId: String? = null,
    val slot: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
