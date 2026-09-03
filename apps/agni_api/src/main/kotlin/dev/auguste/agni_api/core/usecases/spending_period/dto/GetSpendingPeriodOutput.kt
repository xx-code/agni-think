package dev.auguste.agni_api.core.usecases.spending_period.dto

import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class GetSpendingPeriodOutput(
    val id: UUID,
    val spendingPeriodTemplateId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val suggestionAmount: Double,
    val savingsTarget: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpenses: Double,
    val state: SpendingPeriodStateType,
    val wantSpendingItems: List<SpendingPeriodItemOutput>
)
