package dev.auguste.agni_api.core.usecases.saving_goals.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.FundType

data class GetAllSavingGoalInput(
    var queryFilter: QueryFilter,
    val type: FundType? = null
)