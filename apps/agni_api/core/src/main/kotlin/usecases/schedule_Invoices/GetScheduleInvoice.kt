package usecases.schedule_Invoices

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.entities.ScheduleInvoice
import usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import usecases.schedule_Invoices.dto.ScheduleInvoiceRepeaterOutput
import java.util.UUID

class GetScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): UseCase<UUID, GetScheduleInvoiceOutput>() {

    override suspend fun process(input: UUID): GetScheduleInvoiceOutput {
        val scheduleInvoice = scheduleInvoiceRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "schedule_invoice")

        return GetScheduleInvoiceOutput(
            id = scheduleInvoice.id,
            name = scheduleInvoice.title,
            accountId = scheduleInvoice.accountId,
            categoryId = scheduleInvoice.categoryId,
            isFreeze = scheduleInvoice.isFreeze,
            isPause = scheduleInvoice.isPause,
            tagIds = scheduleInvoice.tagIds,
            type = scheduleInvoice.type.value,
            amount = scheduleInvoice.amount,
            dueDate = scheduleInvoice.scheduler.date,
            repeater = scheduleInvoice.scheduler.repeater?.let { repeater ->
                ScheduleInvoiceRepeaterOutput(
                    period = repeater.period.value,
                    interval =  repeater.interval
                )
            },
            isEditable = !scheduleInvoice.isContextEditable(),
            endDate = scheduleInvoice.endDate,
            freezeEndDate = scheduleInvoice.freezeScheduler?.date,
            freezeRepeater = scheduleInvoice.freezeScheduler?.repeater?.let { repeater ->
                ScheduleInvoiceRepeaterOutput(
                    period = repeater.period.value,
                    interval = repeater.interval
                )
            }
        )
    }
}