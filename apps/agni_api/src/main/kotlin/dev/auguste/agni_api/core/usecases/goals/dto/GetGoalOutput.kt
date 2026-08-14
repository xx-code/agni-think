package dev.auguste.agni_api.core.usecases.goals.dto

import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import java.time.LocalDate
import java.util.UUID

data class GetGoalEvaluationOutput(
    val currentBalance: Double,
    val progressPercentage: Double
)

data class GetGoalOutput(
    val id: UUID,
    val title: String,
    val description: String,
    val targetAmount: Double,
    val targetSourceId: UUID,
    val dueDate: LocalDate,
    val createdDate: LocalDate,
    val status: GoalStatusType,
    val type: GoalEvaluationType,
    val evaluation: GetGoalEvaluationOutput
)
