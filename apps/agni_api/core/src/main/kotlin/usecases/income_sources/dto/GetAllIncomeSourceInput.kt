package usecases.income_sources.dto

import adapters.dto.QueryFilter

data class GetAllIncomeSourceInput(
    val queryFilter: QueryFilter
)
