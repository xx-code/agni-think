package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<DeleteScheduleInvoiceInput, Unit> {
    override fun execAsync(input: DeleteScheduleInvoiceInput) {
        val scheduleInvoice = scheduleInvoiceRepo.get(input.scheduleInvoiceId)
            ?: throw DomainException.NotFound.ScheduleInvoice(input.scheduleInvoiceId)

        if (input.passContextEdit && !scheduleInvoice.isContextEditable())
            throw DomainException.BusinessLogic.CanNotEditSchedulerInvoiceWithModuleLinkDirectly()


        scheduleInvoiceRepo.delete(input.scheduleInvoiceId)
    }
}