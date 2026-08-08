package dev.auguste.agni_api.core.usecases.goals

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.DomainException
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.factories.GoalEvaluationStrategyFactory
import dev.auguste.agni_api.core.usecases.goals.dto.GetGoalEvaluationOutput
import dev.auguste.agni_api.core.usecases.goals.dto.GetGoalOutput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase
import java.util.UUID

class GetGoal(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext
): IUseCase<UUID, GetGoalOutput> {
    override fun execAsync(input: UUID): GetGoalOutput {
        val goal = goalRepo.get(input) ?: throw DomainException.NotFound.Goal(input)

        val strategy = GoalEvaluationStrategyFactory.getStrategy(goal.type)
        val evaluation = goal.evaluateProgress(strategy, financeContext)

        return GetGoalOutput(
            id = goal.id,
            title = goal.title,
            description = goal.description,
            targetAmount = goal.targetAmount,
            targetSourceId = goal.targetSourceId,
            dueDate = goal.dueDate,
            createdDate = goal.createdAt,
            status = goal.status,
            type = goal.type,
            evaluation = GetGoalEvaluationOutput(
                currentBalance = evaluation.balance,
                progressPercentage = evaluation.progressPercent
            )
        )
    }
}