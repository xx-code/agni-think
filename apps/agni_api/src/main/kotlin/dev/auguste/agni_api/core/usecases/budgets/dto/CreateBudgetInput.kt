package dev.auguste.agni_api.core.usecases.budgets.dto

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import java.time.LocalDateTime
import java.util.UUID

data class BudgetScheduleInput(
    val dueDate: LocalDateTime,
    val repeater: ScheduleRepeaterInput?
)

data class CreateBudgetInput(
    val title: String,
    val target: Double,
    val schedule: BudgetScheduleInput,
    val saveGoalIds: Set<UUID>
)
