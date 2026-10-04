package usecases.budgets.dto

import adapters.dto.ScheduleRepeaterInput
import java.time.LocalDateTime

data class BudgetScheduleInput(
    val dueDate: LocalDateTime,
    val repeater: ScheduleRepeaterInput?
)

data class CreateBudgetInput(
    val title: String,
    val target: Double,
    val schedule: BudgetScheduleInput,
)
