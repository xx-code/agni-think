package usecases.accounts.dto

import adapters.dto.QueryFilter
import java.util.UUID

data class GetAllAccountInput(
    val queryFilter: QueryFilter
)
