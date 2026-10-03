package adapters.dto

import domain.enums.PeriodType

data class ScheduleRepeaterInput(
    val period: PeriodType,
    val interval: Int
)
