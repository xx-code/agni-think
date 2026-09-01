package dev.auguste.agni_api.core.usecases.spending_period.dto

import java.time.LocalDate
import java.util.UUID

data class UpdateSpendingPeriodInput(
    val id: UUID,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val suggestionAmount: Double? = null,
    val amount: Double? = null,
    val wantSpendingItems: List<SpendingPeriodItemInput>? = null
)
