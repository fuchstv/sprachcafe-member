package org.sprachcafe.member.data

object MemberTierDefaults {
    const val DEFAULT_COFFEE_QUOTA = 4

    val COFFEE_QUOTA_BY_TIER: Map<String, Int> = mapOf(
        "SILVER" to 4,
        "GOLD" to 8,
        "PLATINUM" to 10,
        "COMPANY" to 10
    )

    fun getCoffeeQuota(tier: String?): Int {
        if (tier == null) return DEFAULT_COFFEE_QUOTA
        return COFFEE_QUOTA_BY_TIER[tier.uppercase()] ?: DEFAULT_COFFEE_QUOTA
    }
}

data class MemberProfile(
    val memberNumber: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val tier: String,
    val status: String,
    val coffeeRemaining: Int,
    val coffeeTotal: Int,
    val eventDiscount: Int,
    val validUntil: String?,
    val qrToken: String,
    val activatedAt: String?
) {
    val fullName: String get() = "$firstName $lastName"
    val isCoffeeAvailable: Boolean get() = coffeeRemaining > 0
}
