package usecases.analystics.dto

import domain.enums.PeriodType

data class GetPatrimonyEvolutionInput(
    val periodType: PeriodType,
    val interval: Int
)