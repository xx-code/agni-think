package usecases.spending_period.dto

import domain.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class CreateSpendingPeriodInput(
    val spendingPeriodTemplateId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val savingRateTarget: Double,
    val state: SpendingPeriodStateType,
    val scheduleIncomesSelected: Set<UUID>,
    val scheduleExpensesSelected: Set<UUID>,
    val budgetsSelected: Set<UUID>,
    val wishlistItemSelected: Set<UUID>
)
