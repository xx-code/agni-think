package dev.auguste.agni_api.core.usecases.schedule_Invoices.dto

import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.entities.enums.PeriodType
import java.time.LocalDateTime
import java.util.UUID

data class ScheduleInvoiceRepeaterOutput(val periodType: PeriodType, val interval: Int)

data class GetScheduleInvoiceOutput(
    val id: UUID,
    val name: String,
    val accountId: UUID,
    val categoryId: UUID,
    val tagIds: Set<UUID>,
    val type: InvoiceType,
    val amount: Double,
    val isPause: Boolean,
    val isFreeze: Boolean,
    val dueDate: LocalDateTime,
    val repeater: ScheduleInvoiceRepeaterOutput?
)