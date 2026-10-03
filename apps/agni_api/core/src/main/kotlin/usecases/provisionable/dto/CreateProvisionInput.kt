package usecases.provisionable.dto

import domain.enums.PeriodType
import domain.enums.ProvisionType
import domain.value_objects.ProvisionDepreciateCriteria
import java.time.LocalDate
import java.util.UUID

data class ScheduleInvoiceProvisionInput(
    val invoiceAccountId: UUID,
    val invoiceCategoryId: UUID,
    val tagIds: Set<UUID>,
    val budgetIds: Set<UUID>,
    val paymentPeriod: PeriodType,
    val paymentInterval: Int
)

data class CreateProvisionInput (
    val title: String,
    val costHT: Double,
    val costTTC: Double,
    val fundAmortizationId: UUID?,
    val acquisitionDate: LocalDate,
    val expectedLifespanMonth: Int,
    val isInstallmentOnTTC: Boolean = true,
    val type: ProvisionType,
    val isPatrimony: Boolean,
    val depreciationCriteria: List<ProvisionDepreciateCriteria>,
    val scheduleInvoice: ScheduleInvoiceProvisionInput? = null,
    val floorValue: Double = 0.0,
    val interestLoan: Double = 0.0,
    val loanMonth: Int = 0
)