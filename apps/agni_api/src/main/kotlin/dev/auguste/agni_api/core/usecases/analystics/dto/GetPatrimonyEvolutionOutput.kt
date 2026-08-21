package dev.auguste.agni_api.core.usecases.analystics.dto

import java.time.LocalDate
import java.util.UUID

data class NetWorthPeriodOutput(
    val date: LocalDate,
    val networth: Double
)

data class GetPatrimonyEvolutionOutput(
    val networthByPeriod: List<NetWorthPeriodOutput>,
    val breakdown: Map<UUID, List<NetWorthPeriodOutput>>
)
