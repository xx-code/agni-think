package dev.auguste.agni_api.core.usecases.savingGoals.dto

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import java.time.LocalDate
import java.util.UUID

data class ItemSavingGoalInput(
    val title: String,
    val price: Double,
    val url: String
)

data class CreateSavingGoalInput(
    val target: Double,
    val title: String,
    val description: String,
    val accountId: UUID?,
    val desirValue: IntensityEmotionalDesirType,
    val importance: ImportanceGoalType,
    val wishDueDate: LocalDate?,
    val items: Set<ItemSavingGoalInput>
)
