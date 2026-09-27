package dev.auguste.agni_api.core.usecases.income_sources

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.IncomeSource
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.income_sources.dto.DeleteIncomeSourceInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<DeleteIncomeSourceInput, Unit> {
    override fun execAsync(input: DeleteIncomeSourceInput) {
        unitOfWork.execute {
            val incomeSource = incomeSourceRepo.get(input.incomeSourceId) ?: throw DomainException.NotFound.IncomeSource(input.incomeSourceId)
            incomeSourceRepo.delete(input.incomeSourceId)
            val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, incomeSource.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.INCOME_SOURCE.value)

            val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
            if (scheduleInvoices.items.isNotEmpty()) {
                deleteScheduleInvoice.execAsync(DeleteScheduleInvoiceInput(
                    scheduleInvoiceId = scheduleInvoices.items.first().id,
                ))
            }
        }
    }
}