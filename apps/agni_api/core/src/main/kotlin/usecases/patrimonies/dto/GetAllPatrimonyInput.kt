package usecases.patrimonies.dto

import adapters.dto.QueryFilter

data class GetAllPatrimonyInput(
    val queryFilter: QueryFilter
)
