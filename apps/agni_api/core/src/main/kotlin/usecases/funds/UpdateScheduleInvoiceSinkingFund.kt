package usecases.funds

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.enums.FundType
import domain.enums.ScheduleInvoiceModuleLinkerType
import domain.value_objects.Scheduler
import usecases.UseCase
import java.math.BigDecimal
import java.math.RoundingMode

class UpdateScheduleInvoiceSinkingFund(
    private val scheduleInvoiceRepo: IRepository<ScheduleInvoice>,
): UseCase<Fund, Unit>() {
    override suspend fun process(input: Fund) {
        if (input.type == FundType.SINKING_FUND) {
            val conditionScheduleInvoice = QueryExtendBuilder<ScheduleInvoice>()
                .addCondition("moduleLinker.sourceId", QueryComparator.Equal, input.id)
                .addCondition("moduleLinker.module", QueryComparator.Equal, ScheduleInvoiceModuleLinkerType.FUND.value)
            val scheduleInvoices = scheduleInvoiceRepo.getAll(QueryFilter.queryAll(), conditionScheduleInvoice)

            val scheduleInvoice = scheduleInvoices.items.firstOrNull()
            if (scheduleInvoice != null) {
                val startDate = input.sinkingFundDetail?.startDate
                val sinkingFundRecurrence = input.sinkingFundDetail?.contributionRecurrence
                val fundSchedule = input.sinkingFundDetail?.fundSchedule
                if (startDate != null && sinkingFundRecurrence != null && fundSchedule != null) {
                    val target = input.target - input.balance
                    val occurrence = sinkingFundRecurrence.computeOccurrences(
                        startDate,
                        fundSchedule.date.toLocalDate(),
                    )

                    val contribution = target / occurrence

                    val contributionRound = BigDecimal(contribution)
                        .setScale(2, RoundingMode.HALF_UP)
                        .toDouble()

                    val currentContributionRound = BigDecimal(scheduleInvoice.amount)
                        .setScale(2, RoundingMode.HALF_UP)
                        .toDouble()

                    if (contributionRound != currentContributionRound) {
                        scheduleInvoice.amount = contribution

                        // Make sure when a schedule is in pause to restart
                    }
                }

                val fundBalance = BigDecimal(input.balance)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()

                val fundTarget = BigDecimal(input.target)
                    .setScale(2, RoundingMode.HALF_UP)
                    .toDouble()

                if (fundTarget != fundBalance) {
                    scheduleInvoice.isPause = false
                    scheduleInvoice.scheduler = Scheduler(
                        date = scheduleInvoice.scheduler.upgradeDate(),
                        repeater = scheduleInvoice.scheduler.repeater
                    )
                } else if (fundTarget == fundBalance) {
                    scheduleInvoice.isPause = true
                }

                scheduleInvoiceRepo.update(scheduleInvoice)
            }
        }
    }
}