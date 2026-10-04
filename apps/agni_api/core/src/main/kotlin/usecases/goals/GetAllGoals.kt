package usecases.goals

import usecases.UseCase
import adapters.IFinanceContext
import adapters.dto.QueryFilter
import adapters.dto.QuerySortBy
import adapters.repositories.IRepository
import adapters.repositories.QueryExtendBuilder
import adapters.repositories.QueryComparator
import domain.entities.Goal
import domain.factories.GoalEvaluationStrategyFactory
import usecases.dto.ListOutput
import usecases.goals.dto.GetAllGoalInput
import usecases.goals.dto.GetGoalEvaluationOutput
import usecases.goals.dto.GetGoalOutput
class GetAllGoals(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext,
): UseCase<GetAllGoalInput, ListOutput<GetGoalOutput>>(){
    override suspend fun process(input: GetAllGoalInput): ListOutput<GetGoalOutput> {
        val query = QueryFilter(
            offset = input.queryFilter.offset,
            limit =  input.queryFilter.limit,
            queryAll = input.queryFilter.queryAll,
            sortBy = QuerySortBy("due_date", true)
        )
        val conditionGoal = QueryExtendBuilder<Goal>()
            .addCondition("targetSourceId", QueryComparator.In, input.sourceId?.let { setOf(it) })
            .addCondition("status", QueryComparator.Equal, input.status)
            .addCondition("type", QueryComparator.In, input.type?.let { setOf(it.value) })
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