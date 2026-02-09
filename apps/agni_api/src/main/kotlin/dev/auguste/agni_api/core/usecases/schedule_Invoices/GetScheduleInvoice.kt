package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.ScheduleInvoiceRepeaterOutput
import java.util.UUID

class GetScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<UUID, GetScheduleInvoiceOutput> {

    override fun execAsync(input: UUID): GetScheduleInvoiceOutput {
        val scheduleInvoice = scheduleInvoiceRepo.get(input) ?: throw Error("Invoice not found")

        return GetScheduleInvoiceOutput(
            id = scheduleInvoice.id,
            name = scheduleInvoice.title,
            accountId = scheduleInvoice.accountId,
            categoryId = scheduleInvoice.categoryId,
            isFreeze = scheduleInvoice.isFreeze,
            isPause = scheduleInvoice.isPause,
            tagIds = scheduleInvoice.tagIds,
            type = scheduleInvoice.type,
            amount = scheduleInvoice.amount,
            dueDate = scheduleInvoice.scheduler.date,
            repeater = scheduleInvoice.scheduler.repeater?.let { repeater ->
                ScheduleInvoiceRepeaterOutput(
                    periodType = repeater.period,
                    interval =  repeater.interval
                )
            }
        )
    }
}