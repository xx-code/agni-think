package dev.auguste.agni_api.core.usecases.goals.dto

import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import java.time.LocalDate
import java.util.UUID

data class UpdateGoalInput(
    val id: UUID,
    val title: String?,
    val description: String?,
    val targetAmount: Double?,
    val targetDate: LocalDate?,
    val status: GoalStatusType?
)