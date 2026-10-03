package usecases.income_sources

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.exceptions.NotFoundException
import domain.entities.IncomeSource
import domain.entities.ScheduleInvoice
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.income_sources.dto.DeleteIncomeSourceInput
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteIncomeSource(
    private val incomeSourceRepo: IRepository<IncomeSource>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
) : IUseCase<DeleteIncomeSourceInput, Unit> {
    override fun execAsync(input: DeleteIncomeSourceInput) {
        unitOfWork.execute {
            val incomeSource = incomeSourceRepo.get(input.incomeSourceId) ?: throw NotFoundException.SingleEntity(input.incomeSourceId, "income_source")
            incomeSourceRepo.delete(input.incomeSourceId)
            val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, incomeSource.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.INCOME_SOURCE.value)

            val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
            for (scheduleInvoice in scheduleInvoices.items) {
                deleteScheduleInvoice.execAsync(DeleteScheduleInvoiceInput(
                    scheduleInvoiceId = scheduleInvoice.id,
                    passContextEdit = true
                ))
            }
        }
    }
}