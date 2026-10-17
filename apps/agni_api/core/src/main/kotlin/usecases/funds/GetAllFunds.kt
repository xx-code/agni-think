package usecases.funds

import usecases.UseCase
import adapters.IAuthenticateAdapter
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.dto.ScheduleRepeaterOutput
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Goal
import domain.entities.Fund
import domain.value_objects.SchedulerRecurrence
import usecases.dto.ListOutput
import usecases.funds.dto.FundGoalOutput
import usecases.funds.dto.FundScheduleOutput
import usecases.funds.dto.GetAllFundInput
import usecases.funds.dto.GetFundOutput
import usecases.funds.dto.SinkingFundDetailOutput

class GetAllFunds(
    private val fundRepo: IRepository<Fund>,
    private val goalRepo: IRepository<Goal>,
    auth: IAuthenticateAdapter): UseCase<GetAllFundInput, ListOutput<GetFundOutput>>(auth = auth) {
    override suspend fun process(input: GetAllFundInput): ListOutput<GetFundOutput> {
        val currentUser = getCurrentUser()
        val query = QueryFilter(
            offset = input.queryFilter.offset,
            limit = input.queryFilter.limit,
            queryAll = input.queryFilter.queryAll,
            sortBy = QuerySortBy("updated_at", false),
        )
        val condition = QueryExtendBuilder<Fund>()
            .addCondition("userId", QueryComparator.Equal, currentUser.id)
        if (input.type != null)
            condition.addCondition("type", QueryComparator.Equal, input.type.value)

        val funds = fundRepo.getAll(query, condition)
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.In, funds.items.map { it.id }.toSet())
        val goals = goalRepo.getAll(QueryFilter.queryAll(), conditionGoal)
        return ListOutput(
            items = funds.items.map {
                GetFundOutput(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    target = it.target,
                    balance = it.balance,
                    accountId = it.accountId,
                    type = it.type.value,
                    goals = goals.items.filter { goal -> goal.targetSourceId == it.id }.map { goal ->
                        FundGoalOutput(
                            id = goal.id,
                            title = goal.title,
                            dueDate = goal.dueDate
                        )
                    },
                    sinkingFundDetail = it.sinkingFundDetail?.let { sink ->
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
            },
            total = funds.total
        )
    }
}