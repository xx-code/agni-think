package dev.auguste.agni_api.core.usecases.saving_goals

import dev.auguste.agni_api.core.adapters.dto.QueryFilter
import dev.auguste.agni_api.core.adapters.dto.QuerySortBy
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.SavingGoal
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import dev.auguste.agni_api.core.usecases.saving_goals.dto.FundGoalOutput
import dev.auguste.agni_api.core.usecases.saving_goals.dto.GetSavingGoalOutput

class GetAllSavingGoal(
    private val savingGoalRepo: IRepository<SavingGoal>,
    private val goalRepo: IRepository<Goal>): IUseCase<QueryFilter, ListOutput<GetSavingGoalOutput>> {
    override fun execAsync(input: QueryFilter): ListOutput<GetSavingGoalOutput> {
        val query = QueryFilter(
            offset = input.offset,
            limit = input.limit,
            queryAll = input.queryAll,
            sortBy = QuerySortBy("updated_at", false),
        )
        val savingGoals = savingGoalRepo.getAll(query)
        val goals = goalRepo.getAll(QueryFilter.queryAll(), QueryGoalExtend(sourceIds = savingGoals.items.map { it.id }.toSet()))
        return ListOutput(
            items = savingGoals.items.map {
                GetSavingGoalOutput(
                   id = it.id,
                    title = it.title,
                    description = it.description,
                    target = it.target,
                    balance = it.balance,
                    accountId = it.accountId,
                    goals = goals.items.filter { fund -> fund.id == it.id }.map { goal ->
                        FundGoalOutput(
                            id = goal.id,
                            title = goal.title,
                            dueDate = goal.dueDate
                        )
                    }
                )
            },
            total = savingGoals.total
        )
    }
}