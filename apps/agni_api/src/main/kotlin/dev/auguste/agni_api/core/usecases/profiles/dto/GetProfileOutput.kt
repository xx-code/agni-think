package dev.auguste.agni_api.core.usecases.profiles.dto

data class GetProfileOutput(
    val maxWishlistAmount: Double,
    val fixSpendPercentage: Double,
    val varialSpendPercentage: Double,
    val savingPercentage: Double
)