package dev.auguste.agni_api.core.usecases.saving_goals.dto

import java.util.UUID

data class CreateSavingGoalInput(
    val target: Double,
    val title: String,
    val description: String,
    val accountId: UUID?
)
