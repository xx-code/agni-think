package usecases.goals.dto

import adapters.dto.QueryFilter
import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import java.time.LocalDate
import java.util.UUID

data class GetAllGoalInput(
    val queryFilter: QueryFilter,
    val sourceId: UUID? = null,
    val targetDate: LocalDate? = null,
    val status: domain.enums.GoalStatusType? = null,
    val type: domain.enums.GoalEvaluationType? = null
)