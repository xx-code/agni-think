package usecases.schedule_Invoices

import usecases.UseCase
import adapters.IAuthenticateAdapter
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
        val scheduleInvoices = scheduleInvoiceRepo.getAll(input.queryFilter, condition)

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