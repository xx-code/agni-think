package dev.auguste.agni_api.core.usecases.spending_period.dto

import java.time.LocalDate
import java.util.UUID

data class CreateSpendingPeriodInput(
    val spendingPeriodTemplateId: UUID,
    val suggestionAmount: Double,
    val amount: Double,
    val wantSpendingItems: List<SpendingPeriodItemInput>
)
