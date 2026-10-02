package usecases.analystics.dto

import domain.enums.PeriodType
import java.time.LocalDateTime

data class GetBudgetingRuleAnalyticInput(
    val startDate: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val period: domain.enums.PeriodType? = _root_ide_package_.domain.enums.PeriodType.MONTH,
    val interval: Int = 0
)