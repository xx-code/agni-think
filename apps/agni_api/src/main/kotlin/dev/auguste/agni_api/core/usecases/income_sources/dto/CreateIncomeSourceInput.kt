package dev.auguste.agni_api.core.usecases.income_sources.dto

import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import java.time.LocalDate
import java.util.UUID

data class CreateIncomeSourceInput(
    val title: String,
    val type: IncomeSourceType,
    val payFrequencyType: IncomeSourceFrequencyType,
    val reliabilityLevel: Int,
    val taxRate: Double,
    val otherRate: Double,
    val startDate: LocalDate,
    val linkedAccountId: UUID?,
    val annualGrossAmount: Double?,
    val endDate: LocalDate?
)
