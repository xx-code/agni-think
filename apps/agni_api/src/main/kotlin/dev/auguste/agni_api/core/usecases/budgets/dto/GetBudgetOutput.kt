package dev.auguste.agni_api.core.usecases.budgets.dto

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterOutput
import java.time.LocalDateTime
import java.util.UUID

data class GetBudgetOutput (
    val id: UUID,
    val title: String,
    val target: Double,
    val realTarget: Double,
    val savingGoalTarget: Double,
    val savingGoalIds: Set<UUID>,
    val currentBalance: Double,
    val dueDate: LocalDateTime,
    val repeater: ScheduleRepeaterOutput?
)