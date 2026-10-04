package usecases.funds.dto

import adapters.dto.QueryFilter
import domain.enums.FundType

data class GetAllFundInput(
    var queryFilter: QueryFilter,
    val type: FundType? = null
)