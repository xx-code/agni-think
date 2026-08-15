package dev.auguste.agni_api.core.usecases.budgets.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.PeriodType

data class GetAllBudgetInput(
    val query: QueryFilter,
    val periodTypes: Set<PeriodType>? = null
)
