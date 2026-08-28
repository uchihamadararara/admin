package com.example.service

import android.app.KeyguardManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.model.AdvancedConfig
import com.example.model.LiveExperienceType

class AdvancedWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LiveWallpaperEngine()
    }

    inner class LiveWallpaperEngine : Engine() {
        private var mediaPlayer: MediaPlayer? = null
        private var stateMachine: WallpaperStateMachine? = null
        private lateinit var soundManager: SoundManager
        private lateinit var storageManager: LocalWallpaperStorageManager
        private lateinit var keyguardManager: KeyguardManager

        private var isVisible: Boolean = false
        private var isCharging: Boolean = false
        private var activeWallpaperId: String = "default"

        private val powerReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_POWER_CONNECTED -> {
                        isCharging = true
                        val isLocked = keyguardManager.isKeyguardLocked
                        stateMachine?.onChargerConnected(isLocked)
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        isCharging = false
                        val isLocked = keyguardManager.isKeyguardLocked
                        stateMachine?.onChargerDisconnected(isLocked)
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        stateMachine?.onVisibilityChanged(false)
                        pauseAndMutePlayback()
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        stateMachine?.onUnlock()
                    }
                }
            }
        }

        override fun onCreate(surfaceHolder: SurfaceHolder) {
            super.onCreate(surfaceHolder)
            soundManager = SoundManager(applicationContext)
            storageManager = LocalWallpaperStorageManager(applicationContext)
            keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager

            // Initialize baseline normal machine
            stateMachine = WallpaperStateMachine(
                experienceType = LiveExperienceType.NORMAL,
                config = AdvancedConfig()
            )

            val filter = IntentFilter().apply {
                addAction(Intent.ACTION_POWER_CONNECTED)
                addAction(Intent.ACTION_POWER_DISCONNECTED)
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            registerReceiver(powerReceiver, filter)
        }

        override fun onVisibilityChanged(visible: Boolean) {
            this.isVisible = visible
            stateMachine?.onVisibilityChanged(visible)
            if (visible) {
                resumePlayback()
            } else {
                pauseAndMutePlayback()
            }
        }

        private fun resumePlayback() {
            mediaPlayer?.let { player ->
                if (!player.isPlaying) {
                    player.start()
                }
                val vol = soundManager.getEffectiveVolume(activeWallpaperId, isWallpaperVisible = true, hasAudioMedia = true)
                player.setVolume(vol, vol)
            }
        }

        private fun pauseAndMutePlayback() {
            mediaPlayer?.let { player ->
                if (player.isPlaying) {
                    player.pause()
                }
                // Strictly mute audio when hidden
                player.setVolume(0.0f, 0.0f)
            }
        }

        override fun onDestroy() {
            super.onDestroy()
            try {
                unregisterReceiver(powerReceiver)
            } catch (_: Exception) {}
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }
}
