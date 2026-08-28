package com.example.data.model

data class CanonicalSku(
    val skuId: String,
    val name: String,
    val durationDescription: String,
    val googlePlayConfigured: Boolean = false,
    val status: String = "Awaiting Google Play setup"
) {
    companion object {
        val CANONICAL_LIST = listOf(
            CanonicalSku("vip_3days", "VIP 3 Days", "3 Days full VIP pass"),
            CanonicalSku("vip_7days", "VIP 7 Days", "1 Week VIP pass"),
            CanonicalSku("vip_14days", "VIP 14 Days", "2 Weeks VIP pass"),
            CanonicalSku("vip_1month", "VIP 1 Month", "1 Month VIP subscription"),
            CanonicalSku("vip_lifetime", "VIP Lifetime", "Lifetime VIP unlock")
        )
    }
}

enum class VerificationState {
    UNVERIFIED,
    VERIFIED,
    PENDING_WORKER,
    FAILED;

    companion object {
        fun fromString(value: String?): VerificationState {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: UNVERIFIED
        }
    }
}

data class BillingEvent(
    val id: String = "",
    val uid: String = "",
    val sku: String = "",
    val orderId: String = "",
    val purchaseToken: String = "",
    val purchaseTime: Long = System.currentTimeMillis(),
    val verificationState: VerificationState = VerificationState.UNVERIFIED,
    val amount: String = "",
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis()
)

data class AdMobSSVEvent(
    val id: String = "",
    val uid: String = "",
    val rewardType: String = "",
    val rewardAmount: Int = 0,
    val customData: String? = null,
    val verificationState: VerificationState = VerificationState.UNVERIFIED,
    val timestamp: Long = System.currentTimeMillis()
)
