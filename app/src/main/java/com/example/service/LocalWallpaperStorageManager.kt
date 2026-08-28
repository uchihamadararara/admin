package com.example.service

import android.content.Context
import com.example.model.OfflineBundleManifest
import java.io.File

class LocalWallpaperStorageManager(private val context: Context) {

    private val baseDir: File = File(context.filesDir, "wallpapers")
    private val stagingDir: File = File(baseDir, "staging")
    private val activeDir: File = File(baseDir, "active")

    init {
        if (!stagingDir.exists()) stagingDir.mkdirs()
        if (!activeDir.exists()) activeDir.mkdirs()
    }

    fun getStagingFolder(wallpaperId: String): File {
        val folder = File(stagingDir, wallpaperId)
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    fun getActiveFolder(wallpaperId: String): File {
        return File(activeDir, wallpaperId)
    }

    fun getActiveManifest(wallpaperId: String): File {
        return File(getActiveFolder(wallpaperId), "manifest.json")
    }

    /**
     * Promotes staged bundle to active atomically after full download verification.
     */
    fun promoteStagedToActive(wallpaperId: String): Boolean {
        val staged = getStagingFolder(wallpaperId)
        val active = getActiveFolder(wallpaperId)

        if (!staged.exists() || staged.listFiles().isNullOrEmpty()) {
            return false
        }

        // Remove old active if it exists
        if (active.exists()) {
            active.deleteRecursively()
        }

        return staged.renameTo(active)
    }

    fun getMediaFile(wallpaperId: String, slotFilename: String): File? {
        val file = File(getActiveFolder(wallpaperId), slotFilename)
        return if (file.exists()) file else null
    }
}
