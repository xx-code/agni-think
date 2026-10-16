package usecases.funds

import adapters.dto.QueryFilter
import adapters.repositories.IRepository
import adapters.repositories.QueryComparator
import adapters.repositories.QueryExtendBuilder
import domain.entities.Fund
import domain.entities.ScheduleInvoice
import domain.enums.FundType
import domain.enums.ScheduleInvoiceModuleLinkerType
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
            val sinkingFundRecurrence = input.sinkingFundDetail?.contributionRecurrence
            val fundSchedule = input.sinkingFundDetail?.fundSchedule
            if (scheduleInvoice != null && sinkingFundRecurrence != null && fundSchedule != null) {
                val target = input.target - input.balance
                val occurrence = sinkingFundRecurrence.computeOccurrences(
                    scheduleInvoice.scheduler.date.toLocalDate(),
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
                    scheduleInvoiceRepo.update(scheduleInvoice)
                }
            }
        }
    }
}