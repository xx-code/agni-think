package usecases.income_sources.dto

import domain.enums.IncomeSourceFrequencyType
import domain.enums.IncomeSourceType
import java.time.LocalDate
import java.util.UUID

data class CreateIncomeSourceInput(
    val title: String,
    val type: domain.enums.IncomeSourceType,
    val payFrequencyType: domain.enums.IncomeSourceFrequencyType,
    val reliabilityLevel: Int,
    val taxRate: Double,
    val otherRate: Double,
    val startDate: LocalDate,
    val invoiceIncomeCategoryId: UUID?,
    val linkedAccountId: UUID?,
    val annualGrossAmount: Double?,
    val endDate: LocalDate?
)
