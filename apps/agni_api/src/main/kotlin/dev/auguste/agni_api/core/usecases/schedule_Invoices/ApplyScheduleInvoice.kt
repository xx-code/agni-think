package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.IEventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.time.LocalDateTime

class ApplyScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    private val createFreezeInvoice: IUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
    private val eventManager: IEventRegister
): IUseCase<Unit, BackgroundTaskOut> {
    override fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val scheduleInvoices = scheduleInvoiceRepo.getAll(
                QueryFilter(0, 0, true),
                QueryScheduleInvoiceExtend(QueryDateComparator(
                    LocalDateTime.now(),
                    ComparatorType.LesserOrEquals
                ))
            )

            for(scheduleInvoice in scheduleInvoices.items.filter { !it.isPause }) {
                var date = scheduleInvoice.scheduler.date
                if (scheduleInvoice.isFreeze && scheduleInvoice.scheduler.repeater != null)
                    date = scheduleInvoice.scheduler.upgradeDate()!!

                if (scheduleInvoice.isFreeze)
                    createFreezeInvoice.execAsync(CreateFreezeInvoiceInput(
                        title = scheduleInvoice.title,
                        accountId = scheduleInvoice.accountId,
                        endDate = date,
                        amount = scheduleInvoice.amount,
                        status = InvoiceStatusType.PENDING
                    ))
                else {
                    var mouvement = InvoiceMouvementType.CREDIT
                    if (scheduleInvoice.type != InvoiceType.INCOME)
                        mouvement = InvoiceMouvementType.DEBIT


                    createInvoice.execAsync(CreateInvoiceInput(
                        accountId = scheduleInvoice.accountId,
                        status = InvoiceStatusType.PENDING,
                        date = date,
                        type = scheduleInvoice.type,
                        mouvementType = mouvement,
                        currency = null,
                        transactions = setOf(
                            TransactionInput(
                                amount = scheduleInvoice.amount,
                                categoryId = scheduleInvoice.categoryId,
                                description = scheduleInvoice.title,
                                tagIds = scheduleInvoice.tagIds,
                                budgetIds = setOf()
                            )
                        ),
                        deductions = setOf()
                    ))
                }

                if (scheduleInvoice.scheduler.repeater == null)
                    scheduleInvoiceRepo.delete(scheduleInvoice.id)
                else {
                    val date = scheduleInvoice.scheduler.upgradeDate()!!
                    scheduleInvoice.scheduler = Scheduler(
                        date = date,
                        scheduleInvoice.scheduler.repeater,
                    )
                    scheduleInvoiceRepo.update(scheduleInvoice)
                }


                this.eventManager.notify(IEventType.NOTIFICATION, NotificationEventContent(
                    "Schedule Invoice",
                    "La transaction ${scheduleInvoice.isFreeze.let { "gele" }} ${scheduleInvoice.title} at ${scheduleInvoice.amount}",
                    type = NotificationType.Success,
                ))
            }

            return BackgroundTaskOut("Apply Schedule Success")
        } catch (error: Throwable) {
            this.eventManager.notify(IEventType.NOTIFICATION, NotificationEventContent(
                "Schedule Invoice !Error",
                "Error while applying schedule in voice ${error.message}",
                type = NotificationType.Error,
            ))

            return BackgroundTaskOut(error.localizedMessage)
        }
    }

}