package dev.auguste.agni_api.core.usecases.schedule_Invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceType
import java.time.LocalDateTime
import java.util.UUID

data class UpdateScheduleInvoiceInput(
    val id: UUID,
    val name: String?,
    val accountId: UUID?,
    val amount: Double?,
    val categoryId: UUID?,
    val tagIds: Set<UUID>?,
    val type: InvoiceType?,
    val isPause: Boolean?,
    val schedule: SchedulerInvoiceInput?,
    val freezeSchedule: SchedulerInvoiceInput?,
    val endDate: LocalDateTime? = null
)
