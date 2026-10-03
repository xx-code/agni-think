package usecases.goals

import adapters.IFinanceContext
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryCondition
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Goal
import domain.factories.GoalEvaluationStrategyFactory
import usecases.ListOutput
import usecases.goals.dto.GetAllGoalInput
import usecases.goals.dto.GetGoalEvaluationOutput
import usecases.goals.dto.GetGoalOutput
import usecases.interfaces.IUseCase

class GetAllGoals(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext,
): IUseCase<GetAllGoalInput, ListOutput<GetGoalOutput>>{
    override fun execAsync(input: GetAllGoalInput): ListOutput<GetGoalOutput> {
        val query = QueryFilter(
            offset = input.queryFilter.offset,
            limit =  input.queryFilter.limit,
            queryAll = input.queryFilter.queryAll,
            sortBy = QuerySortBy("due_date", true)
        )
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.In, input.sourceId?.let { setOf(it) })
            .addCondition("status", QueryComparator.Equal, input.status)
            .addCondition("type", QueryComparator.In, input.type?.value)
        val res = goalRepo.getAll(
            query = query,
            conditionGoal
        )

       return ListOutput(
           items = res.items.map {
               val strategy = GoalEvaluationStrategyFactory.getStrategy(it.type)
               val evaluation = it.evaluateProgress(strategy, financeContext)

               GetGoalOutput(
                   id = it.id,
                   title = it.title,
                   description = it.description,
                   targetAmount = it.targetAmount,
                   targetSourceId = it.targetSourceId,
                   dueDate = it.dueDate,
                   createdDate = it.createdAt.toLocalDate(),
                   status = it.status,
                   type = it.type,
                   evaluation = GetGoalEvaluationOutput(
                       currentBalance = evaluation.balance,
                       progressPercentage = evaluation.progressPercent
                   )
               )
           },
           total = res.total
       )
    }
}