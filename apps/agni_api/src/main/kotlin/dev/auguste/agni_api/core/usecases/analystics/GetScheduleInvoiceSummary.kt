package dev.auguste.agni_api.core.usecases.analystics

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.analystics.dto.GetScheduleInvoiceSummaryOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

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