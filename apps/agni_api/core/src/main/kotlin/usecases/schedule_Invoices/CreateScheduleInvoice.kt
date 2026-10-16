package usecases.schedule_Invoices

import usecases.UseCase
import adapters.IAuthenticateAdapter
import domain.FREEZE_CATEGORY_ID
import adapters.repositories.IRepository
import domain.SAVING_CATEGORY_ID
import domain.UNKNOWN_CATEGORY_ID
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import domain.entities.User
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceType
import usecases.dto.CreatedOutput
import facades.InvoiceDependencies
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.CreateScheduleInvoiceInput
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class CreateScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>,
    auth: IAuthenticateAdapter
): UseCase<CreateScheduleInvoiceInput, CreatedOutput>(auth = auth) {
    override suspend fun process(input: CreateScheduleInvoiceInput): CreatedOutput {
        val currentUser = getCurrentUser()

        if (scheduleInvoiceRepo.existsByName(input.description))
            throw AlreadyExistException.EntitiesByField(mapOf("name" to input.description), "schedule_invoice")

        if (invoiceDependencies.accountRepo.get(input.accountId) == null)
            throw ValidationException.ScheduleInvoiceAccountNotFound()

        if (input.categoryId == null && input.type == ScheduleInvoiceType.STANDARD)
            throw ValidationException.ScheduleInvoiceCategoryIdMustBeDefined()

        if (input.invoiceType == null && input.type == ScheduleInvoiceType.STANDARD)
            throw ValidationException.ScheduleInvoiceTypeNotDefined(input.type.toString())

        if (input.categoryId != null) {
            if (invoiceDependencies.categoryRepo.get(input.categoryId) == null)
                throw NotFoundException.SingleEntity(input.categoryId, "category")
        }

        if (input.tagIds.isNotEmpty() && input.type == ScheduleInvoiceType.STANDARD) {
            if (invoiceDependencies.tagRepo.getManyByIds(input.tagIds).size != input.tagIds.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to  input.tagIds.joinToString()), "tag")
        }

        var repeater: SchedulerRecurrence? = null
        if (input.schedule.repeater != null)
            repeater = SchedulerRecurrence( period = input.schedule.repeater.period, interval = input.schedule.repeater.interval)

        if (input.moduleLinker != null)
            verifyScheduleModuleLinker.processDirect(input.moduleLinker)


        val newScheduleInvoice = ScheduleInvoice(
            userId = currentUser.id,
            accountId = input.accountId,
            amount = input.amount,
            title = input.description,
            type = input.type,
            invoiceType = input.invoiceType ?: InvoiceType.OTHER,
            isPause = false,
            categoryId = input.categoryId ?: when(input.type) {
                ScheduleInvoiceType.SAVING -> SAVING_CATEGORY_ID
                ScheduleInvoiceType.FREEZE -> FREEZE_CATEGORY_ID
                else -> UNKNOWN_CATEGORY_ID
            } ,
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