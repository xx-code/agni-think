package dev.auguste.agni_api.core.usecases.schedule_Invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.value_objects.ScheduleInvoiceModuleLinker
import java.time.LocalDateTime
import java.util.UUID

data class UpdateScheduleInvoiceInput(
    val id: UUID,
    val name: String? = null,
    val accountId: UUID? = null,
    val amount: Double? = null,
    val categoryId: UUID? = null,
    val tagIds: Set<UUID>? = null,
    val type: InvoiceType? = null,
    val isPause: Boolean? = null,
    val schedule: SchedulerInvoiceInput? = null,
    val freezeSchedule: SchedulerInvoiceInput? = null,
    val moduleLinker: ScheduleInvoiceModuleLinker? = null,
    val endDate: LocalDateTime? = null,
    val passContextEdit: Boolean = false
)
