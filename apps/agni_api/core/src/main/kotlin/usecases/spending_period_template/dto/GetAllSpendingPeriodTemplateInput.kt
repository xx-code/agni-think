package usecases.spending_period_template.dto

import adapters.dto.QueryFilter

data class GetAllSpendingPeriodTemplateInput(
    val queryFilter: QueryFilter
)
