package usecases.analystics

import adapters.IAuthenticateAdapter
import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.SAVING_CATEGORY_ID
import domain.entities.ScheduleInvoice
import usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
class GetScheduleInvoiceSummary(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    auth: IAuthenticateAdapter
): UseCase<Unit, GetScheduleInvoiceSummaryOutput>(auth = auth) {
    override suspend fun process(input: Unit): GetScheduleInvoiceSummaryOutput {
        val currentUser = getCurrentUser()
        val condition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), condition)

        return GetScheduleInvoiceSummaryOutput(
            totalPlan = scheduleInvoices.total.toInt(),
            totalActives = scheduleInvoices.items.filter { !it.isPause }.size,
            totalPause = scheduleInvoices.items.filter { it.isPause }.size,
            totalAmountSchedule = scheduleInvoices.items.filter { it.categoryId != SAVING_CATEGORY_ID }.sumOf { it.amount },
            totalAmountContribution = scheduleInvoices.items.filter { it.categoryId == SAVING_CATEGORY_ID }.sumOf { it.amount },
            totalAmountActive = scheduleInvoices.items.filter { !it.isPause }.sumOf { it.amount },
        )
    }
}