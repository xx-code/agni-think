package usecases.analystics.dto

import adapters.dto.QueryFilter
import domain.enums.PeriodType
import java.time.LocalDateTime
import java.util.UUID

data class GetSpendByTagInput(
    val period: domain.enums.PeriodType,
    val interval: Int,
    val startDate: LocalDateTime,
    val query: QueryFilter,
    val categoryId: UUID?
)
