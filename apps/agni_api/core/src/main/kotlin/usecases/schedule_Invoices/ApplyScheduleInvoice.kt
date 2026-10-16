package usecases.schedule_Invoices

import usecases.interfaces.IUseCase

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
import domain.entities.Notification
import domain.enums.InvoiceModuleLinkerType
import domain.enums.InvoiceMovementType
import domain.enums.InvoiceStatusType
import domain.enums.InvoiceType
import domain.enums.ScheduleInvoiceModuleLinkerType
import domain.enums.ScheduleInvoiceType
import usecases.dto.BackgroundTaskOut
import usecases.dto.CreatedOutput
import usecases.invoices.dto.CreateFreezeInvoiceInput
import usecases.invoices.dto.CreateInvoiceInput
import usecases.invoices.dto.TransactionInput
import domain.value_objects.InvoiceModuleLinker
import domain.value_objects.Scheduler
import usecases.UseCase
import java.time.LocalDate
import java.time.LocalDateTime

class ApplyScheduleInvoice(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val createInvoice: IUseCase<CreateInvoiceInput, CreatedOutput>,
    private val createFreezeInvoice: IUseCase<CreateFreezeInvoiceInput, CreatedOutput>,
    private val eventManager: IEventRegister,
    unitOfWork: IUnitOfWork,
): UseCase<Unit, BackgroundTaskOut>(unitOfWork) {
    override suspend fun process(input: Unit): BackgroundTaskOut {
        try {
            val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("scheduler.date", QueryComparator.LesserOrEquals, LocalDateTime.now())
            val scheduleInvoices = scheduleInvoiceRepo.getAll(
                QueryFilter(0, 30, true),
                conditionScheduleInvoice,
            )

            for(scheduleInvoice in scheduleInvoices.items.filter { !it.isPause }) {
                var date = scheduleInvoice.scheduler.date

                val invoiceModuleLinkers = mutableListOf<InvoiceModuleLinker>()
                invoiceModuleLinkers.add(InvoiceModuleLinker(
                    scheduleInvoice.id,
                    InvoiceModuleLinkerType.SCHEDULE_INVOICE
                ))

                if (scheduleInvoice.moduleLinker != null) {
                    val matchModuleType = when(scheduleInvoice.moduleLinker!!.module) {
                        ScheduleInvoiceModuleLinkerType.INCOME_SOURCE -> InvoiceModuleLinkerType.INCOME_SOURCE
                        ScheduleInvoiceModuleLinkerType.PROVISION -> InvoiceModuleLinkerType.PROVISION
                        ScheduleInvoiceModuleLinkerType.FUND -> InvoiceModuleLinkerType.FUND
                    }

                    invoiceModuleLinkers.add(InvoiceModuleLinker(
                        scheduleInvoice.moduleLinker!!.sourceId,
                        matchModuleType,
                    ))
                }

                when(scheduleInvoice.type) {
                    ScheduleInvoiceType.STANDARD -> {
                        var movement = InvoiceMovementType.CREDIT
                        if (scheduleInvoice.invoiceType != InvoiceType.INCOME)
                            movement = InvoiceMovementType.DEBIT

                        createInvoice.processDirect(CreateInvoiceInput(
                            accountId = scheduleInvoice.accountId,
                            userId = scheduleInvoice.userId,
                            status = InvoiceStatusType.PENDING,
                            date = date,
                            type = scheduleInvoice.invoiceType,
                            movementType = movement,
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
                    ScheduleInvoiceType.FREEZE -> {
                        if (scheduleInvoice.freezeScheduler != null) {
                            date = scheduleInvoice.freezeScheduler!!.date
                        }

                        createFreezeInvoice.processDirect(CreateFreezeInvoiceInput(
                            title = scheduleInvoice.title,
                            userId = scheduleInvoice.userId,
                            accountId = scheduleInvoice.accountId,
                            endDate = date,
                            amount = scheduleInvoice.amount,
                            status = InvoiceStatusType.PENDING
                        ))
                    }
                    ScheduleInvoiceType.SAVING -> {
                        createInvoice.processDirect(CreateInvoiceInput(
                            accountId = scheduleInvoice.accountId,
                            userId = scheduleInvoice.userId,
                            status = InvoiceStatusType.PENDING,
                            date = date,
                            type = scheduleInvoice.invoiceType,
                            movementType = InvoiceMovementType.DEBIT,
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
                }


                if (scheduleInvoice.scheduler.repeater == null || (scheduleInvoice.endDate != null && scheduleInvoice.endDate!!.toLocalDate() < LocalDate.now())) {
                    scheduleInvoiceRepo.delete(scheduleInvoice.id)
                } else {
                    val date = scheduleInvoice.scheduler.upgradeDate()
                    scheduleInvoice.scheduler = Scheduler(
                        date = date,
                        scheduleInvoice.scheduler.repeater,
                    )

                    if (scheduleInvoice.type == ScheduleInvoiceType.FREEZE && scheduleInvoice.freezeScheduler != null) {
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
                    "La transaction ${when(scheduleInvoice.type) {
                        ScheduleInvoiceType.STANDARD -> ""
                        ScheduleInvoiceType.FREEZE -> "gele"
                        ScheduleInvoiceType.SAVING -> "amortie"
                    }} ${scheduleInvoice.title} at ${scheduleInvoice.amount}",
                    type = NotificationType.Success,
                    userId = scheduleInvoice.userId,
                ))
            }

            return BackgroundTaskOut("Apply Schedule Success")
        } catch (error: Throwable) {
            this.eventManager.notify(EventType.NOTIFICATION, NotificationEventContent(
                "Schedule Invoice !Error",
                "Error while applying schedule in voice ${error.message}",
                type = NotificationType.Error,
                userId = Notification.SYSTEM_USER_ID,
            ))

            return BackgroundTaskOut(error.localizedMessage)
        }
    }
}