package dev.auguste.agni_api.core.usecases.goals

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.adapters.repositories.query_extend.QueryGoalExtend
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.factories.GoalEvaluationStrategyFactory
import dev.auguste.agni_api.core.usecases.ListOutput
import dev.auguste.agni_api.core.usecases.goals.dto.GetAllGoalInput
import dev.auguste.agni_api.core.usecases.goals.dto.GetGoalEvaluationOutput
import dev.auguste.agni_api.core.usecases.goals.dto.GetGoalOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class GetAllGoals(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext,
): IUseCase<GetAllGoalInput, ListOutput<GetGoalOutput>>{
    override fun execAsync(input: GetAllGoalInput): ListOutput<GetGoalOutput> {
        val res = goalRepo.getAll(
            query = input.queryFilter,
            QueryGoalExtend(
                sourceIds = input.sourceId?.let { setOf(it) },
                status = input.status,
                type = input.type
            )
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