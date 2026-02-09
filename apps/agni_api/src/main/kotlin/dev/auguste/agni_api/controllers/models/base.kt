package dev.auguste.agni_api.controllers.models

import dev.auguste.agni_api.core.entities.enums.PeriodType
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class ApiScheduleRepeaterModel(
    @field:NotNull("Period type must be defined")
    val period: PeriodType,
    @field:Min(1, "Interval must be greater than or equal to 1")
    val interval: Int
)