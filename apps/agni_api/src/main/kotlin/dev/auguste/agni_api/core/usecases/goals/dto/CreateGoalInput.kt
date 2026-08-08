package dev.auguste.agni_api.core.usecases.goals.dto

import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import java.time.LocalDate
import java.util.UUID

data class CreateGoalInput(
    val title: String,
    val description: String,
    val targetAmount: Double,
    val targetSourceId: UUID,
    val targetDate: LocalDate,
    val startingDate: LocalDate,
    val status: GoalStatusType,
    val type: GoalEvaluationType
)
