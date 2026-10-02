package usecases.analystics.dto

import domain.enums.PeriodType
import java.time.LocalDateTime

data class GetSavingAnalyticInput(
    val period: domain.enums.PeriodType,
    val interval: Int,
    val startDate: LocalDateTime
)
