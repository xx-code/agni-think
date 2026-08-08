package dev.auguste.agni_api.core.usecases.goals.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import java.time.LocalDate
import java.util.UUID

data class GetAllGoalInput(
    val queryFilter: QueryFilter,
    val sourceId: UUID? = null,
    val targetDate: LocalDate? = null,
    val status: GoalStatusType? = null,
    val type: GoalEvaluationType? = null
)