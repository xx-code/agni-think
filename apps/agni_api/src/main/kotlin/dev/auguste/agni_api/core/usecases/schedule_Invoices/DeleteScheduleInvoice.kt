package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class DeleteScheduleInvoice(
    val scheduleInvoiceRepo: IRepository<ScheduleInvoice>
): IUseCase<UUID, Unit> {
    override fun execAsync(input: UUID) {
        if (scheduleInvoiceRepo.get(input) == null)
            throw Error("Schedule invoices not found")

        scheduleInvoiceRepo.delete(input)
    }
}