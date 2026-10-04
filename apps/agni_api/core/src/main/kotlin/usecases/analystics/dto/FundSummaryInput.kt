package usecases.analystics.dto

import domain.enums.FundType

data class FundSummaryInput(
    val type: FundType? = null
)