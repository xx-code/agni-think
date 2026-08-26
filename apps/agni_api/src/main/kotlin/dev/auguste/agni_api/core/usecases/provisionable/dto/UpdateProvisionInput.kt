package dev.auguste.agni_api.core.usecases.provisionable.dto

import dev.auguste.agni_api.core.entities.enums.ProvisionType
import dev.auguste.agni_api.core.value_objects.ProvisionDepreciateCriteria
import java.time.LocalDate
import java.util.UUID

data class UpdateProvisionInput(
    val id: UUID,
    val title: String?,
    val initialCost: Double?,
    val acquisitionDate: LocalDate?,
    val expectedLifespanMonth: Int?,
    val isPatrimony: Boolean?,
    val scheduleInvoice: ScheduleInvoiceProvisionInput?,
    val depreciationCriteria: List<ProvisionDepreciateCriteria>?,
    val type: ProvisionType?,
    val floorValue: Double?,
    val interestLoan: Double?,
    val loanMonth: Int?
)