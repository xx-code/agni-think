package dev.auguste.agni_api.core.usecases.saving_goals.dto

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import java.time.LocalDate
import java.util.UUID

data class UpdateSavingGoalInput(
    val id: UUID,
    val title: String?,
    val target: Double?,
    val description: String?,
    val accountId: UUID?,
    val wishDueDate: LocalDate?,
    val items: Set<ItemSavingGoalInput>?,
    val desirValue: IntensityEmotionalDesirType?,
    val importance: ImportanceGoalType?
    )
