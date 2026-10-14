package usecases.deductions.dto

import adapters.dto.QueryFilter

data class GetAllDeductionInput(
    val queryFilter: QueryFilter,
)
