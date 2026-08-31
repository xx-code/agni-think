package dev.auguste.agni_api.core.usecases.profiles.dto

data class UpdateProfileInput(
    val maxWishlistAmount: Double? = null,
    val fixSpendPercentage: Double? = null,
    val varialSpendPercentage: Double? = null,
    val savingPercentage: Double? = null
)
