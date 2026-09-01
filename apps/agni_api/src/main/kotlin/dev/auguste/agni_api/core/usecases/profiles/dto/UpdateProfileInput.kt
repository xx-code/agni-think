package dev.auguste.agni_api.core.usecases.profiles.dto

import java.util.UUID

data class UpdateProfileInput(
    val id: UUID,
    val maxWishlistAmount: Double? = null,
    val fixSpendPercentage: Double? = null,
    val varialSpendPercentage: Double? = null,
    val savingPercentage: Double? = null
)
