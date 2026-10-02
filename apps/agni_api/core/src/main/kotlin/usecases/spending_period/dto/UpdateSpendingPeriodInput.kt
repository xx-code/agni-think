package usecases.spending_period.dto

import domain.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class UpdateSpendingPeriodInput(
    val id: UUID,
    val spendingPeriodTemplateId: UUID? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val freeAmount: Double? = null,
    val savingRateTarget: Double? = null,
    val state: domain.enums.SpendingPeriodStateType? = null,
    val totalExpectedIncome: Double? = null,
    val totalExpectedExpenses: Double? = null,
    val wantSpendingItems: List<SpendingPeriodItemInput>? = null
)
