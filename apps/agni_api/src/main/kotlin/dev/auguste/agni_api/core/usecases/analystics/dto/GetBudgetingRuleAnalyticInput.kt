package dev.auguste.agni_api.core.usecases.analystics.dto

import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDateTime

data class GetBudgetingRuleAnalyticInput(
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val period: PeriodType? = PeriodType.MONTH,
    val interval: Int = 0
)