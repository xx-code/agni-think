package usecases.schedule_Invoices.dto

import adapters.dto.ScheduleRepeaterInput
import domain.enums.InvoiceType
import domain.value_objects.ScheduleInvoiceModuleLinker
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
    val type: domain.enums.InvoiceType?,
    val schedule: SchedulerInvoiceInput,
    val isFreeze: Boolean?,
    val freezeSchedule: SchedulerInvoiceInput?,
    val moduleLinker: ScheduleInvoiceModuleLinker? = null,
    val endDate: LocalDateTime? = null
)