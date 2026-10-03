package usecases.schedule_Invoices

import usecases.UseCase
import adapters.repositories.IRepository
import domain.exceptions.AlreadyExistException
import domain.exceptions.NotFoundException
import domain.exceptions.ValidationException
import domain.entities.ScheduleInvoice
import facades.InvoiceDependencies
import usecases.interfaces.IUseCase
import usecases.schedule_Invoices.dto.UpdateScheduleInvoiceInput
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import domain.value_objects.SchedulerRecurrence

class UpdateScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val invoiceDependencies: InvoiceDependencies,
    private val verifyScheduleModuleLinker: IUseCase<ScheduleInvoiceModuleLinker, Unit>
): UseCase<UpdateScheduleInvoiceInput, Unit>() {

    override suspend fun process(input: UpdateScheduleInvoiceInput) {
        val scheduleInvoice = scheduleInvoiceRepo.get(input.id) ?: throw NotFoundException.SingleEntity(input.id, "schedule_invoice")

        if (input.passContextEdit && !scheduleInvoice.isContextEditable())
            throw ValidationException.CanNotEditSchedulerInvoiceWithModuleLinkDirectly()

        if (input.name != null) {
            if (input.name != scheduleInvoice.title && scheduleInvoiceRepo.existsByName(input.name))
                throw AlreadyExistException.EntitiesByField(mapOf("name" to input.name), "schedule_invoice")

            scheduleInvoice.title = input.name
        }

        if (input.accountId != null) {
            if (invoiceDependencies.accountRepo.get(input.accountId) == null)
                throw NotFoundException.SingleEntity(input.accountId, "account")

            scheduleInvoice.accountId = input.accountId
        }

        if (!scheduleInvoice.isFreeze && input.categoryId != null) {
            if (invoiceDependencies.categoryRepo.get(input.categoryId) == null)
                throw NotFoundException.SingleEntity(input.categoryId, "category")

            scheduleInvoice.categoryId = input.categoryId
        }

        if (!scheduleInvoice.isFreeze && input.type != null) {
            scheduleInvoice.type = input.type
        }

        if (!scheduleInvoice.isFreeze && input.tagIds != null) {
            if (invoiceDependencies.tagRepo.getManyByIds(input.tagIds).size != input.tagIds.size)
                throw NotFoundException.EntitiesByOtherField(mapOf("ids" to input.tagIds.joinToString()), "tag")
        }

        if (input.amount != null) {
            if (input.amount < 0)
                throw ValidationException.ScheduleInvoiceAmountMustBeGreaterThanZero()
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

        if (input.endDate != null)
            scheduleInvoice.endDate = input.endDate

        if (input.moduleLinker != null) {
            verifyScheduleModuleLinker.processDirect(input.moduleLinker)
            scheduleInvoice.moduleLinker = input.moduleLinker
        }

        if (input.freezeSchedule != null) {
            val newFreezeScheduler = Scheduler(
                repeater = input.freezeSchedule.repeater?.let { repeater ->
                    SchedulerRecurrence(period = repeater.period, interval = repeater.interval)
                },
                date = input.freezeSchedule.dueDate
            )
            scheduleInvoice.freezeScheduler = newFreezeScheduler
        }

        if (scheduleInvoice.hasChanged())
            scheduleInvoiceRepo.update(scheduleInvoice)
    }
}