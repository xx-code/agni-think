package usecases.goals.dto

import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import java.time.LocalDate
import java.util.UUID

data class CreateGoalInput(
    val title: String,
    val description: String,
    val targetAmount: Double,
    val targetSourceId: UUID,
    val targetDate: LocalDate,
    val status: GoalStatusType,
    val type: GoalEvaluationType
)
