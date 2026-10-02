package usecases.goals

import adapters.IFinanceContext
import adapters.repositories.IRepository
import domain.entities.Goal
import domain.factories.GoalEvaluationStrategyFactory
import usecases.CreatedOutput
import usecases.goals.dto.CreateGoalInput
import usecases.interfaces.IUseCase

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