package usecases.saving_goals.dto

import adapters.dto.QueryFilter
import domain.enums.FundType

data class GetAllSavingGoalInput(
    var queryFilter: QueryFilter,
    val type: domain.enums.FundType? = null
)