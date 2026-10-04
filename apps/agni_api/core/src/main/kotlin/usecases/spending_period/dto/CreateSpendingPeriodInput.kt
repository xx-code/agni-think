package usecases.spending_period.dto

import domain.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class CreateSpendingPeriodInput(
    val spendingPeriodTemplateId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val freeAmount: Double,
    val savingRateTarget: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpenses: Double,
    val state: SpendingPeriodStateType,
    val wantSpendingItems: List<SpendingPeriodItemInput>
)
