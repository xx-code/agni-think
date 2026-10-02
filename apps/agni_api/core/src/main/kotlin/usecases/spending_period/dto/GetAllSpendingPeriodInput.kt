package usecases.spending_period.dto

import adapters.dto.QueryFilter
import domain.enums.SpendingPeriodStateType
import java.time.LocalDate
import java.util.UUID

data class GetAllSpendingPeriodInput(
    val queryFilter: QueryFilter,
    val spendingPeriodTemplateId: UUID? = null,
    val state: domain.enums.SpendingPeriodStateType? = null,
)