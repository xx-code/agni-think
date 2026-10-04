package usecases.spending_period_template.dto

import adapters.dto.ScheduleRepeaterInput
import java.time.LocalDate
import java.util.UUID

data class CreateSpendingPeriodTemplateInput(
    val recurrence: ScheduleRepeaterInput,
    val startDate: LocalDate,
    val targetBudgetIds: Set<UUID> = setOf(),
    val endDate: LocalDate? = null
)