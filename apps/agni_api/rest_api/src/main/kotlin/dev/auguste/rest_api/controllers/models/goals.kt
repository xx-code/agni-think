package dev.auguste.rest_api.controllers.models

import domain.enums.GoalEvaluationType
import domain.enums.GoalStatusType
import domain.enums.PeriodType
import domain.value_objects.SchedulerRecurrence
import usecases.goals.dto.CreateGoalInput
import usecases.goals.dto.UpdateGoalInput
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
    val type: String,
    val repeater: ApiScheduleRepeaterModel? = null,
)

data class ApiUpdateGoal(
    val title: String?,
    val description: String?,
    val targetAmount: Double?,
    val targetDate: LocalDate?,
    val repeater: ApiScheduleRepeaterModel? = null,
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
        type= GoalEvaluationType.fromString(apiCreate.type),
        recurrence = apiCreate.repeater?.let {
            SchedulerRecurrence(PeriodType.fromString(it.period), it.interval)
        }
    )
}

fun mapApiUpdateGoal(id: UUID, apiUpdate: ApiUpdateGoal): UpdateGoalInput {
    return UpdateGoalInput(
        id = id,
        title = apiUpdate.title,
        description = apiUpdate.description,
        targetAmount = apiUpdate.targetAmount,
        targetDate = apiUpdate.targetDate,
        status = apiUpdate.status?.let { GoalStatusType.fromInt(apiUpdate.status) },
        recurrence = apiUpdate.repeater?.let {
            SchedulerRecurrence(PeriodType.fromString(it.period), it.interval)
        }
    )
}