package usecases.spending_period.dto

import domain.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class UpdateSpendingPeriodInput(
    val id: UUID,
    val spendingPeriodTemplateId: UUID? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val savingRateTarget: Double? = null,
    val state: SpendingPeriodStateType? = null,
    val scheduleIncomesSelected: Set<UUID>? = null,
    val scheduleExpensesSelected: Set<UUID>? = null,
    val budgetsSelected: Set<UUID>? = null,
    val wishlistItemSelected: Set<UUID>? = null,
)
