package usecases.budgets.dto

import adapters.dto.QueryFilter
import domain.enums.PeriodType

data class GetAllBudgetInput(
    val query: QueryFilter,
    val periodTypes: Set<PeriodType>? = null,
    val loadBalance: Boolean = true
)
