package usecases.schedule_Invoices

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.ScheduleInvoice
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceModuleLinkerType
import usecases.BackgroundTaskOut
import usecases.CreatedOutput
import usecases.interfaces.IInnerUseCase
import usecases.interfaces.ISuspendableUseCase
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import domain.value_objects.InvoiceModuleLinker
import domain.value_objects.Scheduler
import java.time.LocalDate
import java.time.LocalDateTime

class ApplyScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val createInvoice: IInnerUseCase<CreateInvoiceInput, CreatedOutput>,
    private val createFreezeInvoice: IInnerUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
    private val eventManager: IEventRegister,
    private val unitOfWork: IUnitOfWork,
): ISuspendableUseCase<Unit, BackgroundTaskOut> {
    override suspend fun execAsync(input: Unit): BackgroundTaskOut {
        try {
            val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("scheduler.date", QueryComparator.GreaterOrEquals, LocalDateTime.now())
            val scheduleInvoices = scheduleInvoiceRepo.getAll(
                QueryFilter(0, 30, true),
                conditionScheduleInvoice,
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
                        var movement = InvoiceMovementType.CREDIT
                        if (scheduleInvoice.type != InvoiceType.INCOME)
                            movement = InvoiceMovementType.DEBIT

                        val invoiceModuleLinkers = mutableListOf<InvoiceModuleLinker>()
                        invoiceModuleLinkers.add(InvoiceModuleLinker(
                            scheduleInvoice.id,
                            InvoiceModuleLinkerType.SCHEDULE_INVOICE
                        ))

                        if (scheduleInvoice.moduleLinker != null) {
                            val matchModuleType = when(scheduleInvoice.moduleLinker!!.module) {
                                ScheduleInvoiceModuleLinkerType.INCOME_SOURCE -> InvoiceModuleLinkerType.INCOME_SOURCE
                                ScheduleInvoiceModuleLinkerType.PROVISION -> InvoiceModuleLinkerType.PROVISION
                                else -> null
                            }
                            if (matchModuleType != null) {
                                invoiceModuleLinkers.add(InvoiceModuleLinker(
                                    scheduleInvoice.moduleLinker!!.sourceId,
                                    matchModuleType,
                                ))
                            }
                        }

                        createInvoice.execInnerAsync(CreateInvoiceInput(
                            accountId = scheduleInvoice.accountId,
                            status = InvoiceStatusType.PENDING,
                            date = date,
                            type = scheduleInvoice.type,
                            mouvementType = movement,
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
                            moduleSourcesLinker = invoiceModuleLinkers,
                            deductions = setOf()
                        ))
                    }

                    if (scheduleInvoice.scheduler.repeater == null || (scheduleInvoice.endDate != null && scheduleInvoice.endDate!!.toLocalDate() >= LocalDate.now())) {
                        scheduleInvoiceRepo.delete(scheduleInvoice.id)
                    }
                    else {
                        val date = scheduleInvoice.scheduler.upgradeDate()
                        scheduleInvoice.scheduler = Scheduler(
                            date = date,
                            scheduleInvoice.scheduler.repeater,
                        )

                        if (scheduleInvoice.isFreeze && scheduleInvoice.freezeScheduler != null) {
                            if (scheduleInvoice.freezeScheduler!!.date <= LocalDateTime.now()) {
                                scheduleInvoice.freezeScheduler = Scheduler(
                                    date = scheduleInvoice.freezeScheduler!!.upgradeDate(),
                                    scheduleInvoice.freezeScheduler!!.repeater,
                                )
                            }
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