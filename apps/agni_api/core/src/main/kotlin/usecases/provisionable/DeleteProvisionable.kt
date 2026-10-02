package usecases.provisionable

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.query_extend.QueryComparator
import domain.exceptions.NotFoundException
import domain.entities.Provision
import domain.entities.ScheduleInvoice
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.interfaces.IUseCase
import usecases.provisionable.dto.DeleteProvisionInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteProvisionable(
    private val provisionRepo: IRepository<Provision>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
): IUseCase<DeleteProvisionInput, Unit> {
    override fun execAsync(input: DeleteProvisionInput) {
        unitOfWork.execute {
            val provision = provisionRepo.get(input.provisionableId)
                ?: throw NotFoundException.SingleEntity(input.provisionableId, "provisionable")

            provisionRepo.delete(input.provisionableId)
            val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, provision.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.PROVISION.value)

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