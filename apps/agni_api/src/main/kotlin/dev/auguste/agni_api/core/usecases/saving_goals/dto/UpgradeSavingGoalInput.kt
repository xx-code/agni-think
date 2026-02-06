package dev.auguste.agni_api.core.usecases.saving_goals.dto

import java.util.UUID

data class UpgradeSavingGoalInput(
    val savingGoalId: UUID,
    val accountId: UUID?,
    val amount: Double
)
