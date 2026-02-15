package dev.auguste.agni_api.core.usecases.analystics.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDateTime
import java.util.UUID

data class GetSpendByTagInput(
    val period: PeriodType,
    val interval: Int,
    val startDate: LocalDateTime,
    val query: QueryFilter,
    val categoryId: UUID?
)
