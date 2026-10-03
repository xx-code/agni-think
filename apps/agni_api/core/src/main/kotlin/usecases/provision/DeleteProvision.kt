package usecases.provision

import usecases.interfaces.IUseCase

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.exceptions.NotFoundException
import domain.entities.Provision
import domain.entities.ScheduleInvoice
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.UseCase
import usecases.provision.dto.DeleteProvisionInput
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteProvision(
    private val provisionRepo: IRepository<Provision>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    unitOfWork: IUnitOfWork
): UseCase<DeleteProvisionInput, Unit>(unitOfWork) {
    override suspend fun process(input: DeleteProvisionInput) {
        val provision = provisionRepo.get(input.provisionableId)
            ?: throw NotFoundException.SingleEntity(input.provisionableId, "provisionable")

        provisionRepo.delete(input.provisionableId)
        val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
            .addCondition("moduleLinker.sourceId", QueryComparator.Equal, provision.id)
            .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.PROVISION.value)

        val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), scheduleInvoiceCondition)
        for (scheduleInvoice in scheduleInvoices.items) {
            deleteScheduleInvoice.processDirect(DeleteScheduleInvoiceInput(
                scheduleInvoiceId = scheduleInvoice.id,
                passContextEdit = true
            ))
        }
    }
}