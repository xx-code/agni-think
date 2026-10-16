package usecases.funds

import adapters.dto.QueryFilter
import adapters.events.EventType
import adapters.events.IEventRegister
import adapters.events.contents.NotificationEventContent
import adapters.events.contents.NotificationType
import adapters.repositories.IRepository
import adapters.repositories.IUnitOfWork
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.enums.FundType
import domain.enums.ScheduleInvoiceModuleLinkerType
import domain.value_objects.Scheduler
import domain.value_objects.SinkingFundDetail
import usecases.UseCase
import usecases.dto.BackgroundTaskOut
import usecases.funds.dto.DecreaseFundInput
import usecases.interfaces.IUseCase
import java.time.LocalDateTime

class FundAutoCurate(
    private val fundRepo: IRepository<Fund>,
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
    private val decreaseFund: IUseCase<DecreaseFundInput, Unit>,
    private val eventRegister: IEventRegister,
    uow: IUnitOfWork
): UseCase<Unit, BackgroundTaskOut>(uow) {
    override suspend fun process(input: Unit): BackgroundTaskOut {
        val conditionFund = QueryExtendBuilder<Fund>()
            .addCondition("type", QueryComparator.Equal, FundType.SINKING_FUND.value)
        val funds = fundRepo.getAll(QueryFilter.queryAll(), conditionFund)
        val sinkingFunds = funds.items.filter { it.sinkingFundDetail != null }

        for (fund in sinkingFunds) {
            val sinkingFundDetail = fund.sinkingFundDetail!!
            if (sinkingFundDetail.fundSchedule != null &&  sinkingFundDetail.fundSchedule.date <= LocalDateTime.now() && fund.accountId != null) {
                decreaseFund.processDirect(DecreaseFundInput(
                    userId = fund.userId,
                    fund.id,
                    amount = fund.balance,
                    accountId = fund.accountId!!
                ))

                if (sinkingFundDetail.fundSchedule.repeater != null) {
                    fund.sinkingFundDetail = SinkingFundDetail(
                        startDate = sinkingFundDetail.fundSchedule.date.toLocalDate(),
                        numberOfContributions = sinkingFundDetail.numberOfContributions,
                        contributionRecurrence = sinkingFundDetail.contributionRecurrence,
                        fundSchedule = Scheduler(
                            date = sinkingFundDetail.fundSchedule.upgradeDate(),
                            repeater = sinkingFundDetail.fundSchedule.repeater,
                        )
                    )
                    fundRepo.update(fund)
                }

                eventRegister.notify(
                    EventType.NOTIFICATION, NotificationEventContent(
                        "Fond resync",
                        "Le fond ${fund.title} a recommencer et a libere ${fund.balance}",
                        type = NotificationType.Success,
                        userId = fund.userId,
                    )
                )
            } else if (fund.balance == fund.target && sinkingFundDetail.fundSchedule == null) {
                val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
                    .addCondition("moduleLinker.sourceId", QueryComparator.Equal, fund.id)
                    .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.FUND.value)
                val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), conditionScheduleInvoice)
                val scheduleInvoice = scheduleInvoices.items.firstOrNull()
                if (scheduleInvoice != null) {
                    scheduleInvoice.isPause = true
                    scheduleInvoiceRepo.update(scheduleInvoice)
                }

                eventRegister.notify(
                    EventType.NOTIFICATION, NotificationEventContent(
                        "Fond pause",
                        "Le fond ${fund.title} a mis en pause son schedule",
                        type = NotificationType.Success,
                        userId = fund.userId,
                    )
                )
            }
        }

        return BackgroundTaskOut("")
    }
}