package usecases.analystics

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import domain.entities.ScheduleInvoice
import usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
import usecases.interfaces.IUseCase

class GetScheduleInvoiceSummary(
    val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<Unit, GetScheduleInvoiceSummaryOutput> {
    override fun execAsync(input: Unit): GetScheduleInvoiceSummaryOutput {
        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll())

        return GetScheduleInvoiceSummaryOutput(
            totalPlan = scheduleInvoices.total.toInt(),
            totalActives = scheduleInvoices.items.filter { !it.isPause }.size,
            totalPause = scheduleInvoices.items.filter { it.isPause }.size,
            totalAmountActive = scheduleInvoices.items.filter { !it.isPause }.sumOf { it.amount },
        )
    }
}