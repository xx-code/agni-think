package dev.auguste.agni_api.core.usecases.spending_period.dto

import java.time.LocalDate
import java.util.UUID

data class ForcastSpendingPeriodInput(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val budgetIds: List<UUID>,
    val savingRate: Double?
)
