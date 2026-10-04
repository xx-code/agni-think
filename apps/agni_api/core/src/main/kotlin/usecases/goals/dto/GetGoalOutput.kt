package usecases.goals.dto

import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
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
