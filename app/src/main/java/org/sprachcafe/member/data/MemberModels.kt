package org.sprachcafe.member.data

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
    val coffeePercent: Float get() = (coffeeRemaining.toFloat() / maxOf(1, coffeeTotal).toFloat()).coerceIn(0f, 1f)
}
