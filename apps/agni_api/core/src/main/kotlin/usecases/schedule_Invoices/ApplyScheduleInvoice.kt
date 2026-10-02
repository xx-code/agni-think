package usecases.schedule_Invoices

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.query_extend.QueryComparator
import adapters.repositories.query_extend.QueryDateComparator
import adapters.repositories.query_extend.QueryScheduleInvoiceExtend
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
            val scheduleInvoices = scheduleInvoiceRepo.getAll(
                QueryFilter(0, 30, true),
                QueryScheduleInvoiceExtend(
                    comparatorDueDate = QueryDateComparator(
                        LocalDateTime.now(),
                        comparator = QueryComparator.LesserOrEquals,
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
                            status = _root_ide_package_.domain.enums.InvoiceStatusType.PENDING
                        ))
                    } else {
                        var movement = _root_ide_package_.domain.enums.InvoiceMovementType.CREDIT
                        if (scheduleInvoice.type != _root_ide_package_.domain.enums.InvoiceType.INCOME)
                            movement = _root_ide_package_.domain.enums.InvoiceMovementType.DEBIT

                        val invoiceModuleLinkers = mutableListOf<InvoiceModuleLinker>()
                        invoiceModuleLinkers.add(InvoiceModuleLinker(
                            scheduleInvoice.id,
                            _root_ide_package_.domain.enums.InvoiceModuleLinkerType.SCHEDULE_INVOICE
                        ))

                        if (scheduleInvoice.moduleLinker != null) {
                            val matchModuleType = when(scheduleInvoice.moduleLinker!!.module) {
                                _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.INCOME_SOURCE -> _root_ide_package_.domain.enums.InvoiceModuleLinkerType.INCOME_SOURCE
                                _root_ide_package_.domain.enums.ScheduleInvoiceModuleLinkerType.PROVISION -> _root_ide_package_.domain.enums.InvoiceModuleLinkerType.PROVISION
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
                            status = _root_ide_package_.domain.enums.InvoiceStatusType.PENDING,
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