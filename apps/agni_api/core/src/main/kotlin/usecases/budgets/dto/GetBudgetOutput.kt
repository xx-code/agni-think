package usecases.budgets.dto

import adapters.dto.ScheduleRepeaterOutput
import java.time.LocalDateTime
import java.util.UUID

data class GetBudgetOutput (
    val id: UUID,
    val title: String,
    val target: Double,
    val currentBalance: Double,
    val dueDate: LocalDateTime,
    val repeater: ScheduleRepeaterOutput?
)