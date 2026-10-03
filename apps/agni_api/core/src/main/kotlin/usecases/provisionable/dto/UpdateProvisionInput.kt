package usecases.provisionable.dto

import domain.enums.ProvisionType
import domain.value_objects.ProvisionDepreciateCriteria
import java.time.LocalDate
import java.util.UUID

data class UpdateProvisionInput(
    val id: UUID,
    val title: String?,
    val costHT: Double?,
    val costTTC: Double?,
    val acquisitionDate: LocalDate?,
    val expectedLifespanMonth: Int?,
    val isPatrimony: Boolean?,
    val fundAmortizationId: UUID?,
    val isInstallmentOnTTC: Boolean = true,
    val scheduleInvoice: ScheduleInvoiceProvisionInput?,
    val depreciationCriteria: List<ProvisionDepreciateCriteria>?,
    val type: ProvisionType?,
    val floorValue: Double?,
    val interestLoan: Double?,
    val loanMonth: Int?
)