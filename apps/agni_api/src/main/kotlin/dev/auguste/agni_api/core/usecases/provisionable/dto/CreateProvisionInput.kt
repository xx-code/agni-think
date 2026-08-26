package dev.auguste.agni_api.core.usecases.provisionable.dto

import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.time.LocalDate
import java.util.UUID

data class ScheduleInvoiceProvisionInput(
    val invoiceAccountId: UUID,
    val invoiceCategoryId: UUID,
    val scheduler: Scheduler,
    val endDate: LocalDate,
    val tagIds: Set<UUID>,
    val budgetIds: Set<UUID>
)

data class CreateProvisionInput (
    val title: String,
    val initialCost: Double,
    val acquisitionDate: LocalDate,
    val expectedLifespanMonth: Int,
    val type: ProvisionType,
    val isPatrimony: Boolean,
    val depreciationCriteria: List<ProvisionDepreciateCriteria>,
    val scheduleInvoice: ScheduleInvoiceProvisionInput? = null,
    val floorValue: Double = 0.0,
    val interestLoan: Double = 0.0,
    val loanMonth: Int = 0
)