package com.example.service

import com.example.model.AdvancedConfig
import com.example.model.LiveExperienceType

sealed class WallpaperState {
    object Hidden : WallpaperState()
    object HomeScreen : WallpaperState()
    object LockScreen : WallpaperState()
    object LockToHomeTransition : WallpaperState()
    object HomeToLockTransition : WallpaperState()
    object HomeToChargingTransition : WallpaperState()
    object LockToChargingTransition : WallpaperState()
    object ChargingLoop : WallpaperState()
    object ChargingReturnToHome : WallpaperState()
    object ChargingReturnToLock : WallpaperState()
}

class WallpaperStateMachine(
    private val experienceType: LiveExperienceType,
    private val config: AdvancedConfig
) {
    var currentState: WallpaperState = WallpaperState.HomeScreen
        private set

    /**
     * Called when device is unlocked (Lock -> Home)
     */
    fun onUnlock(): WallpaperState {
        currentState = if (experienceType == LiveExperienceType.TRANSITION && config.lockToHome != null) {
            WallpaperState.LockToHomeTransition
        } else {
            WallpaperState.HomeScreen
        }
        return currentState
    }

    /**
     * Called when device is locked (Home -> Lock)
     */
    fun onLock(): WallpaperState {
        currentState = if (experienceType == LiveExperienceType.TRANSITION) {
            if (config.homeToLock != null) {
                WallpaperState.HomeToLockTransition
            } else if (config.lock != null) {
                WallpaperState.LockScreen
            } else {
                WallpaperState.HomeScreen
            }
        } else {
            WallpaperState.HomeScreen
        }
        return currentState
    }

    /**
     * Called when device is plugged into charger
     */
    fun onChargerConnected(isDeviceLocked: Boolean): WallpaperState {
        currentState = if (experienceType == LiveExperienceType.NORMAL) {
            if (config.chargingEntry != null) {
                WallpaperState.HomeToChargingTransition
            } else if (config.chargingLoop != null) {
                WallpaperState.ChargingLoop
            } else {
                WallpaperState.HomeScreen
            }
        } else {
            // TRANSITION experience
            if (isDeviceLocked) {
                if (config.lockToCharging != null) {
                    WallpaperState.LockToChargingTransition
                } else if (config.transitionChargingLoop != null) {
                    WallpaperState.ChargingLoop
                } else {
                    if (config.lock != null) WallpaperState.LockScreen else WallpaperState.HomeScreen
                }
            } else {
                if (config.homeToCharging != null) {
                    WallpaperState.HomeToChargingTransition
                } else if (config.transitionChargingLoop != null) {
                    WallpaperState.ChargingLoop
                } else {
                    WallpaperState.HomeScreen
                }
            }
        }
        return currentState
    }

    /**
     * Called when charger is disconnected
     */
    fun onChargerDisconnected(isDeviceLocked: Boolean): WallpaperState {
        currentState = if (experienceType == LiveExperienceType.NORMAL) {
            if (config.chargingReturn != null) {
                WallpaperState.ChargingReturnToHome
            } else {
                WallpaperState.HomeScreen
            }
        } else {
            if (config.transitionChargingReturn != null) {
                if (isDeviceLocked) WallpaperState.ChargingReturnToLock else WallpaperState.ChargingReturnToHome
            } else {
                if (isDeviceLocked && config.lock != null) WallpaperState.LockScreen else WallpaperState.HomeScreen
            }
        }
        return currentState
    }

    /**
     * Called when a transition video completes playback
     */
    fun onTransitionCompleted(): WallpaperState {
        currentState = when (currentState) {
            WallpaperState.LockToHomeTransition -> WallpaperState.HomeScreen
            WallpaperState.HomeToLockTransition -> if (config.lock != null) WallpaperState.LockScreen else WallpaperState.HomeScreen
            WallpaperState.HomeToChargingTransition,
            WallpaperState.LockToChargingTransition -> WallpaperState.ChargingLoop
            WallpaperState.ChargingReturnToHome -> WallpaperState.HomeScreen
            WallpaperState.ChargingReturnToLock -> if (config.lock != null) WallpaperState.LockScreen else WallpaperState.HomeScreen
            else -> currentState
        }
        return currentState
    }

    fun onVisibilityChanged(visible: Boolean): WallpaperState {
        if (!visible) {
            currentState = WallpaperState.Hidden
        }
        return currentState
    }
}
