package usecases.finance_principles.dto

import adapters.dto.QueryFilter

data class GetAllFinancePrincipleInput(
    val queryFilter: QueryFilter
)
