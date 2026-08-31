package dev.auguste.agni_api.core.usecases.schedule_Invoices

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.events.EventType
import dev.auguste.agni_api.core.adapters.events.IEventRegister
import dev.auguste.agni_api.core.adapters.events.contents.NotificationEventContent
import dev.auguste.agni_api.core.adapters.events.contents.NotificationType
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.IUnitOfWork
import dev.auguste.agni_api.core.adapters.repositories.query_extend.ComparatorType
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryDateComparator
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryScheduleInvoiceExtend
import dev.auguste.agni_api.core.entities.ScheduleInvoice
import dev.auguste.agni_api.core.entities.enums.InvoiceMouvementType
import dev.auguste.agni_api.core.entities.enums.InvoiceStatusType
import dev.auguste.agni_api.core.entities.enums.InvoiceType
import dev.auguste.agni_api.core.usecases.BackgroundTaskOut
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.interfaces.IInnerUseCase
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateFreezeInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.CreateInvoiceInput
import dev.auguste.agni_api.core.usecases.invoices.dto.TransactionInput
import dev.auguste.agni_api.core.value_objects.Scheduler
import java.time.LocalDateTime

class ApplyScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val createFreezeInvoice: IInnerUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
    private val eventManager: IEventRegister,
    private val unitOfWork: IUnitOfWork,
): IUseCase<Unit, BackgroundTaskOut> {
    override fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val scheduleInvoices = scheduleInvoiceRepo.getAll(
                QueryFilter(0, 30, true),
                QueryScheduleInvoiceExtend(
                    comparatorDueDate = QueryDateComparator(
                        LocalDateTime.now(),
                        ComparatorType.LesserOrEquals
                    ),
                    comparatorEndDate = QueryDateComparator(
                        date = LocalDateTime.now(),
                        comparator = ComparatorType.LesserOrEquals
                    )
                )
            )

            for(scheduleInvoice in scheduleInvoices.items.filter { !it.isPause }) {
                unitOfWork.execute {
                    var date = scheduleInvoice.scheduler.date
                    if (
                        scheduleInvoice.isFreeze
                        &&
                        scheduleInvoice.freezeScheduler != null) {
                        date = scheduleInvoice.freezeScheduler!!.date
                    }

                    if (scheduleInvoice.isFreeze) {
                        createFreezeInvoice.execInnerAsync(CreateFreezeInvoiceInput(
                            title = scheduleInvoice.title,
                            accountId = scheduleInvoice.accountId,
                            endDate = date,
                            amount = scheduleInvoice.amount,
                            status = InvoiceStatusType.PENDING
                        ))
                    } else {
                        var mouvement = InvoiceMouvementType.CREDIT
                        if (scheduleInvoice.type != InvoiceType.INCOME)
                            mouvement = InvoiceMouvementType.DEBIT


                        createInvoice.execInnerAsync(CreateInvoiceInput(
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
                        val date = scheduleInvoice.scheduler.upgradeDate()
                        scheduleInvoice.scheduler = Scheduler(
                            date = date,
                            scheduleInvoice.scheduler.repeater,
                        )

                        if (scheduleInvoice.isFreeze && scheduleInvoice.freezeScheduler != null) {
                            scheduleInvoice.freezeScheduler = Scheduler(
                                date = scheduleInvoice.freezeScheduler!!.upgradeDate(),
                                scheduleInvoice.freezeScheduler!!.repeater,
                            )
                        }
                        scheduleInvoiceRepo.update(scheduleInvoice)
                    }


                    this.eventManager.notify(EventType.NOTIFICATION, NotificationEventContent(
                        "Schedule Invoice",
                        "La transaction ${scheduleInvoice.isFreeze.let { "gele" }} ${scheduleInvoice.title} at ${scheduleInvoice.amount}",
                        type = NotificationType.Success,
                    ))
                }
            }

            return BackgroundTaskOut("Apply Schedule Success")
        } catch (error: Throwable) {
            this.eventManager.notify(EventType.NOTIFICATION, NotificationEventContent(
                "Schedule Invoice !Error",
                "Error while applying schedule in voice ${error.message}",
                type = NotificationType.Error,
            ))

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}