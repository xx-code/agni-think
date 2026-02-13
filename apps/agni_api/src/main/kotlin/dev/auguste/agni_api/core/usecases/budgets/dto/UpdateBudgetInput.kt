package dev.auguste.agni_api.core.usecases.budgets.dto

import java.util.UUID

data class UpdateBudgetInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val schedule: BudgetScheduleInput?,
    val savingGoalIds: Set<UUID>?
)
