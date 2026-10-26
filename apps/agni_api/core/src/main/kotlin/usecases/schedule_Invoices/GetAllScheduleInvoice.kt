package usecases.schedule_Invoices

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.ScheduleInvoice
import usecases.dto.ListOutput
import usecases.schedule_Invoices.dto.GetAllScheduleInvoiceInput
import usecases.schedule_Invoices.dto.GetScheduleInvoiceOutput
import usecases.schedule_Invoices.dto.ScheduleInvoiceRepeaterOutput

class GetAllScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    auth: IAuthenticateAdapter
): UseCase<GetAllScheduleInvoiceInput, ListOutput<GetScheduleInvoiceOutput>>(auth = auth) {
    override suspend fun process(input: GetAllScheduleInvoiceInput): ListOutput<GetScheduleInvoiceOutput> {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val query = QueryFilter(
            offset = input.queryFilter.offset,
            limit = input.queryFilter.limit,
            sortBy = QuerySortBy(
                "scheduler->>'due_date'",
                ascending = true
            )
        )
        val scheduleInvoices = scheduleInvoiceRepo.getAll(query, condition)

        return ListOutput(
            items = scheduleInvoices.items.map {
                GetScheduleInvoiceOutput(
                    id = it.id,
                    name = it.title,
                    accountId = it.accountId,
                    categoryId = it.categoryId,
                    type = it.type.value,
                    invoiceType = it.invoiceType.value,
                    isPause = it.isPause,
                    tagIds = it.tagIds,
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