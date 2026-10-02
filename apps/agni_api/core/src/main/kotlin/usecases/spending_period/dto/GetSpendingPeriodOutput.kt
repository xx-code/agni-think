package usecases.spending_period.dto

import domain.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class SpendingPeriodSnapShotOutput(
    val income: Double,
    val fixExpenses: Double,
    val variableExpenses: Double,
    val budgetExpenses: Double,
    val saving: Double
)

data class GetSpendingPeriodOutput(
    val id: UUID,
    val spendingPeriodTemplateId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val freeAmount: Double,
    val savingRateTarget: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpenses: Double,
    val state: domain.enums.SpendingPeriodStateType,
    val wantSpendingItems: List<SpendingPeriodItemOutput>,
    val snapshot: SpendingPeriodSnapShotOutput,
    val forcast: ForcastSpendingPeriodOutput?
)

data class GetAllSpendingPeriodOutput(
    val id: UUID,
    val spendingPeriodTemplateId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val freeAmount: Double,
    val savingRateTarget: Double,
    val totalExpectedIncome: Double,
    val totalExpectedExpenses: Double,
    val state: domain.enums.SpendingPeriodStateType,
    val snapshot: SpendingPeriodSnapShotOutput,
    val wantSpendingItems: List<SpendingPeriodItemOutput>,
)
