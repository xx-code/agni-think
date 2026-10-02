package domain.entities

import domain.exceptions.ValidationException

import domain.enums.InvoiceType
import domain.value_objects.ScheduleInvoiceModuleLinker
import domain.value_objects.Scheduler
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class ScheduleInvoice(
    id: UUID = UUID.randomUUID(),
    title: String,
    accountId: UUID,
    type: InvoiceType,
    amount: Double,
    scheduler: Scheduler,
    categoryId: UUID,
    moduleLinker: ScheduleInvoiceModuleLinker?,
    freezeScheduler: Scheduler?,
    isPause: Boolean = false,
    isFreeze: Boolean = false,
    tagIds: MutableSet<UUID> = mutableSetOf(),
    endDate: LocalDateTime? = null
): Entity(id = id) {

    var title: String by cleanObservable(title, this)

    var accountId: UUID by cleanObservable(accountId, this)

    var type by cleanObservable(type, this)

    var amount: Double by cleanObservable(amount, this, {
        it > 0.0
    }) {
        ValidationException.SchedulerInvoiceAmountShouldGreaterThanZero(it)
    }

    var scheduler: Scheduler by cleanObservable(scheduler, this)

    var categoryId: UUID by cleanObservable(categoryId, this)

    var isPause: Boolean by cleanObservable(isPause, this)

    var isFreeze: Boolean by cleanObservable(isFreeze, this)

    var tagIds: MutableSet<UUID> by cleanObservable(tagIds, this)

    var endDate by cleanObservable(endDate, this)

    var freezeScheduler: Scheduler? by cleanObservable(freezeScheduler, this, {
        it != null && this.isFreeze
    }) {
        ValidationException.ScheduleFreezeInvoiceMustHaveAScheduler()
    }

    var moduleLinker by cleanObservable(moduleLinker, this)

    fun getFreezeEndDate(): LocalDate {
        if (!isFreeze)
            throw ValidationException.ScheduleFreezeInvoiceMustHaveAScheduler()

        if (freezeScheduler == null)
            throw ValidationException.ScheduleFreezeInvoiceMustHaveAScheduler()

        return freezeScheduler!!.date.toLocalDate()
    }

    fun isContextEditable(): Boolean = moduleLinker != null
}