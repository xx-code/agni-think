package dev.auguste.agni_api.core.usecases.saving_goals.dto

import java.util.UUID

data class UpdateSavingGoalInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val description: String?,
    val accountId: UUID?
    )
