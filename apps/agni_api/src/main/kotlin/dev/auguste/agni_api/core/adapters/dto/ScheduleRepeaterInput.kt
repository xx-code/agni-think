package dev.auguste.agni_api.core.adapters.dto

import dev.auguste.agni_api.core.entities.enums.PeriodType

data class ScheduleRepeaterInput(
    val period: PeriodType,
    val interval: Int
)
