package usecases.income_sources.dto

import domain.enums.IncomeSourceFrequencyType
import domain.enums.IncomeSourceType
import java.time.LocalDate
import java.util.UUID

data class UpdateIncomeSourceInput(
    val id: UUID,
    val invoiceIncomeCategoryId: UUID? = null,
    val title: String? = null,
    val type: domain.enums.IncomeSourceType? = null,
    val payFrequencyType: domain.enums.IncomeSourceFrequencyType? = null,
    val reliabilityLevel: Int? = null,
    val taxRate: Double? = null,
    val otherRate: Double? = null,
    val startDate: LocalDate? = null,
    val linkedAccountId: UUID? = null,
    val annualGrossAmount: Double? = null,
    val endDate: LocalDate? = null
)
