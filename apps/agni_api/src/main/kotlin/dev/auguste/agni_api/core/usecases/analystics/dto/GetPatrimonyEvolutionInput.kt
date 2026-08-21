package dev.auguste.agni_api.core.usecases.analystics.dto

import dev.auguste.agni_api.core.entities.enums.PeriodType

data class GetPatrimonyEvolutionInput(
    val periodType: PeriodType,
    val interval: Int
)