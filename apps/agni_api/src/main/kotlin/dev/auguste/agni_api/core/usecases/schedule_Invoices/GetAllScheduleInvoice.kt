package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.ScheduleInvoiceRepeaterOutput

class GetAllScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<QueryFilter, ListOutput<GetScheduleInvoiceOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetScheduleInvoiceOutput> {
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
                            periodType = repeater.period.value,
                            interval =  repeater.interval
                        )
                    }
                )
            },
            total = scheduleInvoices.total
        )
    }
}