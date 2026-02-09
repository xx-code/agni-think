package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.FREEZE_CATEGORY_ID
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.facades.InvoiceDependencies
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import dev.auguste.agni_api.core.value_objects.Scheduler
import dev.auguste.agni_api.core.value_objects.SchedulerRecurrence

class CreateScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val invoiceDependencies: InvoiceDependencies
): IUseCase<CreateScheduleInvoiceInput, CreatedOutput> {
    override fun execAsync(input: CreateScheduleInvoiceInput): CreatedOutput {
        if (scheduleInvoiceRepo.existsByName(input.name))
            throw Error("Account with name ${input.name} already exists")

        if (invoiceDependencies.accountRepo.get(input.accountId) == null)
            throw Error("Account not found")

        if (input.isFreeze == false && input.categoryId == null)
            throw Error("Category Id must be defined if is not a freeze transaction")

        if (input.isFreeze == false && input.type == null)
            throw Error("Type ${input.type} not defined")

        if (input.isFreeze == false && input.categoryId != null && invoiceDependencies.categoryRepo.get(input.categoryId) == null)
            throw Error("Category not found")

        if (input.isFreeze == false && input.tagIds.isNotEmpty()) {
            if (invoiceDependencies.tagRepo.getManyByIds(input.tagIds) != input.tagIds)
                throw Error("Tags not found")
        }

        var categoryId = FREEZE_CATEGORY_ID
        if (input.categoryId != null)
            categoryId = input.categoryId

        var type = InvoiceType.OTHER
        if (input.type != null)
            type = input.type

        var repeater: SchedulerRecurrence? = null
        if (input.schedule.repeater != null)
            repeater = SchedulerRecurrence( period = input.schedule.repeater.period, interval = input.schedule.repeater.interval)

        var newScheduleInvoice = ScheduleInvoice(
            accountId = input.accountId,
            amount = input.amount,
            title = input.description,
            isFreeze = true,
            isPause = false,
            categoryId = categoryId,
            type = type,
            scheduler = Scheduler(
                repeater = repeater,
                date = input.schedule.dueDate
            ),
            tagIds = input.tagIds.toMutableSet()
        )

        scheduleInvoiceRepo.create(newScheduleInvoice)

        return CreatedOutput(newScheduleInvoice.id)
    }
}