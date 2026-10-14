package usecases.spending_period.dto

import java.time.LocalDate
import java.util.UUID

data class ForcastSpendingPeriodInput(
    val userId: UUID,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val scheduleInvoiceIds: Set<UUID>,
    val budgetIds: Set<UUID>,
    val savingRate: Double?
)
