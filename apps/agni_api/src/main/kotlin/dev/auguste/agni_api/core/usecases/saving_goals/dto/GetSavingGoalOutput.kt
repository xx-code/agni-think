package dev.auguste.agni_api.core.usecases.savingGoals.dto

import dev.auguste.agni_api.core.entities.enums.ImportanceGoalType
import dev.auguste.agni_api.core.entities.enums.IntensityEmotionalDesirType
import java.time.LocalDate
import java.util.UUID

data class GetSavingGoalItemOutput(
    val title: String,
    val url: String,
    val price: Double
)

data class GetSavingGoalOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val target: Double,
    val balance: Double,
    val desireValue: IntensityEmotionalDesirType,
    val importance: ImportanceGoalType,
    val wishDueDate: LocalDate?,
    val accountId: UUID?,
    val items: List<GetSavingGoalItemOutput>
)
