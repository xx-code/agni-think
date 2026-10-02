package usecases.analystics.dto

import domain.enums.PeriodType

data class GetPatrimonyEvolutionInput(
    val periodType: domain.enums.PeriodType,
    val interval: Int
)