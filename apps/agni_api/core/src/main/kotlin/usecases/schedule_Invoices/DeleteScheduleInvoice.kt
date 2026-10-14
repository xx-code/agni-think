package usecases.schedule_Invoices

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    auth: IAuthenticateAdapter
): UseCase<DeleteScheduleInvoiceInput, Unit>(auth = auth) {
    override suspend fun process(input: DeleteScheduleInvoiceInput) {
        val scheduleInvoice = scheduleInvoiceRepo.get(input.scheduleInvoiceId)
            ?: throw NotFoundException.SingleEntity(input.scheduleInvoiceId, "schedule_invoice")

        if (input.passContextEdit && !scheduleInvoice.isContextEditable())
            throw ValidationException.CanNotEditSchedulerInvoiceWithModuleLinkDirectly()


        scheduleInvoiceRepo.delete(input.scheduleInvoiceId)
    }
}