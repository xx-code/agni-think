package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class UpdateScheduleInvoice(
    val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    val invoiceDependencies: InvoiceDependencies
): IUseCase<UpdateScheduleInvoiceInput, Unit> {

    override fun execAsync(input: UpdateScheduleInvoiceInput) {
        val scheduleInvoice = scheduleInvoiceRepo.get(input.id) ?: throw Error("Schedule Invoice not found")

        if (input.name != null) {
            if (input.name != scheduleInvoice.title && scheduleInvoiceRepo.existsByName(input.name))
                throw Error("Schedule Invoice with name ${input.name} already exists")

            scheduleInvoice.title = input.name
        }

        if (input.accountId != null) {
            if (invoiceDependencies.accountRepo.get(input.accountId) == null)
                throw Error("Account not found")

            scheduleInvoice.accountId = input.accountId
        }

        if (!scheduleInvoice.isFreeze && input.categoryId != null) {
            if (invoiceDependencies.categoryRepo.get(input.categoryId) == null)
                throw Error("Category not found")

            scheduleInvoice.categoryId = input.categoryId
        }

        if (!scheduleInvoice.isFreeze && input.type != null) {
            scheduleInvoice.type = input.type
        }

        if (!scheduleInvoice.isFreeze && input.tagIds != null) {
            if (invoiceDependencies.tagRepo.getManyByIds(input.tagIds) != input.tagIds)
                throw Error("Tags not found")
        }

        if (input.amount != null) {
            if (input.amount < 0)
                throw Error("Amount must be greater than 0")
            scheduleInvoice.amount = input.amount
        }

        if (input.schedule != null) {
            val newScheduler = Scheduler(
                repeater = input.schedule.repeater?.let { repeater ->
                    SchedulerRecurrence(period = repeater.period, interval = repeater.interval)
                },
                date = input.schedule.dueDate
            )
            scheduleInvoice.scheduler = newScheduler
        }

        if (input.isPause != null)
            scheduleInvoice.isPause = input.isPause

        if (scheduleInvoice.hasChanged())
            scheduleInvoiceRepo.update(scheduleInvoice)
    }
}