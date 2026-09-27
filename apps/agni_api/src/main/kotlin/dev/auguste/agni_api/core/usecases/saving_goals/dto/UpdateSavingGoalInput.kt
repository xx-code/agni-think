package dev.auguste.agni_api.core.usecases.saving_goals.dto

import dev.auguste.agni_api.core.entities.enums.FundType
import java.util.UUID

data class UpdateSavingGoalInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val description: String?,
    val type: FundType?,
    val accountId: UUID?
    )
