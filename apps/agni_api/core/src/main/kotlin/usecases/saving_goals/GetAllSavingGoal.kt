package usecases.saving_goals

import usecases.UseCase
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Goal
import domain.entities.Fund
import usecases.dto.ListOutput
import usecases.saving_goals.dto.FundGoalOutput
import usecases.saving_goals.dto.GetAllSavingGoalInput
import usecases.saving_goals.dto.GetSavingGoalOutput

class GetAllSavingGoal(
    private val fundRepo: IRepository<Fund>,
    private val goalRepo: IRepository<Goal>): UseCase<GetAllSavingGoalInput, ListOutput<GetSavingGoalOutput>>() {
    override suspend fun process(input: GetAllSavingGoalInput): ListOutput<GetSavingGoalOutput> {
        val query = QueryFilter(
            offset = input.queryFilter.offset,
            limit = input.queryFilter.limit,
            queryAll = input.queryFilter.queryAll,
            sortBy = QuerySortBy("updated_at", false),
        )
        val condition = QueryExtendBuilder<Fund>()
        if (input.type != null)
            condition.addCondition("type", QueryComparator.Equal, input.type.value)

        val funds = fundRepo.getAll(query, condition)
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.In, funds.items.map { it.id }.toSet())
        val goals = goalRepo.getAll(QueryFilter.queryAll(), conditionGoal)
        return ListOutput(
            items = funds.items.map {
                GetSavingGoalOutput(
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
                    }
                )
            },
            total = funds.total
        )
    }
}