package adapters.dto

import domain.enums.PeriodType

data class ScheduleRepeaterInput(
    val period: domain.enums.PeriodType,
    val interval: Int
)
