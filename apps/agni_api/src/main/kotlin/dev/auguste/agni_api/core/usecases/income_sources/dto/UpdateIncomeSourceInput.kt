package dev.auguste.agni_api.core.usecases.income_sources.dto

import dev.auguste.agni_api.core.entities.enums.IncomeSourceFrequencyType
import dev.auguste.agni_api.core.entities.enums.IncomeSourceType
import java.time.LocalDate
import java.util.UUID

data class UpdateIncomeSourceInput(
    val id: UUID,
    val invoiceIncomeCategoryId: UUID? = null,
    val title: String? = null,
    val type: IncomeSourceType? = null,
    val payFrequencyType: IncomeSourceFrequencyType? = null,
    val reliabilityLevel: Int? = null,
    val taxRate: Double? = null,
    val otherRate: Double? = null,
    val startDate: LocalDate? = null,
    val linkedAccountId: UUID? = null,
    val annualGrossAmount: Double? = null,
    val endDate: LocalDate? = null
)
