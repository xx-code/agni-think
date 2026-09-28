package dev.auguste.agni_api.core.usecases.provisionable

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.QueryExtendBuilder
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryComparator
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Provision
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.ScheduleInvoiceModuleLinkerType
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.provisionable.dto.DeleteProvisionInput
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteProvisionable(
    private val provisionRepo: IRepository<Provision>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val deleteScheduleInvoice: IUseCase<DeleteScheduleInvoiceInput, Unit>,
    private val unitOfWork: IUnitOfWork
): IUseCase<DeleteProvisionInput, Unit> {
    override fun execAsync(input: DeleteProvisionInput) {
        unitOfWork.execute {
            val provision = provisionRepo.get(input.provisionableId)
                ?: throw DomainException.NotFound.Provisionable(input.provisionableId)

            provisionRepo.delete(input.provisionableId)
            val scheduleInvoiceCondition = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, provision.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.PROVISION.value)

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