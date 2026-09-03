package dev.auguste.agni_api.core.usecases.spending_period.dto

import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class UpdateSpendingPeriodInput(
    val id: UUID,
    val spendingPeriodTemplateId: UUID? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val suggestionAmount: Double? = null,
    val savingsTarget: Double? = null,
    val state: SpendingPeriodStateType? = null,
    val totalExpectedIncome: Double? = null,
    val totalExpectedExpenses: Double? = null,
    val wantSpendingItems: List<SpendingPeriodItemInput>? = null
)
