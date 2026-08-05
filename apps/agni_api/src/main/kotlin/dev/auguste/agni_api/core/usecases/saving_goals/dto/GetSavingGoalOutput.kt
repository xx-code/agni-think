package dev.auguste.agni_api.core.usecases.saving_goals.dto

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import java.time.LocalDate
import java.util.UUID

data class GetSavingGoalOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val target: Double,
    val balance: Double,
    val accountId: UUID?,
)
