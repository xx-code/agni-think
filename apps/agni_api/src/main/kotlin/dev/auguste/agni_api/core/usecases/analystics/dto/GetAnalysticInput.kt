package dev.auguste.agni_api.core.usecases.analystics.dto

import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDateTime

data class GetAnalysticInput(
    val period: PeriodType,
    val interval: Int,
    val startDate: LocalDateTime,
)
