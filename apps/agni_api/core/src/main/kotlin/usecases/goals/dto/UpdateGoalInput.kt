package usecases.goals.dto

import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.value_objects.SchedulerRecurrence
import java.time.LocalDate
import java.util.UUID

data class UpdateGoalInput(
    val id: UUID,
    val title: String?,
    val description: String?,
    val targetAmount: Double?,
    val targetDate: LocalDate?,
    val status: GoalStatusType?,
    val recurrence: SchedulerRecurrence? = null
)