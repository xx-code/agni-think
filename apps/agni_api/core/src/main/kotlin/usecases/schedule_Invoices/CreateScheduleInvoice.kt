package usecases.schedule_Invoices

import domain.FREEZE_CATEGORY_ID
import adapters.repositories.IRepository
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import domain.enums.InvoiceType
import usecases.CreatedOutput
import facades.InvoiceDependencies
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class CreateScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>
): IUseCase<CreateScheduleInvoiceInput, CreatedOutput> {
    override fun execAsync(input: CreateScheduleInvoiceInput): CreatedOutput {
        if (scheduleInvoiceRepo.existsByName(input.description))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.description), "schedule_invoice")

        if (invoiceDependencies.accountRepo.get(input.accountId) == null)
            throw ValidationException.ScheduleInvoiceAccountNotFound()

        if (input.isFreeze == false && input.categoryId == null)
            throw ValidationException.ScheduleInvoiceCategoryIdMustBeDefined()

        if (input.isFreeze == false && input.type == null)
            throw ValidationException.ScheduleInvoiceTypeNotDefined(input.type.toString())

        if (input.isFreeze == false && input.categoryId != null && invoiceDependencies.categoryRepo.get(input.categoryId) == null)
            throw NotFoundException.SingleEntity(input.categoryId, "category")

        if (input.isFreeze == false && input.tagIds.isNotEmpty()) {
            if (invoiceDependencies.tagRepo.getManyByIds(input.tagIds).size != input.tagIds.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.tagIds.joinToString()), "tag")
        }

        var categoryId = FREEZE_CATEGORY_ID
        if (input.categoryId != null)
            categoryId = input.categoryId

        var type = _root_ide_package_.domain.enums.InvoiceType.OTHER
        if (input.type != null)
            type = input.type

        var repeater: SchedulerRecurrence? = null
        if (input.schedule.repeater != null)
            repeater = SchedulerRecurrence( period = input.schedule.repeater.period, interval = input.schedule.repeater.interval)

        if (input.moduleLinker != null)
            verifyScheduleModuleLinker.execAsync(input.moduleLinker)

        val newScheduleInvoice = ScheduleInvoice(
            accountId = input.accountId,
            amount = input.amount,
            title = input.description,
            isFreeze = input.isFreeze?.let { input.isFreeze } ?: false,
            isPause = false,
            categoryId = categoryId,
            type = type,
            scheduler = Scheduler(
                repeater = repeater,
                date = input.schedule.dueDate
            ),
            endDate = input.endDate,
            tagIds = input.tagIds.toMutableSet(),
            moduleLinker = input.moduleLinker,
            freezeScheduler = input.freezeSchedule?.let { Scheduler(
                repeater = it.repeater?.let { reap -> SchedulerRecurrence( period = reap.period, interval = reap.interval) },
                date = it.dueDate
            ) }
        )

        scheduleInvoiceRepo.create(newScheduleInvoice)

        return CreatedOutput(newScheduleInvoice.id)
    }
}