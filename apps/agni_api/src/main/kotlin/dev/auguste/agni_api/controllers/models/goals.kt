package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.GoalEvaluationType
import dev.auguste.agni_api.core.entities.enums.GoalStatusType
import dev.auguste.agni_api.core.usecases.goals.dto.CreateGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.UpdateGoalInput
import java.time.LocalDate
import java.util.UUID
import kotlin.String

data class ApiCreateGoal(
    val title: String,
    val description: String,
    val targetAmount: Double,
    val targetSourceId: UUID,
    val targetDate: LocalDate,
    val status: Int,
    val type: String
)

data class ApiUpdateGoal(
    val title: String?,
    val description: String?,
    val targetAmount: Double?,
    val targetDate: LocalDate?,
    val status: Int?
)

data class ApiGaolQueryExtend(
    val sourceId: UUID?,
    val status: Int?,
    val type: String?
)

fun mapApiCreateGoal(apiCreate: ApiCreateGoal): CreateGoalInput {
    return CreateGoalInput(
        title= apiCreate.title,
        description= apiCreate.description,
        targetAmount= apiCreate.targetAmount,
        targetSourceId= apiCreate.targetSourceId,
        targetDate= apiCreate.targetDate,
        status= GoalStatusType.fromInt(apiCreate.status),
        type= GoalEvaluationType.fromString(apiCreate.type)
    )
}

fun mapApiUpdateGoal(id: UUID, apiUpdate: ApiUpdateGoal): UpdateGoalInput {
    return UpdateGoalInput(
        id = id,
        title = apiUpdate.title,
        description = apiUpdate.description,
        targetAmount = apiUpdate.targetAmount,
        targetDate = apiUpdate.targetDate,
        status = apiUpdate.status?.let { GoalStatusType.fromInt(apiUpdate.status) }
    )
}