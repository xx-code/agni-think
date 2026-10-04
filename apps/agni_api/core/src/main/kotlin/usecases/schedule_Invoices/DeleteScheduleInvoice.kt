package usecases.schedule_Invoices

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): UseCase<DeleteScheduleInvoiceInput, Unit>() {
    override suspend fun process(input: DeleteScheduleInvoiceInput) {
        val scheduleInvoice = scheduleInvoiceRepo.get(input.scheduleInvoiceId)
            ?: throw NotFoundException.SingleEntity(input.scheduleInvoiceId, "schedule_invoice")

        if (input.passContextEdit && !scheduleInvoice.isContextEditable())
            throw ValidationException.CanNotEditSchedulerInvoiceWithModuleLinkDirectly()


        scheduleInvoiceRepo.delete(input.scheduleInvoiceId)
    }
}