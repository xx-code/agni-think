package dev.auguste.agni_api.core.usecases.saving_goals.dto

import java.util.UUID

data class DecreaseSavingGoalInput(
    val savingGoalId: UUID,
    val accountId: UUID?,
    val amount: Double
)

data class IncreaseSavingGoalInput(
    val savingGoalId: UUID,
    val accountId: UUID?,
    val amount: Double
)
