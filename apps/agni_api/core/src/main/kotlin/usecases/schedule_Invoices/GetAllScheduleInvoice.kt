package usecases.schedule_Invoices

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.ScheduleInvoice
import usecases.dto.ListOutput
import usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import usecases.schedule_Invoices.dto.ScheduleInvoiceRepeaterOutput

class GetAllScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): UseCase<QueryFilter, ListOutput<GetScheduleInvoiceOutput>>() {
    override suspend fun process(input: QueryFilter): ListOutput<GetScheduleInvoiceOutput> {
        val scheduleInvoices = scheduleInvoiceRepo.getAll(input)

        return ListOutput(
            items = scheduleInvoices.items.map {
                GetScheduleInvoiceOutput(
                    id = it.id,
                    name = it.title,
                    accountId = it.accountId,
                    categoryId = it.categoryId,
                    isFreeze = it.isFreeze,
                    isPause = it.isPause,
                    tagIds = it.tagIds,
                    type = it.type.value,
                    amount = it.amount,
                    dueDate = it.scheduler.date,
                    repeater = it.scheduler.repeater?.let { repeater ->
                        ScheduleInvoiceRepeaterOutput(
                            period = repeater.period.value,
                            interval = repeater.interval
                        )
                    },
                    endDate = it.endDate,
                    isEditable = !it.isContextEditable(),
                    freezeEndDate = it.freezeScheduler?.date,
                    freezeRepeater = it.freezeScheduler?.repeater?.let { repeater ->
                        ScheduleInvoiceRepeaterOutput(
                            period = repeater.period.value,
                            interval = repeater.interval
                        )
                    }
                )
            },
            total = scheduleInvoices.total
        )
    }
}