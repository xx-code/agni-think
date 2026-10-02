package usecases.schedule_Invoices

import adapters.repositories.IRepository
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput

class DeleteScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<DeleteScheduleInvoiceInput, Unit> {
    override fun execAsync(input: DeleteScheduleInvoiceInput) {
        val scheduleInvoice = scheduleInvoiceRepo.get(input.scheduleInvoiceId)
            ?: throw NotFoundException.SingleEntity(input.scheduleInvoiceId, "schedule_invoice")

        if (input.passContextEdit && !scheduleInvoice.isContextEditable())
            throw ValidationException.CanNotEditSchedulerInvoiceWithModuleLinkDirectly()


        scheduleInvoiceRepo.delete(input.scheduleInvoiceId)
    }
}