package dev.auguste.agni_api.core.usecases.currencies.dto

import java.util.UUID

data class UpdateCurrencyInput(
    val id: UUID,
    val name: String?,
    val symbol: String?,
    val locale: String?,
    val rateToBase: Double?,
    val isBase: Boolean?
)
