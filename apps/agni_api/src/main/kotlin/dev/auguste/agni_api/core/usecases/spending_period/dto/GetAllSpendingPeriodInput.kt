package dev.auguste.agni_api.core.usecases.spending_period.dto

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.entities.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class GetAllSpendingPeriodInput(
    val queryFilter: QueryFilter,
    val spendingPeriodId: UUID? = null,
    val state: SpendingPeriodStateType? = null,
)