package com.example.service

import android.content.Context
import android.content.SharedPreferences

class SoundManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("applied_wallpaper_sound_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_APPLIED_WALLPAPER_SOUND_PREFIX = "APPLIED_WALLPAPER_SOUND_"
    }

    /**
     * Retrieves whether sound is enabled for the specific applied wallpaper ID.
     * Defaults to false (muted).
     */
    fun isSoundEnabledForWallpaper(wallpaperId: String): Boolean {
        return prefs.getBoolean(KEY_APPLIED_WALLPAPER_SOUND_PREFIX + wallpaperId, false)
    }

    /**
     * Saves user sound preference when applying a specific wallpaper.
     */
    fun setSoundEnabledForWallpaper(wallpaperId: String, enabled: Boolean) {
        prefs.edit().putBoolean(KEY_APPLIED_WALLPAPER_SOUND_PREFIX + wallpaperId, enabled).apply()
    }

    /**
     * Computes the volume level (0.0f to 1.0f).
     * Must be 0.0f whenever wallpaper is hidden or screen is off.
     */
    fun getEffectiveVolume(wallpaperId: String, isWallpaperVisible: Boolean, hasAudioMedia: Boolean): Float {
        if (!isWallpaperVisible || !hasAudioMedia) {
            return 0.0f
        }
        return if (isSoundEnabledForWallpaper(wallpaperId)) 1.0f else 0.0f
    }
}
