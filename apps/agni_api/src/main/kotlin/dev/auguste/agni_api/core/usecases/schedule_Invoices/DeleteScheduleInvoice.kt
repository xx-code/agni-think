package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.DeleteScheduleInvoiceInput
import java.util.UUID

class DeleteScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<DeleteScheduleInvoiceInput, Unit> {
    override fun execAsync(input: DeleteScheduleInvoiceInput) {
        if (scheduleInvoiceRepo.get(input.scheduleInvoiceId) == null)
            throw Error("Schedule invoices not found")

        scheduleInvoiceRepo.delete(input.scheduleInvoiceId)
    }
}