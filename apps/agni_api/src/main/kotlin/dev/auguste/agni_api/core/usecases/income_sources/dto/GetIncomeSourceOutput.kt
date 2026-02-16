package dev.auguste.agni_api.core.usecases.income_sources.dto

import java.time.LocalDate
import java.util.UUID

data class GetIncomeSourceOutput(
    val id: UUID,
    val type: String,
    val reliabilityLevel: Int,
    val taxRate: Double,
    val otherRate: Double,
    val startDate: LocalDate,
    val estimatedFutureOccurrences: Int,
    val estimateNextNetAmount: Double?,
    val linkedAccountId: UUID?,
    val annualGrossAmount: Double?,
    val endDate: LocalDate?,
)
