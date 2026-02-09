package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.usecases.budgets.dto.BudgetScheduleInput
import dev.auguste.agni_api.core.usecases.budgets.dto.CreateBudgetInput
import dev.auguste.agni_api.core.usecases.budgets.dto.UpdateBudgetInput
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime
import java.util.UUID

data class ApiBudgeScheduleInput(
    @field:NotBlank("Date must be set")
    val dueDate: LocalDateTime,
    val repeater: ApiScheduleRepeaterModel?
)

data class ApiCreateBudgetModel(
    @field:NotEmpty("Name must not be empty")
    val title: String,

    @field:NotEmpty("Description must not be empty")
    val target: Double,

    val schedule: ApiBudgeScheduleInput,
    val savingGoalIds: Set<UUID>
)

data class ApiUpdateBudgetModel(
    val title: String?,
    val target: Double?,
    val schedule: ApiBudgeScheduleInput?,
    val savingGoalIds: Set<UUID>?
)

fun mapApiCreateBudgetModel(model: ApiCreateBudgetModel): CreateBudgetInput {
    return CreateBudgetInput(
        title = model.title,
        target = model.target,
        schedule = BudgetScheduleInput(
            dueDate = model.schedule.dueDate,
            repeater = model.schedule.repeater?.let {
                ScheduleRepeaterInput(
                    it.period,
                    it.interval,
                )
            }
        ),
        saveGoalIds = model.savingGoalIds
    )
}

fun mapApiUpdateBudgetModel(id: UUID, model: ApiUpdateBudgetModel): UpdateBudgetInput {
    return UpdateBudgetInput(
        id = id,
        title = model.title,
        target = model.target,
        schedule = model.schedule?.let {
            BudgetScheduleInput(
                dueDate = it.dueDate,
                repeater = it.repeater?.let { repeater ->
                    ScheduleRepeaterInput(
                        repeater.period,
                        repeater.interval,
                    )
                }
            )
        },
        savingGoalIds = model.savingGoalIds
    )
}