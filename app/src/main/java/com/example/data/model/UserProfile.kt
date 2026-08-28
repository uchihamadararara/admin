package com.example.data.model

enum class SubscriptionTier {
    FREE,
    VIP;

    companion object {
        fun fromString(value: String?): SubscriptionTier {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: FREE
        }
    }
}

enum class SubscriptionStatus {
    INACTIVE,
    ACTIVE,
    EXPIRED,
    CANCELLED;

    companion object {
        fun fromString(value: String?): SubscriptionStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: INACTIVE
        }
    }
}

enum class AccountStatus {
    ACTIVE,
    SUSPENDED,
    BANNED;

    companion object {
        fun fromString(value: String?): AccountStatus {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ACTIVE
        }
    }
}

data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.INACTIVE,
    val subscriptionExpiry: Long? = null,
    val activeSku: String? = null,
    val currentAppliedWallpaperId: String? = null,
    val currentAppliedWallpaperTitle: String? = null,
    val isAppliedWallpaperPremium: Boolean = false,
    val appliedWallpaperSoundEnabled: Boolean = false,
    val accountStatus: AccountStatus = AccountStatus.ACTIVE,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long? = null
)
