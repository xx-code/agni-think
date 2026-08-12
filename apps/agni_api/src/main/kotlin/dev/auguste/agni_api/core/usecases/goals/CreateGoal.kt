package dev.auguste.agni_api.core.usecases.goals

import dev.auguste.agni_api.core.adapters.IFinanceContext
import dev.auguste.agni_api.core.adapters.repositories.IRepository
import dev.auguste.agni_api.core.entities.Goal
import dev.auguste.agni_api.core.entities.factories.GoalEvaluationStrategyFactory
import dev.auguste.agni_api.core.usecases.CreatedOutput
import dev.auguste.agni_api.core.usecases.goals.dto.CreateGoalInput
import dev.auguste.agni_api.core.usecases.interfaces.IUseCase

class CreateGoal(
    private val goalRepo: IRepository<Goal>,
    private val financeContext: IFinanceContext
): IUseCase<CreateGoalInput, CreatedOutput> {
    override fun execAsync(input: CreateGoalInput): CreatedOutput {
        val newGoal = Goal(
            title = input.title,
            description = input.description,
            targetSourceId = input.targetSourceId,
            targetAmount = input.targetAmount,
            dueDate = input.targetDate,
            status = input.status,
            type = input.type,
        )

        val strategy = GoalEvaluationStrategyFactory.getStrategy(newGoal.type)
        strategy.verifyGoalBusinessLogic(newGoal, financeContext)
        newGoal.evaluateProgress(strategy, financeContext)

        goalRepo.create(newGoal)

        return CreatedOutput(newGoal.id)
    }
}