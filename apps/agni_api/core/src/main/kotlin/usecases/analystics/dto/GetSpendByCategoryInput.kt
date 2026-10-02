package usecases.analystics.dto

import adapters.dto.QueryFilter
import domain.enums.PeriodType
import java.time.LocalDateTime

data class GetSpendByCategoryInput(
    val period: domain.enums.PeriodType,
    val interval: Int,
    val startDate: LocalDateTime,
    val query: QueryFilter,
)
