package dev.auguste.agni_api.core.usecases.schedule_Invoices.dto

import dev.auguste.agni_api.core.adapters.dto.ScheduleRepeaterInput
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import java.time.LocalDateTime
import java.util.UUID

data class SchedulerInvoiceInput(
    val dueDate: LocalDateTime,
    val repeater: ScheduleRepeaterInput? = null
)

data class CreateScheduleInvoiceInput(
    val accountId: UUID,
    val amount: Double,
    val description: String,
    val categoryId: UUID?,
    val tagIds: Set<UUID>,
    val type: InvoiceType?,
    val schedule: SchedulerInvoiceInput,
    val isFreeze: Boolean?,
    val freezeSchedule: SchedulerInvoiceInput?,
    val endDate: LocalDateTime? = null
)