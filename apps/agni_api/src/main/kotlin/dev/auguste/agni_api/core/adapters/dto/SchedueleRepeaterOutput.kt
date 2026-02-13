package dev.auguste.agni_api.core.adapters.dto

import dev.auguste.agni_api.core.entities.enums.PeriodType

data class ScheduleRepeaterOutput(
    val period: String,
    val interval: Int
)
