package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.entities.enums.PeriodType
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.CreateSpendingPeriodTemplateInput
import dev.auguste.agni_api.core.usecases.spending_period_template.dto.UpdateSpendingPeriodTemplateInput
import java.time.LocalDate
import java.util.UUID

data class ApiCreateSpendingPeriodTemplateModel(
    val startDate: LocalDate,
    val recurrence: ApiScheduleRepeaterModel,
    val endDate: LocalDate?
)

data class ApiUpdateSpendingPeriodTemplateModel(
    val startDate: LocalDate?,
    val isActive: Boolean?,
    val recurrence: ApiScheduleRepeaterModel?,
    val endDate: LocalDate?
)

fun mapApiCreateSpendingPeriodTemplateToSpendingPeriodTemplate(request: ApiCreateSpendingPeriodTemplateModel) : CreateSpendingPeriodTemplateInput {
    return CreateSpendingPeriodTemplateInput(
        recurrence = ScheduleRepeaterInput(
            period = PeriodType.fromString(request.recurrence.period),
            interval = request.recurrence.interval
        ),
        startDate = request.startDate,
        endDate = request.endDate
    )
}

fun mapApiUpdateSpendingPeriodTemplateToSpendingPeriodTemplate(id: UUID, request: ApiUpdateSpendingPeriodTemplateModel): UpdateSpendingPeriodTemplateInput{
    return UpdateSpendingPeriodTemplateInput(
        id = id,
        recurrence = request.recurrence?.let {
            ScheduleRepeaterInput(
                period = PeriodType.fromString(it.period),
                interval = it.interval
            )
        },
        isActive = request.isActive,
        startDate = request.startDate,
        endDate = request.endDate,
    )
}