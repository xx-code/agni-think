package usecases.funds

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Fund
import domain.exceptions.NotFoundException
import domain.entities.Goal
import usecases.funds.dto.FundGoalOutput
import usecases.funds.dto.FundScheduleOutput
import usecases.funds.dto.GetFundOutput
import usecases.funds.dto.SinkingFundDetailOutput
import java.util.UUID

class GetFund(
    private val fundRepo: IRepository<Fund>,
    private val goalRepo: IRepository<Goal>,
    auth: IAuthenticateAdapter
    ): UseCase<UUID, GetFundOutput>(auth = auth) {

    override suspend fun process(input: UUID): GetFundOutput {
        val fund = fundRepo.get(input) ?: throw NotFoundException.SingleEntity(input, "fund")
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.Equal, fund.id)
        val goals = goalRepo.getAll(QueryFilter.queryAll(), conditionGoal)

        return GetFundOutput(
            id = fund.id,
            title = fund.title,
            description = fund.description,
            target = fund.target,
            balance = fund.balance,
            accountId = fund.accountId,
            type = fund.type.value,
            goals = goals.items.map {
                FundGoalOutput(
                    id = it.id,
                    title = it.title,
                    dueDate = it.dueDate
                )
            },
            sinkingFundDetail = fund.sinkingFundDetail?.let { sink ->
                SinkingFundDetailOutput(
                    numberOfContributions = sink.numberOfContributions,
                    startDate = sink.startDate,
                    contributionRecurrence = ScheduleRepeaterOutput(
                        period = sink.contributionRecurrence.period.value,
                        interval = sink.contributionRecurrence.interval
                    ),
                    fundScheduler = sink.fundSchedule?.let { sch ->
                        FundScheduleOutput(
                            date = sch.date.toLocalDate(),
                            repeater = sch.repeater?.let { rep ->
                                ScheduleRepeaterOutput(
                                    period = rep.period.value,
                                    interval = rep.interval
                                )
                            }
                        )
                    },
                    isClosed = sink.fundSchedule?.isDueDate() ?: false
                )
            }
        )
    }
}