package dev.auguste.agni_api.core.usecases.provisionable.dto

import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDate
import java.util.UUID

data class ProvisionDepreciateCriteriaOutput(
    val title: String,
    val description: String,
    val type: String,
    val value: Double,
    val monthRange: Int
)

data class ProvisionInvoiceOutput(
    val accountId: UUID,
    val categoryId: UUID,
    val tagIds: List<UUID>,
    val budgetIds: List<UUID>,
    val nextPaymentDate: LocalDate?,
    val paymentPeriod: String?,
    val paymentInterval: Int?
)

data class GetProvisionOutput(
    val id: UUID,
    val title: String,
    val costHT: Double,
    val costTTC: Double,
    val totalCost: Double,
    val acquisitionDate: LocalDate,
    val expectedLifespanMonth: Int,
    val costByMonth: Double,
    val monthlyPayment: Double,
    val residualValue: Double,
    val isPatrimony: Boolean,
    val type: String,
    val floorValue: Double,
    val interestLoan: Double,
    val loanMonth: Int,
    val depreciationCriteria: List<ProvisionDepreciateCriteriaOutput>,
    val scheduleInvoice: ProvisionInvoiceOutput?
)
